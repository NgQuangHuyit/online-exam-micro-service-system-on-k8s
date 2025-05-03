package com.ptit.soa2025.quizparticipationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAccessCodeRequest {
    private UUID studentId;
    private String accessCode;
}