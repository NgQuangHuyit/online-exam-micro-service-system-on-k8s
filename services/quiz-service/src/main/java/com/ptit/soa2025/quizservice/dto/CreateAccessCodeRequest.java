package com.ptit.soa2025.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccessCodeRequest {
    private String studentId;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}