package com.example.events.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponse {
    private EventResponse event;
    private List<SectionResponse> sections;
    private List<SeatResponse> selectedSeats;
    private SectionResponse selectedSection;
    private BigDecimal totalPrice;
    private PaymentResponse payment;
}