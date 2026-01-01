package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.DTO.SectionRequestDTO;
import com.example.events.mapper.EventMapper;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.model.Section;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventResponse createEvent(EventCreateDTO eventDTO) {
        validateEventTimes(eventDTO.getStartTime(), eventDTO.getEndTime());

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDate(eventDTO.getDate());
        event.setLocation(eventDTO.getLocation());
        event.setDescription(eventDTO.getDescription());
        event.setLongDescription(eventDTO.getLongDescription());
        event.setCategory(eventDTO.getCategory());
        event.setImage(eventDTO.getImage());
        event.setOrganizer(eventDTO.getOrganizer());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLatitude(eventDTO.getLatitude());
        event.setLongitude(eventDTO.getLongitude());
        event.setIsFinished(false);

        Event savedEvent = eventRepository.save(event);

        for (SectionRequestDTO sectionReq : eventDTO.getSections()) {
            Section section = Section.builder()
                    .event(savedEvent)
                    .name(sectionReq.getName())
                    .price(sectionReq.getPrice())
                    .rowsCount(sectionReq.getRows())
                    .colsCount(sectionReq.getCols())
                    .build();

            Section savedSection = sectionRepository.save(section);

            for (int r = 0; r < sectionReq.getRows(); r++) {
                String rowLabel = String.valueOf((char) ('A' + r));
                for (int c = 1; c <= sectionReq.getCols(); c++) {
                    Seat seat = Seat.builder()
                            .event(savedEvent)
                            .section(savedSection)
                            .rowLabel(rowLabel)
                            .seatNumber(c)
                            .isAvailable(true)
                            .build();
                    seatRepository.save(seat);
                }
            }
        }

        return eventMapper.toResponseDTO(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID id) {
        Event event = findEventById(id);
        return buildEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::buildEventResponse)
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

    private EventResponse buildEventResponse(Event event) {
        EventResponse response = eventMapper.toResponseDTO(event);

        List<Section> sections = sectionRepository.findByEventIdOrderByNameAsc(event.getId());

        if (!sections.isEmpty()) {
            response.setMinPrice(sections.stream()
                    .map(Section::getPrice)
                    .min(java.math.BigDecimal::compareTo)
                    .orElse(java.math.BigDecimal.ZERO));

            response.setMaxPrice(sections.stream()
                    .map(Section::getPrice)
                    .max(java.math.BigDecimal::compareTo)
                    .orElse(java.math.BigDecimal.ZERO));

            response.setTotalSeats(sections.stream()
                    .mapToInt(s -> s.getRowsCount() * s.getColsCount())
                    .sum());

            response.setAvailableSeats((int) seatRepository.countAvailableByEventId(event.getId()));

            response.setSectionCount(sections.size());
        } else {
            response.setMinPrice(java.math.BigDecimal.ZERO);
            response.setMaxPrice(java.math.BigDecimal.ZERO);
            response.setTotalSeats(0);
            response.setAvailableSeats(0);
            response.setSectionCount(0);
        }

        return response;
    }
}