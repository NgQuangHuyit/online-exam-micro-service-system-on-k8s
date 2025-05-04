package com.ptit.soa2025.quizparticipationservice.exception;

import com.ptit.soa2025.quizparticipationservice.constant.ErrorCode;
import com.ptit.soa2025.quizparticipationservice.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import org.springframework.web.bind.annotation.ExceptionHandler;


@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler({QuizParticipationException.class})
    public ResponseEntity<ErrorResponse> handleQuizParticipationException(QuizParticipationException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder().
                message(ex.getMessage()).
                errorCode(ex.getErrorCode().getHttpStatus().value()).
                build();
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus().value()).body(errorResponse);
    }
}
