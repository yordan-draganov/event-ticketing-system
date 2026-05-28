package com.example.events.service;

import com.example.events.DTO.TicketDetailResponse;
import com.example.events.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketEmailDispatchService {

    private static final Logger logger = LoggerFactory.getLogger(TicketEmailDispatchService.class);

    private final EmailService emailService;
    private final TicketRepository ticketRepository;

    @Value("${tickets.email.max-attempts:3}")
    private int maxAttempts;

    @Value("${tickets.email.retry-delay-ms:2000}")
    private long retryDelayMs;

    @Async
    public void sendTicketConfirmationEmail(TicketDetailResponse ticketDetail, byte[] qrCodeImage) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                emailService.sendTicketConfirmationEmail(ticketDetail, qrCodeImage);
                markEmailSent(ticketDetail, attempt);
                logger.info("Ticket confirmation email sent for ticket {} on attempt {}",
                        ticketDetail.getId(), attempt);
                return;
            } catch (Exception e) {
                lastFailure = e;
                markEmailFailed(ticketDetail, attempt, e);
                logger.warn("Ticket confirmation email attempt {} failed for ticket {}: {}",
                        attempt, ticketDetail.getId(), e.getMessage());

                if (attempt < maxAttempts) {
                    waitBeforeRetry();
                }
            }
        }

        logger.error("Ticket confirmation email permanently failed for ticket {} after {} attempts",
                ticketDetail.getId(), maxAttempts, lastFailure);
    }

    @Async
    public void sendTicketCancellationEmail(String userEmail, String userName, String eventTitle, String ticketId) {
        try {
            emailService.sendTicketCancellationEmail(userEmail, userName, eventTitle, ticketId);
            logger.info("Ticket cancellation email sent for ticket {}", ticketId);
        } catch (Exception e) {
            logger.warn("Ticket cancellation email failed for ticket {}: {}", ticketId, e.getMessage());
        }
    }

    private void markEmailSent(TicketDetailResponse ticketDetail, int attempt) {
        ticketRepository.findById(ticketDetail.getId()).ifPresent(ticket -> {
            ticket.setEmailSent(true);
            ticket.setEmailAttempts(attempt);
            ticket.setLastEmailError(null);
            ticketRepository.save(ticket);
        });
    }

    private void markEmailFailed(TicketDetailResponse ticketDetail, int attempt, Exception e) {
        ticketRepository.findById(ticketDetail.getId()).ifPresent(ticket -> {
            ticket.setEmailSent(false);
            ticket.setEmailAttempts(attempt);
            ticket.setLastEmailError(truncateError(e.getMessage()));
            ticketRepository.save(ticket);
        });
    }

    private String truncateError(String message) {
        if (message == null) {
            return "Unknown email delivery error";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}