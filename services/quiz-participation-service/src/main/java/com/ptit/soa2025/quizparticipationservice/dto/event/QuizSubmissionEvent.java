package com.ptit.soa2025.quizparticipationservice.dto.event;

import com.ptit.soa2025.quizparticipationservice.dto.request.SubmitQuizRequest;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionEvent {
    private String messageId;
    private LocalDateTime timestamp;
    private String eventType;
    private PayloadData payload;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayloadData {
        private String examSessionId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String studentId;
        private String quizId;
        private LocalDateTime submissionTime;
        private List<SubmitQuizRequest.SubmittedAnswer> answers;
        private Map<String, Object> metadata;
    }


}