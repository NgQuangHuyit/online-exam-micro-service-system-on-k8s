package com.ptit.soa2025.quizparticipationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswer {
    private String questionId;
    private String correctAnswer;
}