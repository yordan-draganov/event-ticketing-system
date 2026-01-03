package com.example.events.service;

import com.example.events.DTO.QRCodeValidationResponse;
import com.example.events.DTO.TicketCreateDTO;
import com.example.events.DTO.TicketDetailResponse;
import com.example.events.DTO.TicketResponse;
import com.example.events.exception.ResourceNotFoundException;
import com.example.events.exception.ValidationException;
import com.example.events.mapper.TicketMapper;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.model.Section;
import com.example.events.model.Ticket;
import com.example.events.model.TicketStatus;
import com.example.events.model.User;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.repository.TicketRepository;
import com.example.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;

    @Transactional
    public TicketResponse createTicket(TicketCreateDTO request, UUID userId) {
        logger.info("Creating ticket for user {} and event {} with {} seats", userId, request.getEventId(), request.getSeatIds().size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        if (event.getIsFinished()) {
            throw new ValidationException("Cannot purchase tickets for a finished event");
        }

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new ValidationException("At least one seat must be selected");
        }

        List<Seat> seats = seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ValidationException("Some of the requested seats were not found or do not belong to this event");
        }

        List<Seat> unavailableSeats = seats.stream()
                .filter(seat -> !seat.getIsAvailable() || seat.getTicket() != null)
                .collect(Collectors.toList());

        if (!unavailableSeats.isEmpty()) {
            throw new ValidationException("Some of the requested seats are not available");
        }

        Set<UUID> sectionIds = seats.stream()
                .map(seat -> seat.getSection().getId())
                .collect(Collectors.toSet());

        if (sectionIds.size() != 1) {
            throw new ValidationException("All seats must be from the same section");
        }

        Section section = seats.get(0).getSection();
        BigDecimal totalPrice = section.getPrice().multiply(BigDecimal.valueOf(seats.size()));

        Ticket ticket = Ticket.builder()
                .user(user)
                .event(event)
                .section(section)
                .totalPrice(totalPrice)
                .status(TicketStatus.confirmed)
                .emailSent(false)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        String seatInfo = seats.stream()
                .map(seat -> seat.getRowLabel() + "-" + seat.getSeatNumber())
                .collect(Collectors.joining(", "));

        String qrContent = qrCodeService.buildTicketQRContent(
                savedTicket.getId(),
                event.getId(),
                event.getTitle(),
                user.getName(),
                seatInfo
        );

        String qrCodeUrl = qrCodeService.generateAndSaveQRCode(savedTicket.getId(), qrContent);
        savedTicket.setQrCodeUrl(qrCodeUrl);
        savedTicket = ticketRepository.save(savedTicket);

        for (Seat seat : seats) {
            seat.setTicket(savedTicket);
            seat.setIsAvailable(false);
            seatRepository.save(seat);
        }

        logger.info("Ticket created successfully with id: {} for {} seats with QR code", savedTicket.getId(), seats.size());

        TicketResponse response = ticketMapper.toResponse(savedTicket);
        response.setSeatCount(seats.size());
        return response;
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
                .map(ticket -> {
                    TicketResponse response = ticketMapper.toResponse(ticket);
                    List<Seat> ticketSeats = seatRepository.findByTicketId(ticket.getId());
                    response.setSeatCount(ticketSeats.size());
                    return response;
                })
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

        TicketDetailResponse response = ticketMapper.toDetailResponse(ticket);
        List<Seat> seats = seatRepository.findByTicketId(ticketId);
        response.setSeatCount(seats.size());

        List<com.example.events.DTO.SeatResponse> seatResponses = seats.stream()
                .map(seat -> com.example.events.DTO.SeatResponse.builder()
                        .id(seat.getId())
                        .sectionId(seat.getSection().getId())
                        .sectionName(seat.getSection().getName())
                        .sectionPrice(seat.getSection().getPrice())
                        .rowLabel(seat.getRowLabel())
                        .seatNumber(seat.getSeatNumber())
                        .isAvailable(seat.getIsAvailable())
                        .displayLabel(seat.getRowLabel() + "-" + seat.getSeatNumber())
                        .build())
                .collect(Collectors.toList());
        response.setSeats(seatResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        logger.info("Fetching all tickets");

        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(ticket -> {
                    TicketResponse response = ticketMapper.toResponse(ticket);
                    List<Seat> ticketSeats = seatRepository.findByTicketId(ticket.getId());
                    response.setSeatCount(ticketSeats.size());
                    return response;
                })
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

        List<Seat> seats = seatRepository.findByTicketId(ticketId);
        for (Seat seat : seats) {
            seat.setTicket(null);
            seat.setIsAvailable(true);
            seatRepository.save(seat);
        }

        ticket.setStatus(TicketStatus.cancelled);
        ticketRepository.save(ticket);

        logger.info("Ticket {} cancelled successfully and {} seats released", ticketId, seats.size());
    }

    public QRCodeValidationResponse validateTicketQR(String qrContent) {
        Map<String, String> ticketData = qrCodeService.validateAndParse(qrContent);

        if (ticketData == null) {
            return QRCodeValidationResponse.builder()
                    .valid(false)
                    .message("Invalid QR code signature or corrupted data")
                    .build();
        }

        try {
            String ticketIdStr = ticketData.get("TICKET_ID");
            UUID ticketId = UUID.fromString(ticketIdStr);

            return ticketRepository.findById(ticketId)
                    .map(ticket -> {
                        if (ticket.getStatus() != TicketStatus.confirmed) {
                            return QRCodeValidationResponse.builder()
                                    .valid(false)
                                    .message("Ticket is " + ticket.getStatus())
                                    .ticketData(ticketData)
                                    .build();
                        }

                        return QRCodeValidationResponse.builder()
                                .valid(true)
                                .message("QR code verified and ticket is active")
                                .ticketData(ticketData)
                                .build();
                    })
                    .orElseGet(() -> QRCodeValidationResponse.builder()
                            .valid(false)
                            .message("Ticket ID not found in database")
                            .build());

        } catch (Exception e) {
            logger.error("Error parsing Ticket ID from QR: {}", e.getMessage());
            return QRCodeValidationResponse.builder()
                    .valid(false)
                    .message("Malformed Ticket ID in QR code")
                    .build();
        }
    }

}