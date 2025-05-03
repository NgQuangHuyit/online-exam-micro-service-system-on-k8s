package com.ptit.soa2025.quizparticipationservice.exception;

public class QuizServiceException extends RuntimeException {

    public QuizServiceException(String message) {
        super(message);
    }

    public QuizServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}