package com.ptit.soa2025.quizservice.exception;

import com.ptit.soa2025.quizservice.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An unexpected error occurred");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidAccessException.class)
    public ResponseEntity<Object> handlerInvalidAccessEx(InvalidAccessException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessTimeInvalidException.class)
    public ResponseEntity<Object> handlerAccessTimeInvalidEx(AccessTimeInvalidException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}