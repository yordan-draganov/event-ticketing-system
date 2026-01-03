package com.example.events.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class QRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);

    @Value("${qr.code.directory}")
    private String qrCodeDirectory;

    @Value("${qr.code.width}")
    private int width;

    @Value("${qr.code.height}")
    private int height;

    public String generateAndSaveQRCode(UUID ticketId, String content) {
        try {
            Path directory = Paths.get(qrCodeDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            BitMatrix bitMatrix = generateQRCodeMatrix(content);

            String fileName = "ticket_" + ticketId.toString() + ".png";
            Path filePath = directory.resolve(fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            logger.info("QR code generated and saved: {}", filePath);

            return "/qr-codes/" + fileName;

        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code for ticket {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public String generateQRCodeBase64(String content) {
        try {
            BitMatrix bitMatrix = generateQRCodeMatrix(content);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrCodeBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);

            return "data:image/png;base64," + base64Image;

        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private BitMatrix generateQRCodeMatrix(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
    }

    public String buildTicketQRContent(UUID ticketId, UUID eventId, String eventTitle, String userName, String seatInfo) {
        return String.format(
                "TICKET_ID:%s|EVENT_ID:%s|EVENT:%s|USER:%s|SEATS:%s",
                ticketId, eventId, eventTitle, userName, seatInfo
        );
    }
}