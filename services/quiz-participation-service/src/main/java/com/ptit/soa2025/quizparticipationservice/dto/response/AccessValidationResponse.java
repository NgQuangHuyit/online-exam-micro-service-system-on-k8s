package com.ptit.soa2025.quizparticipationservice.dto.response;

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
    private UUID quizId;
    private UUID studentId;
    private String message;
}