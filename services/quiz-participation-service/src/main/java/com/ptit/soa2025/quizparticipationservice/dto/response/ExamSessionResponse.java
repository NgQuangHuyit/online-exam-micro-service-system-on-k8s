package com.ptit.soa2025.quizparticipationservice.dto.response;

import com.ptit.soa2025.quizparticipationservice.dto.QuizInfo;
import com.ptit.soa2025.quizparticipationservice.model.ParticipationSession.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionResponse {
    private String sessionId;
    private QuizInfo quizInfo;
    private String studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;
    private List<QuestionResponse> questions;
}