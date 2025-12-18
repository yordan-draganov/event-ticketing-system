package com.example.events.exception;

public class TokenBlacklistedException extends RuntimeException {
    public TokenBlacklistedException(String message) {
        super(message);
    }
}
