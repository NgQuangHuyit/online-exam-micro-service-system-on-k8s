package com.ptit.soa2025.quizparticipationservice.dto.response;

import com.ptit.soa2025.quizparticipationservice.dto.QuizInfo;
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
    private String accessCodeId;
    private QuizInfo quizInfo;
    private String studentId;
    private String message;
}