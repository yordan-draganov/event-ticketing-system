package com.example.events.controller;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management operations")
public class EventController {

    private final EventService eventService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create event", description = "Create a new event with sections and seats")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateDTO eventDTO) {
        EventResponse createdEvent = eventService.createEvent(eventDTO);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve event details by UUID")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all events", description = "Retrieve list of all available events")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all events for admin", description = "Retrieve list of all events, including hidden events")
    public ResponseEntity<List<EventResponse>> getAllEventsForAdmin() {
        List<EventResponse> events = eventService.getAllEventsForAdmin();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update event", description = "Update existing event information")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventCreateDTO eventDTO) {
        EventResponse updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete event", description = "Remove event from system")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set event hidden state", description = "Hide or show an event in public listings")
    public ResponseEntity<EventResponse> setEventHidden(@PathVariable UUID id, @RequestParam boolean hidden) {
        EventResponse event = eventService.setEventHidden(id, hidden);
        return ResponseEntity.ok(event);
    }
}