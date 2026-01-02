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
public class SeatResponse {
    private UUID id;
    private UUID sectionId;
    private String sectionName;
    private BigDecimal sectionPrice;
    private String rowLabel;
    private Integer seatNumber;
    private Boolean isAvailable;
    private String displayLabel;
}