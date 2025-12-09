package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.mapper.EventMapper;
import com.example.events.model.Event;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventResponse createEvent(EventCreateDTO eventDTO) {
        validateEventTimes(eventDTO.getStartTime(), eventDTO.getEndTime());

        Event event = eventMapper.toEntity(eventDTO);
        event.setAvailableTickets(eventDTO.getTotalTickets());
        event.setIsFinished(false);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponseDTO(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID id) {
        Event event = findEventById(id);
        return eventMapper.toResponseDTO(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(UUID id, EventCreateDTO eventDTO) {
        Event event = findEventById(id);
        validateEventTimes(eventDTO.getStartTime(), eventDTO.getEndTime());

        eventMapper.updateEntityFromDTO(eventDTO, event);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponseDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    private Event findEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    private void validateEventTimes(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new ValidationException("End time must be after start time");
        }
    }
}