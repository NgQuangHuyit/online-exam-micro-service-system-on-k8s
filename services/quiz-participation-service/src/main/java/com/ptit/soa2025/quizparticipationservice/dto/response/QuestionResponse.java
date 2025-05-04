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
public class QuestionResponse {
    private UUID id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}