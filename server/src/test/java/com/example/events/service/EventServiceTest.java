package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.SectionRequestDTO;
import com.example.events.exception.ValidationException;
import com.example.events.mapper.EventMapper;
import com.example.events.mapper.SectionMapper;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.repository.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private SectionMapper sectionMapper;

    @Test
    void updateEventShouldFailBeforePersistingWhenSeatsAreAlreadySold() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Original");

        EventCreateDTO request = new EventCreateDTO();
        request.setTitle("Updated");
        request.setDate(LocalDate.now().plusDays(10));
        request.setLocation("Arena");
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(20, 0));
        request.setSections(List.of(SectionRequestDTO.builder()
                .name("VIP")
                .price(BigDecimal.TEN)
                .rows(2)
                .cols(2)
                .build()));

        Seat soldSeat = Seat.builder()
                .isAvailable(false)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId)).thenReturn(List.of(soldSeat));

        assertThrows(ValidationException.class, () -> eventService.updateEvent(eventId, request));

        verify(eventMapper, never()).updateEntityFromDTO(request, event);
        verify(eventRepository, never()).save(event);
        verify(sectionRepository, never()).deleteAll();
    }
}
