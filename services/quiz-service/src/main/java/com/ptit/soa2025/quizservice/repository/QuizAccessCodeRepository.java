package com.ptit.soa2025.quizservice.repository;

import com.ptit.soa2025.quizservice.entity.Quiz;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface QuizAccessCodeRepository extends JpaRepository<QuizAccessCode, QuizAccessCode.QuizAccessCodeId> {
    QuizAccessCode findQuizAccessCodesByQuizIdAndStudentId(UUID quizId, String studentId);
    boolean existsQuizAccessCodeByQuizAndStudentIdAndAccessCode(Quiz quiz, String studentId, String accessCode);
    List<QuizAccessCode> findByQuiz(Quiz quiz);
//    List<QuizAccessCode> findByStudentId(String studentId);
//    Optional<QuizAccessCode> findByAccessCode(String accessCode);
}