package com.example.events.DTO;

import com.example.events.model.TicketStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "string", format = "time", example = "11:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;
    @Schema(type = "string", format = "time", example = "12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;
    private String eventImage;
    private UUID sectionId;
    private String sectionName;
    private Integer seatCount;
    private BigDecimal totalPrice;
    private TicketStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;

    private Boolean emailSent;
    private Integer emailAttempts;
    private String lastEmailError;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkedInAt;
}