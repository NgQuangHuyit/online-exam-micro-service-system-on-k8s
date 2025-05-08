package com.ptit.soa2025.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessValidationResponse {
    private boolean valid;
    private UUID accessCodeId;
    private String message;
    private String studentId;
    private QuizInfo quizInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizInfo {
        private UUID quizId;
        private String title;
        private String description;
        private Integer duration;
    }
}