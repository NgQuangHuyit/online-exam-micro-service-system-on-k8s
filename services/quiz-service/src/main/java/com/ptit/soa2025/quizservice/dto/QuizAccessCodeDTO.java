package com.ptit.soa2025.quizservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAccessCodeDTO {
    private UUID id;
    private UUID quizId;
    private String studentId;
    private String accessCode;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}