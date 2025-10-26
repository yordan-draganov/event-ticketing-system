package com.example.events.exception;

public class UserAlreadyLoggedOutException extends RuntimeException {
    public UserAlreadyLoggedOutException(String message) {
        super(message);
    }
}
