package com.example.events.controller;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.model.EventCategory;
import com.example.events.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void testCreateEvent() throws Exception {
        EventCreateDTO eventDTO = createSampleEventDTO();

        MvcResult result = mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Spring Boot Conference"))
                .andExpect(jsonPath("$.location").value("Tech Center"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.category").value("Technology"))
                .andExpect(jsonPath("$.totalTickets").value(100))
                .andExpect(jsonPath("$.availableTickets").value(100))
                .andExpect(jsonPath("$.isFinished").value(false))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        EventResponse response = objectMapper.readValue(responseBody, EventResponse.class);

        assertNotNull(response.getId());
        assertEquals(1, eventRepository.count());
    }

    @Test
    void testGetEventById() throws Exception {
        EventCreateDTO eventDTO = createSampleEventDTO();

        MvcResult createResult = mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        EventResponse createdEvent = objectMapper.readValue(createResponseBody, EventResponse.class);
        UUID eventId = createdEvent.getId();

        mockMvc.perform(get("/api/events/get/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Spring Boot Conference"))
                .andExpect(jsonPath("$.location").value("Tech Center"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.category").value("Technology"))
                .andExpect(jsonPath("$.totalTickets").value(100))
                .andExpect(jsonPath("$.availableTickets").value(100));
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/events/get/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found with id: " + randomId));
    }

    @Test
    void testDeleteEvent() throws Exception {
        EventCreateDTO eventDTO = createSampleEventDTO();

        MvcResult createResult = mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        EventResponse createdEvent = objectMapper.readValue(createResponseBody, EventResponse.class);
        UUID eventId = createdEvent.getId();

        assertEquals(1, eventRepository.count());

        mockMvc.perform(delete("/api/events/delete/" + eventId))
                .andExpect(status().isNoContent());

        assertEquals(0, eventRepository.count());
    }

    @Test
    void testDeleteEvent_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/api/events/delete/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found with id: " + randomId));
    }

    @Test
    void testCreateEvent_ValidationError() throws Exception {
        EventCreateDTO eventDTO = createSampleEventDTO();
        eventDTO.setTitle("");

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    private EventCreateDTO createSampleEventDTO() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Conference");
        dto.setDate(LocalDate.now().plusDays(30));
        dto.setLocation("Tech Center");
        dto.setDescription("A great tech event");
        dto.setLongDescription("An amazing conference about Spring Boot and microservices");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setCategory(EventCategory.Technology);
        dto.setImage("https://example.com/image.jpg");
        dto.setOrganizer("Tech Events Inc");
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));
        dto.setTotalTickets(100);
        dto.setLatitude(new BigDecimal("40.7128"));
        dto.setLongitude(new BigDecimal("-74.0060"));
        return dto;
    }
}
