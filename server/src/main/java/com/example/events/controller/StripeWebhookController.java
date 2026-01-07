package com.example.events.controller;

import com.example.events.DTO.TicketCreateDTO;
import com.example.events.service.TicketService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    private final TicketService ticketService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            logger.error("Webhook secret is not configured. Rejecting webhook for security.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
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
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;

                case "payment_intent.canceled":
                    logger.info("Payment intent canceled: {}", eventId);
                    break;

                default:
                    logger.info("Unhandled event type: {}", eventType);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing webhook event {} (id: {}): {}", eventType, eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    private void handlePaymentSucceeded(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        PaymentIntent paymentIntent;

        try {
            if (dataObjectDeserializer.getObject().isPresent()) {
                paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            } else {
                logger.warn("Safe deserialization failed for event {}. Attempting unsafe deserialization.", event.getId());
                paymentIntent = (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
            }
        } catch (EventDataObjectDeserializationException e) {
            logger.error("Failed to deserialize PaymentIntent from event {}: {}", event.getId(), e.getMessage(), e);
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

        String userIdStr = metadata.get("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty() || !UUID_PATTERN.matcher(userIdStr.trim()).matches()) {
            logger.error("Invalid or missing userId in metadata for payment: {}", paymentIntentId);
            return;
        }
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid userId format in metadata for payment: {}", paymentIntentId);
            return;
        }

        String eventIdStr = metadata.get("eventId");
        if (eventIdStr == null || eventIdStr.trim().isEmpty() || !UUID_PATTERN.matcher(eventIdStr.trim()).matches()) {
            logger.error("Invalid or missing eventId in metadata for payment: {}", paymentIntentId);
            return;
        }
        UUID eventId;
        try {
            eventId = UUID.fromString(eventIdStr.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid eventId format in metadata for payment: {}", paymentIntentId);
            return;
        }

        String seatIdsStr = metadata.get("seatIds");
        List<UUID> seatIds = parseSeatIds(seatIdsStr, paymentIntentId);

        if (seatIds.isEmpty()) {
            logger.error("No valid seatIds found in metadata for payment: {}", paymentIntentId);
            return;
        }

        TicketCreateDTO ticketRequest = new TicketCreateDTO();
        ticketRequest.setEventId(eventId);
        ticketRequest.setSeatIds(seatIds);

        ticketService.createTicket(ticketRequest, userId);

        logger.info("Ticket created successfully via webhook for payment: {}", paymentIntentId);
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

        String[] seatIdArray = cleaned.split(",");
        for (String seatId : seatIdArray) {
            String trimmed = seatId.trim();
            if (!trimmed.isEmpty()) {
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
        }

        return seatIds;
    }

    private void handlePaymentFailed(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        PaymentIntent paymentIntent;

        try {
            if (dataObjectDeserializer.getObject().isPresent()) {
                paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            } else {
                paymentIntent = (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
            }
        } catch (EventDataObjectDeserializationException e) {
            logger.error("Failed to deserialize PaymentIntent from event {}: {}", event.getId(), e.getMessage(), e);
            return;
        }

        String errorMessage = paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Unknown";
        
        logger.warn("Payment failed: {} - Reason: {}", paymentIntent.getId(), errorMessage);
    }
}