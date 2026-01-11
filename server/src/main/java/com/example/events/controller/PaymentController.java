package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.UnauthorizedException;
import com.example.events.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Payments", description = "Stripe payment processing endpoints")
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final StripeService stripeService;

    @PostMapping("/create-intent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create payment intent", description = "Initialize Stripe payment for selected seats")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        logger.info("Creating payment intent for user: {}", userId);

        return ResponseEntity.ok(
                stripeService.createPaymentIntent(
                        request.getEventId(),
                        request.getSeatIds(),
                        userId
                )
        );
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm payment", description = "Verify payment success and retrieve/create ticket")
    public ResponseEntity<?> confirmPayment(
            @Valid @RequestBody PaymentConfirmDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        logger.info("Confirming payment {} for user {}", request.getPaymentIntentId(), userId);

        return stripeService.confirmPayment(
                request.getPaymentIntentId(),
                userId
        );
    }

    @GetMapping("/status/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment status", description = "Check current status of payment intent")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String paymentIntentId) {

        logger.info("Checking payment status for {}", paymentIntentId);
        return ResponseEntity.ok(
                stripeService.getPaymentStatus(paymentIntentId)
        );
    }

    @PostMapping("/cancel/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel payment", description = "Cancel pending payment intent")
    public ResponseEntity<String> cancelPayment(
            @PathVariable String paymentIntentId) {

        logger.info("Cancelling payment {}", paymentIntentId);
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
