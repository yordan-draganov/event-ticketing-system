package com.example.events.controller;

import com.example.events.DTO.*;
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
    public ResponseEntity<TicketResponse> confirmPayment(
            @Valid @RequestBody PaymentConfirmDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);

        logger.info("Confirming payment {} for user: {}", request.getPaymentIntentId(), userId);

        if (!stripeService.isPaymentSuccessful(request.getPaymentIntentId())) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(null);
        }

        com.stripe.model.PaymentIntent paymentIntent = stripeService.getPaymentIntent(request.getPaymentIntentId());

        String seatIdsStr = paymentIntent.getMetadata().get("seatIds");
        String eventIdStr = paymentIntent.getMetadata().get("eventId");

        seatIdsStr = seatIdsStr.substring(1, seatIdsStr.length() - 1);
        String[] seatIdArray = seatIdsStr.split(",");
        java.util.List<UUID> seatIds = new java.util.ArrayList<>();
        for (String seatId : seatIdArray) {
            seatIds.add(UUID.fromString(seatId.trim()));
        }

        TicketCreateDTO ticketRequest = new TicketCreateDTO();
        ticketRequest.setEventId(UUID.fromString(eventIdStr));
        ticketRequest.setSeatIds(seatIds);

        TicketResponse ticket = ticketService.createTicket(ticketRequest, userId);

        logger.info("Ticket created successfully after payment confirmation: {}", ticket.getId());

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/status/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String paymentIntentId) {

        logger.info("Checking payment status for: {}", paymentIntentId);

        com.stripe.model.PaymentIntent paymentIntent = stripeService.getPaymentIntent(paymentIntentId);

        return ResponseEntity.ok(paymentIntent.getStatus());
    }

    private UUID extractUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return UUID.fromString(userIdStr);
    }
}