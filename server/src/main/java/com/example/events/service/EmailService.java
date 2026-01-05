package com.example.events.service;

import com.example.events.DTO.TicketDetailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${qr.code.directory}")
    private String qrCodeDirectory;

    @Value("${app.url}")
    private String appUrl;


    @Async
    public void sendTicketConfirmationEmail(TicketDetailResponse ticket) {
        try {
            logger.info("Preparing to send ticket confirmation email to: {}", ticket.getUserEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getUserEmail());
            helper.setSubject("Your Ticket for " + ticket.getEventTitle());

            String emailContent = buildEmailContent(ticket);
            helper.setText(emailContent, true);

            if (ticket.getQrCodeUrl() != null && !ticket.getQrCodeUrl().isEmpty()) {
                String qrCodePath = ticket.getQrCodeUrl().replace("/qr-codes/", "");
                File qrCodeFile = Paths.get(qrCodeDirectory, qrCodePath).toFile();

                if (qrCodeFile.exists()) {
                    FileSystemResource qrCode = new FileSystemResource(qrCodeFile);
                    helper.addInline("qrCode", qrCode);
                    logger.info("QR code attached to email");
                } else {
                    logger.warn("QR code file not found: {}", qrCodeFile.getAbsolutePath());
                }
            }

            mailSender.send(message);
            logger.info("Ticket confirmation email sent successfully to: {}", ticket.getUserEmail());

        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", ticket.getUserEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailContent(TicketDetailResponse ticket) {
        Context context = new Context();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = ticket.getEventDate().format(dateFormatter);
        String formattedStartTime = ticket.getStartTime().format(timeFormatter);
        String formattedEndTime = ticket.getEndTime().format(timeFormatter);

        String seatInfo = ticket.getSeats().stream()
                .map(seat -> seat.getRowLabel() + "-" + seat.getSeatNumber())
                .collect(Collectors.joining(", "));

        context.setVariable("userName", ticket.getUserName());
        context.setVariable("eventTitle", ticket.getEventTitle());
        context.setVariable("eventDate", formattedDate);
        context.setVariable("startTime", formattedStartTime);
        context.setVariable("endTime", formattedEndTime);
        context.setVariable("eventLocation", ticket.getEventLocation());
        context.setVariable("sectionName", ticket.getSectionName());
        context.setVariable("seatInfo", seatInfo);
        context.setVariable("seatCount", ticket.getSeatCount());
        context.setVariable("totalPrice", ticket.getTotalPrice());
        context.setVariable("ticketId", ticket.getId());
        context.setVariable("hasQrCode", ticket.getQrCodeUrl() != null);

        return templateEngine.process("ticket-confirmation", context);
    }

    @Async
    public void sendTicketCancellationEmail(String userEmail, String userName,
                                            String eventTitle, String ticketId) {
        try {
            logger.info("Preparing to send cancellation email to: {}", userEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject("Ticket Cancellation - " + eventTitle);

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("eventTitle", eventTitle);
            context.setVariable("ticketId", ticketId);

            String emailContent = templateEngine.process("ticket-cancellation", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            logger.info("Cancellation email sent successfully to: {}", userEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send cancellation email to {}: {}", userEmail, e.getMessage(), e);
        }
    }
}