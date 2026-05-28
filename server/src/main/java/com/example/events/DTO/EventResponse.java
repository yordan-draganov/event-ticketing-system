package com.example.events.DTO;

import com.example.events.model.EventCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "string", format = "time", example = "11:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;
    @Schema(type = "string", format = "time", example = "12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;
    private Boolean isFinished;
    private Boolean isHidden;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer sectionCount;
}