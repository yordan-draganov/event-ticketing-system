package com.example.events.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmDTO {

    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId;
}