package com.example.events.DTO;

import com.example.events.model.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String title;
    private LocalDate date;
    private String location;
    private String description;
    private String longDescription;
    private EventCategory category;
    private String image;
    private String organizer;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isFinished;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer sectionCount;
}