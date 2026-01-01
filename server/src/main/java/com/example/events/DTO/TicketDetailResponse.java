package com.example.events.DTO;

import com.example.events.model.EventCategory;
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
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailResponse {
    private UUID id;
    private UUID sectionId;
    private String sectionName;
    private Integer seatCount;
    private BigDecimal totalPrice;
    private TicketStatus status;
    private List<SeatResponse> seats;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;

    private String qrCodeUrl;
    private Boolean emailSent;

    private UUID userId;
    private String userName;
    private String userEmail;

    private UUID eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private String eventLocation;
    private String eventDescription;
    private String eventLongDescription;
    private EventCategory eventCategory;
    private String eventImage;
    private String eventOrganizer;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
}