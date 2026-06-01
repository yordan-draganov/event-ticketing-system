package com.example.events.controller;

import com.example.events.DTO.QRCodeValidationResponse;
import com.example.events.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class VerificationControllerTest {

    @Mock
    private TicketService ticketService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VerificationController(ticketService))
                .build();
    }

    @Test
    void verifyEndpointShouldRenderTicketVerificationResult() throws Exception {
        UUID ticketId = UUID.randomUUID();
        String token = "valid-token";
        Map<String, String> ticketData = Map.of(
                "TICKET_ID", ticketId.toString(),
                "EVENT", "Test Event",
                "SEATS", "A-7"
        );

        when(ticketService.validateTicketByUrl(ticketId, token))
                .thenReturn(QRCodeValidationResponse.builder()
                        .valid(true)
                        .message("QR code verified and ticket checked in")
                        .ticketData(ticketData)
                        .build());

        mockMvc.perform(get("/verify/{ticketId}", ticketId).param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-verification"))
                .andExpect(model().attribute("valid", true))
                .andExpect(model().attribute("message", "QR code verified and ticket checked in"))
                .andExpect(model().attribute("ticketData", ticketData));

        verify(ticketService).validateTicketByUrl(ticketId, token);
    }
}
