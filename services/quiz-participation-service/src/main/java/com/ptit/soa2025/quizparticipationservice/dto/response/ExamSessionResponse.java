package com.ptit.soa2025.quizparticipationservice.dto.response;

import com.ptit.soa2025.quizparticipationservice.dto.QuizInfo;
import com.ptit.soa2025.quizparticipationservice.dto.QuizQuestion;
import com.ptit.soa2025.quizparticipationservice.model.ParticipationSession.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionResponse {
    private String sessionId;
    private QuizInfo quizInfo;
    private String studentId;
    private Long startTime;
    private Long endTime;
    private SessionStatus status;
    private List<QuizQuestion> questions;
}