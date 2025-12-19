package com.example.events.controller;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.model.EventCategory;
import com.example.events.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    private EventCreateDTO eventDTO;
    private EventResponse eventResponse;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        eventDTO = new EventCreateDTO();
        eventDTO.setTitle("Test Event");
        eventDTO.setDate(LocalDate.now().plusDays(7));
        eventDTO.setLocation("Test Location");
        eventDTO.setDescription("Test Description");
        eventDTO.setPrice(new BigDecimal("50.00"));
        eventDTO.setCategory(EventCategory.Music);
        eventDTO.setStartTime(LocalTime.of(18, 0));
        eventDTO.setEndTime(LocalTime.of(22, 0));
        eventDTO.setTotalTickets(100);

        eventResponse = new EventResponse();
        eventResponse.setId(eventId);
        eventResponse.setTitle("Test Event");
        eventResponse.setDate(LocalDate.now().plusDays(7));
        eventResponse.setLocation("Test Location");
        eventResponse.setDescription("Test Description");
        eventResponse.setPrice(new BigDecimal("50.00"));
        eventResponse.setCategory(EventCategory.Music);
        eventResponse.setStartTime(LocalTime.of(18, 0));
        eventResponse.setEndTime(LocalTime.of(22, 0));
        eventResponse.setTotalTickets(100);
        eventResponse.setAvailableTickets(100);
    }

    @Test
    void testCreateEventSuccess() {
        Mockito.when(eventService.createEvent(eventDTO)).thenReturn(eventResponse);

        ResponseEntity<EventResponse> response = eventController.createEvent(eventDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        Mockito.verify(eventService).createEvent(eventDTO);
    }

    @Test
    void testCreateEventFail() {
        Mockito.doThrow(new ValidationException("End time must be after start time"))
                .when(eventService).createEvent(Mockito.any(EventCreateDTO.class));

        assertThrows(ValidationException.class, () -> {
            eventController.createEvent(eventDTO);
        });

        Mockito.verify(eventService).createEvent(eventDTO);
    }

    @Test
    void testGetEventById() {
        Mockito.when(eventService.getEventById(eventId)).thenReturn(eventResponse);

        ResponseEntity<EventResponse> response = eventController.getEventById(eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        Mockito.verify(eventService).getEventById(eventId);
    }

    @Test
    void testGetEventByIdNotFound() {
        Mockito.doThrow(new ResourceNotFoundException("Event not found with id: " + eventId))
                .when(eventService).getEventById(eventId);

        assertThrows(ResourceNotFoundException.class, () -> {
            eventController.getEventById(eventId);
        });

        Mockito.verify(eventService).getEventById(eventId);
    }

    @Test
    void testGetAllEvents() {
        EventResponse eventResponse2 = new EventResponse();
        eventResponse2.setId(UUID.randomUUID());
        eventResponse2.setTitle("Test Event 2");

        List<EventResponse> events = Arrays.asList(eventResponse, eventResponse2);
        Mockito.when(eventService.getAllEvents()).thenReturn(events);

        ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Test Event", response.getBody().get(0).getTitle());
        assertEquals("Test Event 2", response.getBody().get(1).getTitle());
        Mockito.verify(eventService).getAllEvents();
    }

    @Test
    void testUpdateEventSuccess() {
        Mockito.when(eventService.updateEvent(eventId, eventDTO)).thenReturn(eventResponse);

        ResponseEntity<EventResponse> response = eventController.updateEvent(eventId, eventDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().getId());
        assertEquals("Test Event", response.getBody().getTitle());
        Mockito.verify(eventService).updateEvent(eventId, eventDTO);
    }

    @Test
    void testDeleteEventSuccess() {
        Mockito.doNothing().when(eventService).deleteEvent(eventId);

        ResponseEntity<Void> response = eventController.deleteEvent(eventId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(eventService).deleteEvent(eventId);
    }
}