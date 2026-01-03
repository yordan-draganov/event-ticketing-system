package com.example.events.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeValidationResponse {
    private boolean valid;
    private String message;
    private Map<String, String> ticketData;
}

