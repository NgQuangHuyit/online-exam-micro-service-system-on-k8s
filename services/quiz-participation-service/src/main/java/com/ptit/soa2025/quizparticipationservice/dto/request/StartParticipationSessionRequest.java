package com.ptit.soa2025.quizparticipationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartParticipationSessionRequest {
    private String quizId;
    private String studentId;
    private String accessCode;
}
