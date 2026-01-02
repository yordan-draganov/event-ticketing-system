package com.example.events.controller;

import com.example.events.DTO.SectionResponse;
import com.example.events.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<SectionResponse>> getSectionsByEvent(@PathVariable UUID eventId) {
        List<SectionResponse> sections = sectionService.getSectionsByEventId(eventId);
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable UUID sectionId) {
        SectionResponse section = sectionService.getSectionById(sectionId);
        return ResponseEntity.ok(section);
    }
}