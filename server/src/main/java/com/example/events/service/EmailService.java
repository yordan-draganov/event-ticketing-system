package com.example.events.service;

import com.example.events.DTO.TicketDetailResponse;
import com.example.events.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    public void sendTicketConfirmationEmail(TicketDetailResponse ticket, byte[] qrCodeImage) {
        try {
            logger.info("Preparing to send ticket confirmation email to: {}", ticket.getUserEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getUserEmail());
            helper.setSubject("Your Ticket for " + ticket.getEventTitle());

            boolean hasQrCode = qrCodeImage != null && qrCodeImage.length > 0;
            String emailContent = buildEmailContent(ticket, hasQrCode);
            helper.setText(emailContent, true);

            if (hasQrCode) {
                helper.addInline("qrCode", new ByteArrayResource(qrCodeImage), "image/png");
                logger.info("QR code attached to email from memory");
            }

            mailSender.send(message);
            logger.info("Ticket confirmation email sent successfully to: {}", ticket.getUserEmail());

        } catch (MessagingException | MailException e) {
            logger.error("Failed to send email to {}: {}", ticket.getUserEmail(), e.getMessage(), e);
            throw new EmailSendException("Failed to send email", e);
        }
    }

    private String buildEmailContent(TicketDetailResponse ticket, boolean hasQrCode) {
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
        context.setVariable("hasQrCode", hasQrCode);

        return templateEngine.process("ticket-confirmation", context);
    }

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

        } catch (MessagingException | MailException e) {
            logger.error("Failed to send cancellation email to {}: {}", userEmail, e.getMessage(), e);
            throw new EmailSendException("Failed to send cancellation email", e);
        }
    }
}