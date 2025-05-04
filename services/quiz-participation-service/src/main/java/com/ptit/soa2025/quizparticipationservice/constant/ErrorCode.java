package com.ptit.soa2025.quizparticipationservice.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    QUIZ_NOT_FOUND("QUIZ_NOT_FOUND", HttpStatus.NOT_FOUND),
    QUIZ_PARTICIPATION_SUBMITTED("QUIZ_PARTICIPATION_SUBMITTED", HttpStatus.BAD_REQUEST);

    @Getter
    private final String message;
    @Getter
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }


}
