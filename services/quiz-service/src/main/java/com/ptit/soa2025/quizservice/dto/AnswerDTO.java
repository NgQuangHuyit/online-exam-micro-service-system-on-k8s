package com.ptit.soa2025.quizservice.dto;

import com.ptit.soa2025.quizservice.entity.CorrectOptionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private UUID questionId;
    private CorrectOptionEnum correctOption;
}