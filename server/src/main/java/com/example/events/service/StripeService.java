package com.example.events.service;

import com.example.events.DTO.PaymentResponse;
import com.example.events.exception.ValidationException;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.model.Section;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe initialized with API key");
    }

    @Transactional(readOnly = true)
    public PaymentResponse createPaymentIntent(UUID eventId, List<UUID> seatIds, UUID userId) {
        try {
            logger.info("Creating PaymentIntent for user {} with {} seats", userId, seatIds.size());

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ValidationException("Event not found"));

            if (event.getIsFinished()) {
                throw new ValidationException("Cannot purchase tickets for a finished event");
            }

            List<Seat> seats = seatRepository.findAllById(seatIds).stream()
                    .filter(seat -> seat.getEvent().getId().equals(eventId))
                    .toList();

            if (seats.size() != seatIds.size()) {
                throw new ValidationException("Some seats not found or do not belong to this event");
            }

            List<Seat> unavailableSeats = seats.stream()
                    .filter(seat -> !seat.getIsAvailable() || seat.getTicket() != null)
                    .toList();

            if (!unavailableSeats.isEmpty()) {
                throw new ValidationException("Some seats are not available");
            }

            Set<UUID> sectionIds = seats.stream()
                    .map(seat -> seat.getSection().getId())
                    .collect(java.util.stream.Collectors.toSet());

            if (sectionIds.size() != 1) {
                throw new ValidationException("All seats must be from the same section");
            }

            Section section = seats.get(0).getSection();
            BigDecimal totalPrice = section.getPrice().multiply(BigDecimal.valueOf(seats.size()));

            long amountInCents = totalPrice
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", userId.toString());
            metadata.put("eventId", eventId.toString());
            metadata.put("eventTitle", event.getTitle());
            metadata.put("seatIds", seatIds.toString());
            metadata.put("seatCount", String.valueOf(seats.size()));
            metadata.put("sectionId", section.getId().toString());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setDescription("Ticket purchase for " + event.getTitle())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            logger.info("PaymentIntent created: {} for amount: ${}", paymentIntent.getId(), totalPrice);

            return PaymentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(totalPrice)
                    .currency("usd")
                    .status(paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            logger.error("Stripe error creating PaymentIntent: {}", e.getMessage());
            throw new RuntimeException("Payment processing error: " + e.getMessage());
        }
    }

    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            logger.error("Error retrieving PaymentIntent: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve payment: " + e.getMessage());
        }
    }

    public boolean isPaymentSuccessful(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            logger.error("Error checking payment status: {}", e.getMessage());
            return false;
        }
    }

    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            if (!"succeeded".equals(paymentIntent.getStatus()) &&
                    !"canceled".equals(paymentIntent.getStatus())) {
                paymentIntent.cancel();
                logger.info("PaymentIntent cancelled: {}", paymentIntentId);
            }
        } catch (StripeException e) {
            logger.error("Error cancelling PaymentIntent: {}", e.getMessage());
        }
    }
}