package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.mapper.EventMapper;
import com.example.events.model.Event;
import com.example.events.model.EventCategory;
import com.example.events.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private EventCreateDTO eventDTO;
    private Event event;
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

        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");
        event.setDate(LocalDate.now().plusDays(7));
        event.setLocation("Test Location");
        event.setDescription("Test Description");
        event.setPrice(new BigDecimal("50.00"));
        event.setCategory(EventCategory.Music);
        event.setStartTime(LocalTime.of(18, 0));
        event.setEndTime(LocalTime.of(22, 0));
        event.setTotalTickets(100);
        event.setAvailableTickets(100);
        event.setIsFinished(false);

        eventResponse = new EventResponse();
        eventResponse.setId(eventId);
        eventResponse.setTitle("Test Event");
    }

    @Test
    void createEvent_Success() {
        when(eventMapper.toEntity(eventDTO)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(eventResponse);

        EventResponse result = eventService.createEvent(eventDTO);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("Test Event", result.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_InvalidTimes_ThrowsValidationException() {
        eventDTO.setStartTime(LocalTime.of(22, 0));
        eventDTO.setEndTime(LocalTime.of(18, 0));

        assertThrows(ValidationException.class, () -> eventService.createEvent(eventDTO));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getEventById_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toResponseDTO(event)).thenReturn(eventResponse);

        EventResponse result = eventService.getEventById(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    void getEventById_NotFound_ThrowsResourceNotFoundException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(eventId));
        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    void getAllEvents_Success() {
        Event event2 = new Event();
        event2.setId(UUID.randomUUID());
        event2.setTitle("Test Event 2");

        EventResponse eventResponse2 = new EventResponse();
        eventResponse2.setId(event2.getId());
        eventResponse2.setTitle("Test Event 2");

        List<Event> events = Arrays.asList(event, event2);
        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toResponseDTO(event)).thenReturn(eventResponse);
        when(eventMapper.toResponseDTO(event2)).thenReturn(eventResponse2);

        List<EventResponse> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void updateEvent_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(eventResponse);

        EventResponse result = eventService.updateEvent(eventId, eventDTO);

        assertNotNull(result);
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventMapper, times(1)).updateEntityFromDTO(eventDTO, event);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.existsById(eventId)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(eventId);

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).existsById(eventId);
        verify(eventRepository, times(1)).deleteById(eventId);
    }
}