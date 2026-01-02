package com.example.events.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionResponse {
    private UUID id;
    private UUID eventId;
    private String name;
    private BigDecimal price;
    private Integer rowsCount;
    private Integer colsCount;
    private Integer totalSeats;
    private Integer availableSeats;
}