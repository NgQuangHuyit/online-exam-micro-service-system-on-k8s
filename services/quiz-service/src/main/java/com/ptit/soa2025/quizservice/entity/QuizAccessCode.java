package com.ptit.soa2025.quizservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_access_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(QuizAccessCode.QuizAccessCodeId.class)
public class QuizAccessCode {
    
    // First part of composite primary key - using the quiz object directly
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    // Second part of composite primary key
    @Id
    @Column(name = "student_id", nullable = false)
    private String studentId;
    
    @Column(name = "access_code", nullable = false, unique = true)
    private String accessCode;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method to get the quiz ID without loading the entire Quiz object
    public UUID getQuizId() {
        return this.quiz != null ? this.quiz.getId() : null;
    }
    
    /**
     * Composite primary key class for QuizAccessCode
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class QuizAccessCodeId implements Serializable {
        private Quiz quiz;  // This matches the quiz field in the entity
        private String studentId;
    }
}