package com.ptit.soa2025.quizservice.repository;

import com.ptit.soa2025.quizservice.entity.AccessCodeStatusEnum;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAccessCodeRepository extends JpaRepository<QuizAccessCode, UUID> {
    List<QuizAccessCode> findByQuizId(UUID quizId);
    List<QuizAccessCode> findByStudentId(String studentId);
    Optional<QuizAccessCode> findByAccessCode(String accessCode);
    List<QuizAccessCode> findByQuizIdAndStudentId(UUID quizId, String studentId);
    List<QuizAccessCode> findByQuizIdAndStatus(UUID quizId, AccessCodeStatusEnum status);
}