package com.example.events.controller;

import com.example.events.DTO.SeatResponse;
import com.example.events.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByEvent(@PathVariable UUID eventId) {
        List<SeatResponse> seats = seatService.getSeatsByEventId(eventId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<SeatResponse>> getSeatsBySection(@PathVariable UUID sectionId) {
        List<SeatResponse> seats = seatService.getSeatsBySectionId(sectionId);
        return ResponseEntity.ok(seats);
    }
}