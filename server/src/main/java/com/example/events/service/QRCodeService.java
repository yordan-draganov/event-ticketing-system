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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class QRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);
    private static final String hmacAlgorithm = "HmacSHA256";
    private static final String protectionSignature = "|SIGNATURE:";

    @Value("${qr.code.directory}")
    private String qrCodeDirectory;

    @Value("${qr.code.width}")
    private int width;

    @Value("${qr.code.height}")
    private int height;

    @Value("${qr.code.hmac.secret}")
    private String hmacSecret;

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

    private BitMatrix generateQRCodeMatrix(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
    }

    public String buildTicketQRContent(UUID ticketId, UUID eventId, String eventTitle, String userName, String seatInfo) {
        String data = String.format(
                "TICKET_ID:%s|EVENT_ID:%s|EVENT:%s|USER:%s|SEATS:%s",
                ticketId, eventId, eventTitle, userName, seatInfo
        );
        
        String signature = generateHMAC(data);
        return data + QRCodeService.protectionSignature + signature;
    }


    private String generateHMAC(String data) {
        try {
            Mac mac = Mac.getInstance(hmacAlgorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8),
                    hmacAlgorithm
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating HMAC signature: {}", e.getMessage());
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    public boolean verifySignature(String qrContent) {
        try {
            if (qrContent == null || !qrContent.contains(protectionSignature)) {
                logger.warn("QR code content does not contain signature separator");
                return false;
            }

            int signatureIndex = qrContent.lastIndexOf(protectionSignature);
            if (signatureIndex == -1) {
                logger.warn("Invalid QR code format: signature separator not found");
                return false;
            }

            String data = qrContent.substring(0, signatureIndex);
            String providedSignature = qrContent.substring(signatureIndex + protectionSignature.length());

            String expectedSignature = generateHMAC(data);

            return constantTimeEquals(providedSignature, expectedSignature);
        } catch (Exception e) {
            logger.error("Error verifying QR code signature: {}", e.getMessage());
            return false;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public Map<String, String> parseQRCodeContent(String qrContent) {
        if (qrContent == null || !qrContent.contains(protectionSignature)) {
            logger.warn("Invalid QR code format");
            return null;
        }

        int signatureIndex = qrContent.lastIndexOf(protectionSignature);
        String data = qrContent.substring(0, signatureIndex);

        Map<String, String> ticketData = new HashMap<>();
        String[] parts = data.split("\\|");

        for (String part : parts) {
            int colonIndex = part.indexOf(':');
            if (colonIndex > 0 && colonIndex < part.length() - 1) {
                String key = part.substring(0, colonIndex);
                String value = part.substring(colonIndex + 1);
                ticketData.put(key, value);
            }
        }

        return ticketData;
    }

    public Map<String, String> validateAndParse(String qrContent) {
        if (!verifySignature(qrContent)) {
            logger.warn("QR code signature verification failed");
            return null;
        }

        return parseQRCodeContent(qrContent);
    }
}