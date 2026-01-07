package com.example.events.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotEmpty(message = "At least one seat must be selected")
    @NotNull(message = "Seat IDs are required")
    private List<UUID> seatIds;
}