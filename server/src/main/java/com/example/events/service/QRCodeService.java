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

    @Value("${qr.code.directory}")
    private String qrCodeDirectory;

    @Value("${qr.code.width}")
    private int width;

    @Value("${qr.code.height}")
    private int height;

    @Value("${qr.code.hmac.secret}")
    private String hmacSecret;

    @Value("${app.url}")
    private String appUrl;

    public String generateAndSaveQRCode(UUID ticketId, String verificationToken) {
        try {
            Path directory = Paths.get(qrCodeDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String qrContent = buildVerificationUrl(ticketId, verificationToken);
            BitMatrix bitMatrix = generateQRCodeMatrix(qrContent);

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

    private String buildVerificationUrl(UUID ticketId, String verificationToken) {
        return String.format("%s/verify/%s?token=%s",
                appUrl,
                ticketId.toString(),
                verificationToken);
    }

    public String generateCompactToken(UUID ticketId) {
        return generateHMAC(ticketId.toString());
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
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating HMAC signature: {}", e.getMessage());
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    public boolean verifyTicketToken(UUID ticketId, String providedToken) {
        try {
            String expectedToken = generateCompactToken(ticketId);
            return constantTimeEquals(providedToken, expectedToken);
        } catch (Exception e) {
            logger.error("Error verifying ticket token: {}", e.getMessage());
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
}