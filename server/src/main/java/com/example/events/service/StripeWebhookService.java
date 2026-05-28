package com.example.events.service;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    private final StripeService stripeService;

    public ResponseEntity<String> handleWebhook(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            logger.error("Webhook secret is not configured. Rejecting webhook for security.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error constructing webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook payload");
        }

        String eventType = event.getType();
        String eventId = event.getId();
        logger.info("Received webhook event: {} (id: {})", eventType, eventId);

        try {
            switch (eventType) {
                case "payment_intent.succeeded" -> handleSucceeded(event);
                case "payment_intent.payment_failed" -> handleFailed(event);
                case "payment_intent.canceled" -> handleCanceled(event);
                default -> logger.info("Unhandled event type: {}", eventType);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing webhook event {} (id: {}): {}", eventType, eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    private void handleSucceeded(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        if (paymentIntent == null) {
            return;
        }

        String paymentIntentId = paymentIntent.getId();
        logger.info("Processing payment succeeded: {}", paymentIntentId);

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            logger.warn("Payment intent {} status is '{}', not 'succeeded'. Skipping ticket creation.",
                    paymentIntentId, paymentIntent.getStatus());
            return;
        }

        Map<String, String> metadata = paymentIntent.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            logger.error("No metadata found for payment: {}", paymentIntentId);
            return;
        }

        UUID userId = parseRequiredUuid(metadata, "userId", paymentIntentId);
        UUID eventId = parseRequiredUuid(metadata, "eventId", paymentIntentId);
        UUID reservationId = parseRequiredUuid(metadata, "reservationId", paymentIntentId);
        List<UUID> seatIds = parseSeatIds(metadata.get("seatIds"), paymentIntentId);

        if (userId == null || eventId == null || reservationId == null) {
            return;
        }

        if (seatIds.isEmpty()) {
            logger.error("No valid seatIds found in metadata for payment: {}", paymentIntentId);
            return;
        }

        stripeService.finalizeSuccessfulPayment(paymentIntentId, userId, eventId, reservationId, seatIds);
        logger.info("Ticket created successfully via webhook for payment: {}", paymentIntentId);
    }

    private void handleFailed(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        if (paymentIntent == null) {
            return;
        }

        String errorMessage = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Unknown";

        logger.warn("Payment failed: {} - Reason: {}", paymentIntent.getId(), errorMessage);
        stripeService.releaseReservation(paymentIntent.getId());
    }

    private void handleCanceled(Event event) {
        PaymentIntent paymentIntent = deserializePaymentIntent(event);
        if (paymentIntent == null) {
            return;
        }

        logger.info("Payment intent canceled: {}", paymentIntent.getId());
        stripeService.releaseReservation(paymentIntent.getId());
    }

    private PaymentIntent deserializePaymentIntent(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        try {
            if (dataObjectDeserializer.getObject().isPresent()) {
                return (PaymentIntent) dataObjectDeserializer.getObject().get();
            }

            logger.warn("Safe deserialization failed for event {}. Attempting unsafe deserialization.", event.getId());
            return (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            logger.error("Failed to deserialize PaymentIntent from event {}: {}", event.getId(), e.getMessage(), e);
            return null;
        }
    }

    private UUID parseRequiredUuid(Map<String, String> metadata, String key, String paymentIntentId) {
        String value = metadata.get(key);
        if (value == null || value.trim().isEmpty() || !UUID_PATTERN.matcher(value.trim()).matches()) {
            logger.error("Invalid or missing {} in metadata for payment: {}", key, paymentIntentId);
            return null;
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid {} format in metadata for payment: {}", key, paymentIntentId);
            return null;
        }
    }

    private List<UUID> parseSeatIds(String seatIdsStr, String paymentIntentId) {
        List<UUID> seatIds = new ArrayList<>();

        if (seatIdsStr == null || seatIdsStr.trim().isEmpty()) {
            return seatIds;
        }

        String cleaned = seatIdsStr.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }

        if (cleaned.isEmpty()) {
            return seatIds;
        }

        for (String seatId : cleaned.split(",")) {
            String trimmed = seatId.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (!UUID_PATTERN.matcher(trimmed).matches()) {
                logger.warn("Invalid UUID format in seatIds for payment {}: {}", paymentIntentId, trimmed);
                continue;
            }

            try {
                seatIds.add(UUID.fromString(trimmed));
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to parse UUID in seatIds for payment {}: {}", paymentIntentId, trimmed);
            }
        }

        return seatIds;
    }
}
