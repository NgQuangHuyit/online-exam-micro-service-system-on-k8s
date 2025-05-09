package com.ptit.soa2025.quizservice.service;

import com.ptit.soa2025.quizservice.dto.QuizDTO;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizDTO getQuizById(UUID id);
    List<QuizDTO> getAllQuizzes();
    List<String> getAllowedStudentsForQuiz(UUID quizId);
}