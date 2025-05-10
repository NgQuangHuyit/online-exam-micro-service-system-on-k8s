package com.ptit.soa2025.quizservice.repository;

import com.ptit.soa2025.quizservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuizId(UUID quizId);
}