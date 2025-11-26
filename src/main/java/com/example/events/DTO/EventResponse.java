package com.example.events.DTO;

import com.example.events.model.EventCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private LocalDate date;
    private String location;
    private String description;
    private String longDescription;
    private BigDecimal price;
    private EventCategory category;
    private String image;
    private String organizer;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer availableTickets;
    private Integer totalTickets;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isFinished;
}
