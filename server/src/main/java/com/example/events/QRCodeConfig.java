package com.example.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class QRCodeConfig implements WebMvcConfigurer {

    @Value("${qr.code.directory:qr-codes}")
    private String qrCodeDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve QR code images from the file system
        String absolutePath = Paths.get(qrCodeDirectory).toAbsolutePath().toString();

        registry.addResourceHandler("/qr-codes/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCachePeriod(3600);
    }
}