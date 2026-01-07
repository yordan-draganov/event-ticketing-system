package com.example.events.controller;

import com.example.events.DTO.TicketCreateDTO;
import com.example.events.service.TicketService;
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

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    private final TicketService ticketService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret.isEmpty() ? null : webhookSecret
            );
        } catch (SignatureVerificationException e) {
            logger.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }


        String eventType = event.getType();
        logger.info("Received webhook event: {}", eventType);

        switch (eventType) {
            case "payment_intent.succeeded":
                handlePaymentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentFailed(event);
                break;

            case "payment_intent.canceled":
                logger.info("Payment intent canceled: {}", event.getId());
                break;

            default:
                logger.info("Unhandled event type: {}", eventType);
        }

        return ResponseEntity.ok("Webhook received");
    }

    private void handlePaymentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize PaymentIntent"));

            logger.info("Payment succeeded: {}", paymentIntent.getId());

            Map<String, String> metadata = paymentIntent.getMetadata();

            UUID userId = UUID.fromString(metadata.get("userId"));
            UUID eventId = UUID.fromString(metadata.get("eventId"));
            String seatIdsStr = metadata.get("seatIds");

            seatIdsStr = seatIdsStr.substring(1, seatIdsStr.length() - 1);
            String[] seatIdArray = seatIdsStr.split(",");
            List<UUID> seatIds = new ArrayList<>();
            for (String seatId : seatIdArray) {
                seatIds.add(UUID.fromString(seatId.trim()));
            }

            TicketCreateDTO ticketRequest = new TicketCreateDTO();
            ticketRequest.setEventId(eventId);
            ticketRequest.setSeatIds(seatIds);

            ticketService.createTicket(ticketRequest, userId);

            logger.info("Ticket created successfully via webhook for payment: {}", paymentIntent.getId());

        } catch (Exception e) {
            logger.error("Error handling payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize PaymentIntent"));

            logger.warn("Payment failed: {} - Reason: {}",
                    paymentIntent.getId(),
                    paymentIntent.getLastPaymentError() != null ?
                            paymentIntent.getLastPaymentError().getMessage() : "Unknown");


        } catch (Exception e) {
            logger.error("Error handling payment_intent.payment_failed: {}", e.getMessage(), e);
        }
    }
}