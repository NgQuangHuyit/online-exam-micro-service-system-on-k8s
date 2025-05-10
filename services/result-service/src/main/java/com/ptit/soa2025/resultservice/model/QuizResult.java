package com.ptit.soa2025.resultservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "results")
public class QuizResult {
    
    @Id
    private String id;
    
    private String examSessionId;
    private String studentId;
    private String quizId;
    private Double score;
    private Integer totalQuestions;
    
    private List<ScoringDetail> scoringDetails;
    
    private LocalDateTime submissionTime;
    private LocalDateTime gradedAt;
    private String status;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoringDetail {
        private String questionId;
        private String selectedOption;
        private String correctOption;
        private boolean isCorrect;
    }
}