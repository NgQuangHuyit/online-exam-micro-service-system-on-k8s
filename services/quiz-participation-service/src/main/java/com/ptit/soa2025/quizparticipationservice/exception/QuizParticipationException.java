package com.ptit.soa2025.quizparticipationservice.exception;

import com.ptit.soa2025.quizparticipationservice.constant.ErrorCode;
import lombok.Getter;

@Getter
public class QuizParticipationException extends RuntimeException {
    private final ErrorCode errorCode;

    public QuizParticipationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public QuizParticipationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
