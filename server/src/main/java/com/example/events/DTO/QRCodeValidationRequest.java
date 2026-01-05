package com.example.events.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeValidationRequest {
    @NotBlank(message = "QR code content is required")
    private String qrContent;
}

