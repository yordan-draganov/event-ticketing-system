package com.example.events.service;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.DTO.SectionRequestDTO;
import com.example.events.mapper.EventMapper;
import com.example.events.mapper.SectionMapper;
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

import java.util.ArrayList;
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
    private final SectionMapper sectionMapper;

    @Transactional
    public EventResponse createEvent(EventCreateDTO eventDTO) {
        validateEventTimes(eventDTO.getStartTime(), eventDTO.getEndTime());

        Event event = eventMapper.toEntity(eventDTO);
        event.setIsFinished(false);

        Event savedEvent = eventRepository.save(event);

        createSectionsAndSeats(savedEvent, eventDTO.getSections());

        return buildEventResponse(savedEvent);
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

        long soldSeats = seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(id)
                .stream()
                .filter(seat -> !seat.getIsAvailable())
                .count();

        if (soldSeats > 0) {
            throw new ValidationException("Cannot modify sections with sold tickets " + soldSeats + " seats have already been purchased.");
        }

        List<Section> existingSections = sectionRepository.findByEventIdOrderByNameAsc(id);
        sectionRepository.deleteAll(existingSections);

        createSectionsAndSeats(updatedEvent, eventDTO.getSections());

        return buildEventResponse(updatedEvent);
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

    private void createSectionsAndSeats(Event event, List<SectionRequestDTO> sectionRequests) {
        List<Seat> allSeats = new ArrayList<>();
        
        for (SectionRequestDTO sectionReq : sectionRequests) {
            Section section = sectionMapper.toEntity(sectionReq, event);

            Section savedSection = sectionRepository.save(section);

            for (int r = 0; r < sectionReq.getRows(); r++) {
                String rowLabel = String.valueOf((char) ('A' + r));
                for (int c = 1; c <= sectionReq.getCols(); c++) {
                    Seat seat = Seat.builder()
                            .event(event)
                            .section(savedSection)
                            .rowLabel(rowLabel)
                            .seatNumber(c)
                            .isAvailable(true)
                            .build();
                    allSeats.add(seat);
                }
            }
        }
        
        if (!allSeats.isEmpty()) {
            seatRepository.saveAll(allSeats);
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