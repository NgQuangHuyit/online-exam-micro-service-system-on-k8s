package com.ptit.soa2025.resultservice.repository;

import com.ptit.soa2025.resultservice.model.QuizResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizResultRepository extends MongoRepository<QuizResult, String> {
    
    Optional<QuizResult> findByExamSessionId(String examSessionId);
    
    Optional<QuizResult> findByStudentIdAndQuizId(String studentId, String quizId);
}