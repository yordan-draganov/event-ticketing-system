package com.example.events.controller;

import com.example.events.DTO.ErrorResponse;
import com.example.events.DTO.PaymentStatusResponse;
import com.example.events.DTO.PaymentResponse;
import com.example.events.DTO.PaymentDTO;
import com.example.events.DTO.PaymentConfirmDTO;
import com.example.events.DTO.TicketResponse;
import com.example.events.exception.UnauthorizedException;
import com.example.events.service.StripeService;
import com.example.events.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final StripeService stripeService;
    private final TicketService ticketService;

    @PostMapping("/create-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        logger.info("Creating payment intent for user: {}", userId);

        PaymentResponse response = stripeService.createPaymentIntent(
                request.getEventId(),
                request.getSeatIds(),
                userId
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> confirmPayment(
            @Valid @RequestBody PaymentConfirmDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        logger.info("Confirming payment {} for user: {}", request.getPaymentIntentId(), userId);

        try {
            com.stripe.model.PaymentIntent paymentIntent =
                    stripeService.getPaymentIntent(request.getPaymentIntentId());

            // Verify payment belongs to user
            String metadataUserId = paymentIntent.getMetadata().get("userId");
            if (!userId.toString().equals(metadataUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message("This payment does not belong to you")
                                .build());
            }

            // Check payment status
            String paymentStatus = paymentIntent.getStatus();
            if (!"succeeded".equals(paymentStatus)) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.PAYMENT_REQUIRED.value())
                                .error("Payment Required")
                                .message("Payment status: " + paymentStatus)
                                .build());
            }

            // Extract metadata
            String eventIdStr = paymentIntent.getMetadata().get("eventId");
            String seatIdsStr = paymentIntent.getMetadata().get("seatIds");
            
            if (eventIdStr == null || seatIdsStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message("Payment metadata is incomplete")
                                .build());
            }

            UUID eventId = UUID.fromString(eventIdStr);
            
            // Parse seat IDs
            List<UUID> seatIds = parseSeatIds(seatIdsStr);

            // Try to find existing ticket created by webhook
            TicketResponse ticket = ticketService.findTicketByUserEventAndSeats(userId, eventId, seatIds);

            if (ticket != null) {
                logger.info("Ticket found for payment confirmation: {}", ticket.getId());
                return ResponseEntity.ok(ticket);
            }

            // Ticket not created yet - webhook might be processing
            // Return processing status (client should poll or wait for webhook)
            logger.info("Payment succeeded but ticket not yet created. Webhook may be processing.");
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.ACCEPTED.value())
                            .error("Processing")
                            .message("Payment successful. Ticket is being processed. Please wait a moment and refresh.")
                            .build());

        } catch (Exception e) {
            logger.error("Error confirming payment", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .error("Internal Server Error")
                            .message("Failed to confirm payment: " + e.getMessage())
                            .build());
        }
    }

    private List<UUID> parseSeatIds(String seatIdsStr) {
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
                seatIds.add(UUID.fromString(trimmed));
            }
        }
        return seatIds;
    }

    @GetMapping("/status/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String paymentIntentId) {

        logger.info("Checking payment status for: {}", paymentIntentId);

        com.stripe.model.PaymentIntent paymentIntent =
                stripeService.getPaymentIntent(paymentIntentId);

        PaymentStatusResponse response = PaymentStatusResponse.builder()
                .paymentIntentId(paymentIntentId)
                .status(paymentIntent.getStatus())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> cancelPayment(
            @PathVariable String paymentIntentId) {

        logger.info("Cancelling payment: {}", paymentIntentId);
        stripeService.cancelPaymentIntent(paymentIntentId);

        return ResponseEntity.ok("Payment cancelled successfully");
    }

    private UUID extractUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return UUID.fromString(userIdStr);
    }
}
