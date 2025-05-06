package com.ptit.soa2025.quizparticipationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizResponse {
    private String sessionId;
    private String message;
    private LocalDateTime submittedAt;
    private int totalAnswers;
}