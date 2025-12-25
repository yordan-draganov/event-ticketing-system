package com.example.events.DTO;

import com.example.events.model.TicketStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private String eventLocation;
    private LocalTime startTime;
    private LocalTime endTime;
    private String eventImage;
    private Integer quantity;
    private BigDecimal pricePerTicket;
    private BigDecimal totalPrice;
    private TicketStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;

    private String qrCodeUrl;
    private Boolean emailSent;
}