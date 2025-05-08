package com.ptit.soa2025.resultservice.dto.event;

import com.ptit.soa2025.resultservice.dto.StudentAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        private List<StudentAnswer> answers;
        private Map<String, Object> metadata;
    }

}