package com.example.events.service;

import com.example.events.DTO.*;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.service.TicketService;
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

            long totalAmount = seats.stream()
                    .mapToLong(seat -> seat.getSection().getPrice().longValue())
                    .sum();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(totalAmount * 100)
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
                    .amount(BigDecimal.valueOf(totalAmount))
                    .currency("USD")
                    .build();

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
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

            // Ticket doesn't exist, create it
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

    public PaymentStatusResponse getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            return PaymentStatusResponse.builder()
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .build();

        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.cancel(PaymentIntentCancelParams.builder().build());
        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<UUID> parseSeatIds(String seatIdsStr) {
        List<UUID> result = new ArrayList<>();
        if (seatIdsStr == null) return result;

        seatIdsStr = seatIdsStr.replace("[", "").replace("]", "");
        for (String id : seatIdsStr.split(",")) {
            result.add(UUID.fromString(id.trim()));
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

    private ResponseEntity<ErrorResponse> accepted(String msg) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ErrorResponse.builder()
                        .status(202)
                        .error("Processing")
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
