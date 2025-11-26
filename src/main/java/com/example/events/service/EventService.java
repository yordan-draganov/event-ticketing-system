package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.model.Event;
import com.example.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse createEvent(EventCreateDTO eventDTO) {
        Event event = mapToEntity(eventDTO);
        event.setAvailableTickets(eventDTO.getTotalTickets());
        event.setIsFinished(false);

        Event savedEvent = eventRepository.save(event);
        return mapToResponseDTO(savedEvent);
    }


    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    private Event mapToEntity(EventCreateDTO dto) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDate(dto.getDate());
        event.setLocation(dto.getLocation());
        event.setDescription(dto.getDescription());
        event.setLongDescription(dto.getLongDescription());
        event.setPrice(dto.getPrice());
        event.setCategory(dto.getCategory());
        event.setImage(dto.getImage());
        event.setOrganizer(dto.getOrganizer());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setTotalTickets(dto.getTotalTickets());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        return event;
    }

    private EventResponse mapToResponseDTO(Event event) {
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDate(event.getDate());
        dto.setLocation(event.getLocation());
        dto.setDescription(event.getDescription());
        dto.setLongDescription(event.getLongDescription());
        dto.setPrice(event.getPrice());
        dto.setCategory(event.getCategory());
        dto.setImage(event.getImage());
        dto.setOrganizer(event.getOrganizer());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setAvailableTickets(event.getAvailableTickets());
        dto.setTotalTickets(event.getTotalTickets());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setIsFinished(event.getIsFinished());
        return dto;
    }
}