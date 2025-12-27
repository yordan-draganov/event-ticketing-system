package com.example.events.service;

import com.example.events.DTO.TicketCreateDTO;
import com.example.events.DTO.TicketDetailResponse;
import com.example.events.DTO.TicketResponse;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.mapper.TicketMapper;
import com.example.events.model.Event;
import com.example.events.model.Ticket;
import com.example.events.model.TicketStatus;
import com.example.events.model.User;
import com.example.events.repository.EventRepository;
import com.example.events.repository.TicketRepository;
import com.example.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Transactional
    public TicketResponse createTicket(TicketCreateDTO request, UUID userId) {
        logger.info("Creating ticket for user {} and event {}", userId, request.getEventId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        if (event.getIsFinished()) {
            throw new ValidationException("Cannot purchase tickets for a finished event");
        }

        if (event.getAvailableTickets() < request.getQuantity()) {
            throw new ValidationException(
                    String.format("Not enough tickets available. Requested: %d, Available: %d",
                            request.getQuantity(), event.getAvailableTickets())
            );
        }

        BigDecimal pricePerTicket = event.getPrice();
        BigDecimal totalPrice = pricePerTicket.multiply(BigDecimal.valueOf(request.getQuantity()));

        Ticket ticket = Ticket.builder()
                .user(user)
                .event(event)
                .quantity(request.getQuantity())
                .pricePerTicket(pricePerTicket)
                .totalPrice(totalPrice)
                .status(TicketStatus.confirmed)
                .emailSent(false)
                .build();

        event.setAvailableTickets(event.getAvailableTickets() - request.getQuantity());
        eventRepository.save(event);

        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket created successfully with id: {}", savedTicket.getId());

        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(UUID userId) {
        logger.info("Fetching tickets for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.cancelled)
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketById(UUID ticketId, UUID userId) {
        logger.info("Fetching ticket {} for user {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have permission to view this ticket");
        }

        return ticketMapper.toDetailResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        logger.info("Fetching all tickets");

        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTicket(UUID ticketId, UUID userId) {
        logger.info("Deleting ticket {} for user {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have permission to delete this ticket");
        }

        if (ticket.getStatus() == TicketStatus.cancelled || ticket.getStatus() == TicketStatus.refunded) {
            throw new ValidationException("Cannot delete a " + ticket.getStatus() + " ticket");
        }

        Event event = ticket.getEvent();
        event.setAvailableTickets(event.getAvailableTickets() + ticket.getQuantity());
        eventRepository.save(event);

        ticket.setStatus(TicketStatus.cancelled);
        ticketRepository.save(ticket);

        logger.info("Ticket {} cancelled successfully", ticketId);
    }

}