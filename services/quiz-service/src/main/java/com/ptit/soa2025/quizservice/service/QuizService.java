package com.ptit.soa2025.quizservice.service;

import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.QuizDTO;
import com.ptit.soa2025.quizservice.dto.request.AccessValidationRequest;
import com.ptit.soa2025.quizservice.dto.response.AccessValidationResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizDTO getQuizById(UUID id);
    List<QuizDTO> getAllQuizzes();
    List<String> getAllowedStudentsForQuiz(UUID quizId);
    List<QuestionDTO> getQuizQuestions(UUID quizId);
    List<AnswerDTO> getQuizAnswers(UUID quizId);
//    AccessValidationResponse validateQuizAccess(AccessValidationRequest accessValidationRequest);
}