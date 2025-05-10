package com.ptit.soa2025.quizservice.exception;

public class AccessTimeInvalidException extends RuntimeException {
    public AccessTimeInvalidException(String message) {
        super(message);
    }
}
