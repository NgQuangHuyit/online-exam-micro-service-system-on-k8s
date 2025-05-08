package com.ptit.soa2025.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessValidationRequest {
    private String studentId;
    private String accessCode;
}