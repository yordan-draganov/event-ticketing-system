package com.example.events.DTO;

import com.example.events.model.EventCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class EventCreateDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate date;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;
    private String longDescription;

    @NotNull(message = "Category is required")
    private EventCategory category;

    private String image;
    private String organizer;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "At least one section is required")
    @Size(min = 1, message = "Event must have at least one section")
    @Valid
    private List<SectionRequestDTO> sections;
}