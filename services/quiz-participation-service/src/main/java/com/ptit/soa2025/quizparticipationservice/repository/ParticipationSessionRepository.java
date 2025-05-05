package com.ptit.soa2025.quizparticipationservice.repository;

import com.ptit.soa2025.quizparticipationservice.model.ParticipationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipationSessionRepository extends JpaRepository<ParticipationSession, UUID> {
    Optional<ParticipationSession> findByStudentIdAndQuizId(String studentId, String quizId);

    Optional<ParticipationSession> findById(String id);
}