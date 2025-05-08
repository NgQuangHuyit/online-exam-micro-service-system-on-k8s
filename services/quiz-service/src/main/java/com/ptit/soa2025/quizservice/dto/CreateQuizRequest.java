package com.ptit.soa2025.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {
    private String title;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private List<QuestionDTO> questions;
}