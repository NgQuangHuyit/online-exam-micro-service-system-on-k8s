package com.ptit.soa2025.quizservice.dto;

import com.ptit.soa2025.quizservice.entity.CorrectOptionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private UUID id;
    private UUID quizId;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
//    private CorrectOptionEnum correctOption;
}