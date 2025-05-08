package com.ptit.soa2025.quizparticipationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    private String id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}