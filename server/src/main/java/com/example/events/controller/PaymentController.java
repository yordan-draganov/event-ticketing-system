package com.example.events.controller;

import com.example.events.DTO.ErrorResponse;
import com.example.events.DTO.PaymentStatusResponse;
import com.example.events.DTO.PaymentResponse;
import com.example.events.DTO.PaymentDTO;
import com.example.events.DTO.PaymentConfirmDTO;
import com.example.events.DTO.TicketCreateDTO;
import com.example.events.DTO.TicketResponse;
import com.example.events.exception.UnauthorizedException;
import com.example.events.exception.ValidationException;
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
            if (!stripeService.isPaymentSuccessful(request.getPaymentIntentId())) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.PAYMENT_REQUIRED.value())
                                .error("Payment Required")
                                .message("Payment not successful")
                                .build());
            }

            com.stripe.model.PaymentIntent paymentIntent =
                    stripeService.getPaymentIntent(request.getPaymentIntentId());

            String metadataUserId = paymentIntent.getMetadata().get("userId");
            if (!userId.toString().equals(metadataUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message("This payment does not belong to you")
                                .build());
            }

            String seatIdsStr = paymentIntent.getMetadata().get("seatIds");
            String eventIdStr = paymentIntent.getMetadata().get("eventId");

            seatIdsStr = seatIdsStr.substring(1, seatIdsStr.length() - 1);
            String[] seatIdArray = seatIdsStr.split(",");
            List<UUID> seatIds = new ArrayList<>();
            for (String seatId : seatIdArray) {
                seatIds.add(UUID.fromString(seatId.trim()));
            }

            TicketCreateDTO ticketRequest = new TicketCreateDTO();
            ticketRequest.setEventId(UUID.fromString(eventIdStr));
            ticketRequest.setSeatIds(seatIds);

            TicketResponse ticket = ticketService.createTicket(ticketRequest, userId);

            logger.info("Ticket created successfully after payment confirmation: {}", ticket.getId());
            return ResponseEntity.ok(ticket);

        } catch (ValidationException e) {
            logger.warn("Seat reservation failed after payment: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.CONFLICT.value())
                            .error("Conflict")
                            .message("Seats no longer available. Refund will be processed automatically.")
                            .build());

        } catch (Exception e) {
            logger.error("Error confirming payment", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .error("Internal Server Error")
                            .message("Failed to process ticket: " + e.getMessage())
                            .build());
        }
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
