package com.example.events.service;

import com.example.events.DTO.SeatResponse;
import com.example.events.mapper.SeatMapper;
import com.example.events.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByEventId(UUID eventId) {
        return seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId)
                .stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsBySectionId(UUID sectionId) {
        return seatRepository.findBySectionIdOrderByRowLabelAscSeatNumberAsc(sectionId)
                .stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }
}