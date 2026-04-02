package com.example.events.exception;

public class HMACGenerationException extends RuntimeException {
    public HMACGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
