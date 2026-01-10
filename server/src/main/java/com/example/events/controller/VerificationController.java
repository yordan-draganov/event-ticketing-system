package com.example.events.controller;

import com.example.events.DTO.QRCodeValidationResponse;
import com.example.events.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class VerificationController {

    private final TicketService ticketService;

    @GetMapping("/verify/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String verifyTicket(
            @PathVariable UUID ticketId,
            @RequestParam String token,
            Model model) {

        QRCodeValidationResponse response = ticketService.validateTicketByUrl(ticketId, token);

        model.addAttribute("valid", response.isValid());
        model.addAttribute("message", response.getMessage());
        model.addAttribute("ticketData", response.getTicketData());

        return "ticket-verification";
    }
}