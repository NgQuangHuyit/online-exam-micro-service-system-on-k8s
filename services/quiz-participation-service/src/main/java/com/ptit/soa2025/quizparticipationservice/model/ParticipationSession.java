package com.ptit.soa2025.quizparticipationservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "exam_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ParticipationSession {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "student_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String studentId;
    
    @Column(name = "quiz_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String quizId;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum SessionStatus {
        STARTED, SUBMITTED, CANCELLED
    }
    
    // Method to update status to SUBMITTED and set end time
    public void markAsSubmitted() {
        this.status = SessionStatus.SUBMITTED;
        this.updatedAt = LocalDateTime.now();
        log.info("Session {} marked as submitted at {}", id, LocalDateTime.now());
    }
    
    // Method to update status to CANCELLED
    public void markAsCancelled() {
        this.status = SessionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        log.info("Session {} marked as cancelled", id);
    }
    
    // Check if session is still valid (not expired)
    public boolean isValid(LocalDateTime currentTime) {
        boolean valid = status == SessionStatus.STARTED && 
               (endTime == null || currentTime.isBefore(endTime));
        
        if (!valid) {
            log.debug("Session {} is no longer valid. Status: {}, EndTime: {}, Current time: {}", 
                    id, status, endTime, LocalDateTime.now());
        }
        
        return valid;
    }
    
    // Check if session is already submitted
    public boolean isSubmitted() {
        return status == SessionStatus.SUBMITTED;
    }
    
    // Helper method to create a session with default timestamps
    public static ParticipationSession createSession(String studentId, String quizId, LocalDateTime startTime, LocalDateTime endTime) {
        return ParticipationSession.builder()
                .studentId(studentId)
                .quizId(quizId)
                .startTime(startTime)
                .endTime(endTime)
                .status(SessionStatus.STARTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
