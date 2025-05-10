package com.ptit.soa2025.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizInfoDTO {
    private UUID quizId;
    private String title;
    private String description;
    private List<QuestionDTO> questions;
}