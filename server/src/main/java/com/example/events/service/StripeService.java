package com.example.events.service;

import com.example.events.DTO.*;
import com.example.events.exception.PaymentProcessingException;
import com.example.events.exception.UnauthorizedException;
import com.example.events.exception.ValidationException;
import com.example.events.model.Event;
import com.example.events.model.Reservation;
import com.example.events.model.ReservationStatus;
import com.example.events.model.Seat;
import com.example.events.model.User;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.mapper.EventMapper;
import com.example.events.mapper.SeatMapper;
import com.example.events.mapper.SectionMapper;
import com.example.events.repository.EventRepository;
import com.example.events.repository.ReservationRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.repository.SectionRepository;
import com.example.events.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${checkout.reservation-ttl-minutes:10}")
    private long reservationTtlMinutes;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;
    private final TicketService ticketService;
    private final PlatformTransactionManager transactionManager;
    private final EventMapper eventMapper;
    private final SectionMapper sectionMapper;
    private final SeatMapper seatMapper;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentResponse createPaymentIntent(UUID eventId, List<UUID> seatIds, UUID userId) {
        Reservation reservation = null;
        try {
            reservation = reserveSeats(eventId, seatIds, userId);

            long amountInCents = reservation.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("eventId", eventId.toString())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("seatIds", seatIds.toString())
                    .putMetadata("reservationId", reservation.getId().toString())
                    .putMetadata("reservationExpiresAt", toUtcOffset(reservation.getExpiresAt()).toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            attachPaymentIntentToReservation(reservation.getId(), intent.getId());

            return PaymentResponse.builder()
                    .reservationId(reservation.getId())
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .amount(reservation.getTotalAmount())
                    .currency("USD")
                    .status(reservation.getStatus().name())
                    .reservationExpiresAt(toUtcOffset(reservation.getExpiresAt()))
                    .build();

        } catch (StripeException e) {
            releaseReservation(reservation);
            throw new PaymentProcessingException("Stripe error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            releaseReservation(reservation);
            throw e;
        }
    }

    public ResponseEntity<?> confirmPayment(String paymentIntentId, UUID userId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if (!userId.toString().equals(intent.getMetadata().get("userId"))) {
                return forbidden("This payment does not belong to you");
            }

            if (!"succeeded".equals(intent.getStatus())) {
                return paymentRequired("Payment status: " + intent.getStatus());
            }

            UUID eventId = UUID.fromString(intent.getMetadata().get("eventId"));
            List<UUID> seatIds = parseSeatIds(intent.getMetadata().get("seatIds"));
            Reservation reservation = reservationRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new ValidationException("Reservation not found for payment"));

            TicketResponse ticket = ticketService.findTicketByUserEventAndSeats(userId, eventId, seatIds);

            if (ticket != null) {
                logger.info("Ticket already exists for payment {}, returning existing ticket", paymentIntentId);
                return ResponseEntity.ok(ticket);
            }

            logger.info("No ticket found for payment {}, creating new ticket", paymentIntentId);
            try {
                TicketCreateDTO ticketRequest = new TicketCreateDTO(eventId, seatIds);
                
                TicketResponse createdTicket = ticketService.createTicket(ticketRequest, userId, reservation.getId());
                markReservationPaid(reservation.getId());
                logger.info("Ticket created successfully with id: {} for payment {}", createdTicket.getId(), paymentIntentId);
                return ResponseEntity.ok(createdTicket);
            } catch (Exception e) {
                logger.error("Failed to create ticket for payment {}: {}", paymentIntentId, e.getMessage(), e);
                return serverError("Failed to create ticket: " + e.getMessage());
            }

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    @Transactional
    public CheckoutSessionResponse getCheckoutSession(UUID reservationId, UUID userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.belongsTo(userId)) {
            throw new UnauthorizedException("This reservation does not belong to you");
        }

        if (reservation.getStatus() != ReservationStatus.pending) {
            throw new ValidationException("Reservation is not pending");
        }

        if (!reservation.isActive()) {
            releaseReservation(reservation);
            throw new ResponseStatusException(HttpStatus.GONE, "Reservation expired");
        }

        if (reservation.getPaymentIntentId() == null || reservation.getPaymentIntentId().isBlank()) {
            throw new ValidationException("Reservation payment is not initialized");
        }

        List<Seat> seats = seatRepository.findByReservationIdOrderByRowLabelAscSeatNumberAsc(reservationId);
        if (seats.isEmpty()) {
            throw new ValidationException("Reservation has no seats");
        }

        PaymentIntent intent = retrievePaymentIntent(reservation.getPaymentIntentId());
        Event event = reservation.getEvent();

        EventResponse eventResponse = eventMapper.toResponseDTO(event);
        List<SectionResponse> sections = sectionRepository.findByEventIdOrderByNameAsc(event.getId())
                .stream()
                .map(section -> {
                    SectionResponse response = sectionMapper.toResponse(section);
                    response.setAvailableSeats((int) seatRepository.countAvailableBySectionId(section.getId()));
                    return response;
                })
                .toList();

        eventResponse.setMinPrice(sections.stream()
                .map(SectionResponse::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));
        eventResponse.setMaxPrice(sections.stream()
                .map(SectionResponse::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));
        eventResponse.setTotalSeats(sections.stream()
                .mapToInt(section -> section.getRowsCount() * section.getColsCount())
                .sum());
        eventResponse.setAvailableSeats((int) seatRepository.countAvailableByEventId(event.getId()));
        eventResponse.setSectionCount(sections.size());

        SectionResponse selectedSection = sectionMapper.toResponse(seats.get(0).getSection());
        selectedSection.setAvailableSeats((int) seatRepository.countAvailableBySectionId(selectedSection.getId()));

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .reservationId(reservation.getId())
                .clientSecret(intent.getClientSecret())
                .paymentIntentId(intent.getId())
                .amount(reservation.getTotalAmount())
                .currency(intent.getCurrency() == null ? "USD" : intent.getCurrency().toUpperCase())
                .status(reservation.getStatus().name())
                .reservationExpiresAt(toUtcOffset(reservation.getExpiresAt()))
                .build();

        return CheckoutSessionResponse.builder()
                .event(eventResponse)
                .sections(sections)
                .selectedSeats(seats.stream().map(seatMapper::toResponse).toList())
                .selectedSection(selectedSection)
                .totalPrice(reservation.getTotalAmount())
                .payment(paymentResponse)
                .build();
    }

    public PaymentStatusResponse getPaymentStatus(String paymentIntentId, UUID userId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            assertPaymentOwnership(intent, userId);

            return PaymentStatusResponse.builder()
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }
    }

    private PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }
    }

    public void cancelPaymentIntent(String paymentIntentId, UUID userId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            assertPaymentOwnership(intent, userId);
            intent.cancel(PaymentIntentCancelParams.builder().build());
            releaseReservation(paymentIntentId);
        } catch (StripeException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }
    }

    public void releaseReservation(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return;
        }

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            reservationRepository.findByPaymentIntentIdForUpdate(paymentIntentId)
                    .ifPresent(reservation -> {
                        reservation.setStatus(ReservationStatus.cancelled);
                        reservationRepository.save(reservation);
                    });
            seatRepository.clearReservationByPaymentIntentId(paymentIntentId);
        });
    }

    private void releaseReservation(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            return;
        }

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Reservation lockedReservation = reservationRepository.findById(reservation.getId()).orElse(null);
            if (lockedReservation != null) {
                lockedReservation.setStatus(ReservationStatus.cancelled);
                reservationRepository.save(lockedReservation);
            }
            seatRepository.clearReservationByReservationId(reservation.getId());
        });
    }

    private Reservation reserveSeats(UUID eventId, List<UUID> seatIds, UUID userId) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (event.getIsFinished()) {
                throw new ValidationException("Cannot purchase tickets for a finished event");
            }

            List<Seat> seats = seatRepository.findByIdInAndEventId(seatIds, eventId);
            if (seats.size() != seatIds.size()) {
                throw new ResourceNotFoundException("Some seats not found");
            }

            boolean hasUnavailableSeats = seats.stream().anyMatch(seat -> !seat.isAvailableForPurchase());
            if (hasUnavailableSeats) {
                throw new ValidationException("Some seats are no longer available");
            }

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(reservationTtlMinutes);
            BigDecimal totalAmount = seats.stream()
                    .map(seat -> seat.getSection().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .event(event)
                    .status(ReservationStatus.pending)
                    .expiresAt(expiresAt)
                    .totalAmount(totalAmount)
                    .build();
            Reservation savedReservation = reservationRepository.save(reservation);

            for (Seat seat : seats) {
                seat.reserve(savedReservation);
                seatRepository.save(seat);
            }

            return savedReservation;
        });
    }

    private void attachPaymentIntentToReservation(UUID reservationId, String paymentIntentId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            reservation.setPaymentIntentId(paymentIntentId);
            reservationRepository.save(reservation);
            seatRepository.attachPaymentIntentToReservationSeats(reservationId, paymentIntentId);
        });
    }

    public void markReservationPaid(UUID reservationId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            reservation.setStatus(ReservationStatus.paid);
            reservationRepository.save(reservation);
        });
    }

    private void assertPaymentOwnership(PaymentIntent intent, UUID userId) {
        String paymentUserId = intent.getMetadata().get("userId");
        if (paymentUserId == null || !userId.toString().equals(paymentUserId)) {
            throw new UnauthorizedException("This payment does not belong to you");
        }
    }

    private OffsetDateTime toUtcOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    private List<UUID> parseSeatIds(String seatIdsStr) {
        List<UUID> result = new ArrayList<>();
        if (seatIdsStr == null || seatIdsStr.trim().isEmpty()) {
            return result;
        }

        try {
            seatIdsStr = seatIdsStr.replace("[", "").replace("]", "").trim();
            if (seatIdsStr.isEmpty()) {
                return result;
            }

            for (String id : seatIdsStr.split(",")) {
                String trimmedId = id.trim();
                if (!trimmedId.isEmpty()) {
                    result.add(UUID.fromString(trimmedId));
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format in seat IDs: {}", seatIdsStr, e);
            throw new ValidationException("Invalid seat ID format");
        }

        return result;
    }

    private ResponseEntity<ErrorResponse> forbidden(String msg) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(403)
                        .error("Forbidden")
                        .message(msg)
                        .build());
    }

    private ResponseEntity<ErrorResponse> paymentRequired(String msg) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ErrorResponse.builder()
                        .status(402)
                        .error("Payment Required")
                        .message(msg)
                        .build());
    }

    private ResponseEntity<ErrorResponse> serverError(String msg) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(500)
                        .error("Internal Server Error")
                        .message(msg)
                        .build());
    }
}
