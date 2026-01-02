package com.example.events.service;

import com.example.events.DTO.SectionResponse;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.mapper.SectionMapper;
import com.example.events.model.Section;
import com.example.events.repository.SectionRepository;
import com.example.events.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final SectionMapper sectionMapper;

    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsByEventId(UUID eventId) {
        return sectionRepository.findByEventIdOrderByNameAsc(eventId)
                .stream()
                .map(this::toSectionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectionResponse getSectionById(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));
        return toSectionResponse(section);
    }

    private SectionResponse toSectionResponse(Section section) {
        SectionResponse response = sectionMapper.toResponse(section);
        response.setAvailableSeats((int) seatRepository.countAvailableBySectionId(section.getId()));
        return response;
    }
}