package com.ptit.soa2025.quizparticipationservice.client;

import com.ptit.soa2025.quizparticipationservice.dto.request.ValidateAccessCodeRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.QuestionResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Feign Client for Quiz Service
 */
@FeignClient(name = "quiz-service", url = "${services.quiz-service.url}")
public interface QuizServiceClient {
    
    /**
     * Validates access code for a quiz
     * @param quizId ID of the quiz
     * @param request DTO containing studentId and accessCode
     * @return AccessValidationResponse containing validation result
     */
    @PostMapping("/quizzes/{quizId}/access-validate")
    AccessValidationResponse validateAccessCode(@PathVariable("quizId") UUID quizId, 
                                               @RequestBody ValidateAccessCodeRequest request);
    
    /**
     * Retrieves questions for a quiz
     * @param quizId ID of the quiz
     * @return List of questions
     */
    @GetMapping("/quizzes/{quizId}/questions")
    List<QuestionResponse> getQuestionsForQuiz(@PathVariable("quizId") UUID quizId);
}