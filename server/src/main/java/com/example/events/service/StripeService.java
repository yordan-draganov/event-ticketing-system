package com.example.events.service;

import com.example.events.DTO.*;
import com.example.events.exception.PaymentProcessingException;
import com.example.events.exception.UnauthorizedException;
import com.example.events.exception.ValidationException;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketService ticketService;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentResponse createPaymentIntent(UUID eventId, List<UUID> seatIds, UUID userId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

            List<Seat> seats = seatRepository.findAllById(seatIds);
            if (seats.size() != seatIds.size()) {
                throw new ResourceNotFoundException("Some seats not found");
            }

            boolean hasSeatFromDifferentEvent = seats.stream().anyMatch(seat -> seat.getEvent() == null || !seat.getEvent().getId().equals(eventId));
            if (hasSeatFromDifferentEvent) {
                throw new ValidationException("Some seats do not belong to the selected event");
            }

            boolean hasUnavailableSeats = seats.stream().anyMatch(seat -> !Boolean.TRUE.equals(seat.getIsAvailable()) || seat.getTicket() != null);
            if (hasUnavailableSeats) {
                throw new ValidationException("Some seats are no longer available");
            }

            BigDecimal totalAmount = seats.stream()
                    .map(seat -> seat.getSection().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("eventId", eventId.toString())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("seatIds", seatIds.toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return PaymentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .amount(totalAmount)
                    .currency("USD")
                    .build();

        } catch (StripeException e) {
            throw new PaymentProcessingException("Stripe error: " + e.getMessage(), e);
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

            TicketResponse ticket = ticketService.findTicketByUserEventAndSeats(userId, eventId, seatIds);

            if (ticket != null) {
                logger.info("Ticket already exists for payment {}, returning existing ticket", paymentIntentId);
                return ResponseEntity.ok(ticket);
            }

            logger.info("No ticket found for payment {}, creating new ticket", paymentIntentId);
            try {
                TicketCreateDTO ticketRequest = new TicketCreateDTO(eventId, seatIds);
                
                TicketResponse createdTicket = ticketService.createTicket(ticketRequest, userId);
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

    public void cancelPaymentIntent(String paymentIntentId, UUID userId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            assertPaymentOwnership(intent, userId);
            intent.cancel(PaymentIntentCancelParams.builder().build());
        } catch (StripeException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }
    }

    private void assertPaymentOwnership(PaymentIntent intent, UUID userId) {
        String paymentUserId = intent.getMetadata().get("userId");
        if (paymentUserId == null || !userId.toString().equals(paymentUserId)) {
            throw new UnauthorizedException("This payment does not belong to you");
        }
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
