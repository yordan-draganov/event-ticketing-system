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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
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
    private final TicketEmailDispatchService ticketEmailDispatchService;

    @Transactional
    public TicketResponse createTicket(TicketCreateDTO request, UUID userId) {
        return createTicket(request, userId, null);
    }

    @Transactional
    public TicketResponse createTicket(TicketCreateDTO request, UUID userId, UUID reservationId) {
        return createTicket(request, userId, reservationId, null);
    }

    @Transactional
    public TicketResponse createTicket(TicketCreateDTO request, UUID userId, UUID reservationId, String paymentIntentId) {
        logger.info("Creating ticket for user {} and event {} with {} seats", userId, request.getEventId(), request.getSeatIds().size());

        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            TicketResponse existingTicket = findTicketByPaymentIntentId(paymentIntentId);
            if (existingTicket != null) {
                logger.info("Ticket already exists for payment intent {}", paymentIntentId);
                return existingTicket;
            }
        }

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
                .filter(seat -> !canCreateTicketForSeat(seat, userId, reservationId))
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
                .paymentIntentId(paymentIntentId)
                .emailSent(false)
                .emailAttempts(0)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        String verificationToken = qrCodeService.generateCompactToken(savedTicket.getId());
        byte[] qrCodeImage = qrCodeService.generateQRCodeImage(savedTicket.getId(), verificationToken);

        for (Seat seat : seats) {
            seat.setTicket(savedTicket);
            seat.setIsAvailable(false);
            seat.clearReservation();
            seatRepository.save(seat);
        }

        logger.info("Ticket created successfully with id: {} for {} seats", savedTicket.getId(), seats.size());

        try {
            TicketDetailResponse ticketDetail = getTicketDetailForEmail(savedTicket.getId());
            runAfterCommit(() -> ticketEmailDispatchService.sendTicketConfirmationEmail(ticketDetail, qrCodeImage));
            logger.info("Ticket confirmation email queued for ticket: {}", savedTicket.getId());
        } catch (Exception e) {
            savedTicket.setEmailSent(false);
            savedTicket.setLastEmailError(e.getMessage());
            ticketRepository.save(savedTicket);
            logger.error("Failed to queue email for ticket {}: {}", savedTicket.getId(), e.getMessage());
        }

        TicketResponse response = ticketMapper.toResponse(savedTicket);
        response.setSeatCount(seats.size());
        return response;
    }

    @Transactional(readOnly = true)
    public TicketResponse findTicketByPaymentIntentId(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return null;
        }

        return ticketRepository.findByPaymentIntentId(paymentIntentId)
                .filter(ticket -> ticket.getStatus() == TicketStatus.confirmed)
                .map(ticket -> {
                    TicketResponse response = ticketMapper.toResponse(ticket);
                    response.setSeatCount(seatRepository.findByTicketId(ticket.getId()).size());
                    return response;
                })
                .orElse(null);
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
                        .isAvailable(seat.isAvailableForPurchase())
                        .displayLabel(seat.getRowLabel() + "-" + seat.getSeatNumber())
                        .build())
                .collect(Collectors.toList());
        response.setSeats(seatResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public TicketResponse findTicketByUserEventAndSeats(UUID userId, UUID eventId, List<UUID> seatIds) {
        logger.info("Finding ticket for user {}, event {}, with {} seats", userId, eventId, seatIds.size());

        List<Ticket> tickets = ticketRepository.findByUserIdAndEventId(userId, eventId);

        tickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.confirmed)
                .collect(Collectors.toList());

        for (Ticket ticket : tickets) {
            List<Seat> ticketSeats = seatRepository.findByTicketId(ticket.getId());
            Set<UUID> ticketSeatIds = ticketSeats.stream()
                    .map(Seat::getId)
                    .collect(Collectors.toSet());

            Set<UUID> requestedSeatIds = new HashSet<>(seatIds);

            if (ticketSeatIds.size() == requestedSeatIds.size() && 
                ticketSeatIds.containsAll(requestedSeatIds)) {
                TicketResponse response = ticketMapper.toResponse(ticket);
                response.setSeatCount(ticketSeats.size());
                logger.info("Found matching ticket: {}", ticket.getId());
                return response;
            }
        }

        logger.info("No matching ticket found for user {}, event {}, seats {}", userId, eventId, seatIds);
        return null;
    }

    @Transactional(readOnly = true)
    private TicketDetailResponse getTicketDetailForEmail(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

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
                        .isAvailable(seat.isAvailableForPurchase())
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

        try {
            String userEmail = ticket.getUser().getEmail();
            String userName = ticket.getUser().getName();
            String eventTitle = ticket.getEvent().getTitle();
            String ticketIdText = ticket.getId().toString();
            runAfterCommit(() -> ticketEmailDispatchService.sendTicketCancellationEmail(
                    userEmail,
                    userName,
                    eventTitle,
                    ticketIdText
            ));
            logger.info("Cancellation email queued for ticket: {}", ticketId);
        } catch (Exception e) {
            logger.error("Failed to queue cancellation email for ticket {}: {}", ticketId, e.getMessage());
        }

        logger.info("Ticket {} cancelled successfully and {} seats released", ticketId, seats.size());
    }

    @Transactional
    public QRCodeValidationResponse validateTicketByUrl(UUID ticketId, String token) {
        logger.info("Validating ticket {} with URL token", ticketId);

        if (!qrCodeService.verifyTicketToken(ticketId, token)) {
            return QRCodeValidationResponse.builder()
                    .valid(false)
                    .message("Invalid verification token")
                    .build();
        }

        return ticketRepository.findByIdForUpdate(ticketId)
                .map(ticket -> {
                    Map<String, String> ticketData = new HashMap<>();
                    ticketData.put("TICKET_ID", ticket.getId().toString());
                    ticketData.put("EVENT_ID", ticket.getEvent().getId().toString());
                    ticketData.put("EVENT", ticket.getEvent().getTitle());
                    ticketData.put("USER", ticket.getUser().getName());
                    if (ticket.getCheckedInAt() != null) {
                        ticketData.put("CHECKED_IN_AT", ticket.getCheckedInAt().toString());
                    }

                    List<Seat> seats = seatRepository.findByTicketId(ticketId);
                    String seatInfo = seats.stream()
                            .map(seat -> seat.getRowLabel() + "-" + seat.getSeatNumber())
                            .collect(Collectors.joining(", "));
                    ticketData.put("SEATS", seatInfo);

                    if (ticket.getStatus() != TicketStatus.confirmed) {
                        return QRCodeValidationResponse.builder()
                                .valid(false)
                                .message("Ticket is " + ticket.getStatus())
                                .ticketData(ticketData)
                                .build();
                    }

                    if (ticket.getCheckedInAt() != null) {
                        return QRCodeValidationResponse.builder()
                                .valid(false)
                                .message("Ticket has already been used")
                                .ticketData(ticketData)
                                .build();
                    }

                    LocalDateTime checkedInAt = LocalDateTime.now();
                    ticket.setCheckedInAt(checkedInAt);
                    ticketRepository.save(ticket);
                    ticketData.put("CHECKED_IN_AT", checkedInAt.toString());

                    return QRCodeValidationResponse.builder()
                            .valid(true)
                            .message("QR code verified and ticket checked in")
                            .ticketData(ticketData)
                            .build();
                })
                .orElseGet(() -> QRCodeValidationResponse.builder()
                        .valid(false)
                        .message("Ticket not found")
                        .build());
    }

    private boolean canCreateTicketForSeat(Seat seat, UUID userId, UUID reservationId) {
        if (reservationId == null) {
            return seat.isAvailableForPurchase();
        }

        return seat.getTicket() == null
                && seat.hasReservationFor(userId, reservationId);
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

}