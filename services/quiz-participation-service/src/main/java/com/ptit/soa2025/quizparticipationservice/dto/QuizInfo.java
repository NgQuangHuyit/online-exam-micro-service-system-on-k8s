package com.ptit.soa2025.quizparticipationservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizInfo {
    private String quizId;
    private String title;
    private String description;
    private int duration;
}
