package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.UnauthorizedException;
import com.example.events.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management and retrieval endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create ticket", description = "Create ticket directly (bypassing payment)")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateDTO request,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        TicketResponse ticket = ticketService.createTicket(request, userId);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my tickets", description = "Retrieve all tickets for authenticated user")
    public ResponseEntity<List<TicketResponse>> getMyTickets(HttpServletRequest httpRequest) {
        UUID userId = extractUserId(httpRequest);
        List<TicketResponse> tickets = ticketService.getMyTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get ticket details", description = "Retrieve detailed ticket information including QR code")
    public ResponseEntity<TicketDetailResponse> getTicketById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        TicketDetailResponse ticket = ticketService.getTicketById(id, userId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tickets", description = "Retrieve all tickets in system (Admin only)")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel ticket", description = "Delete/cancel user's ticket")
    public ResponseEntity<String> deleteTicket(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = extractUserId(httpRequest);
        ticketService.deleteTicket(id, userId);
        return ResponseEntity.ok("Ticket cancelled successfully");
    }

    private UUID extractUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return UUID.fromString(userIdStr);
    }
}