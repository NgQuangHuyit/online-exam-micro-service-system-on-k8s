package com.ptit.soa2025.quizparticipationservice.client;

import com.ptit.soa2025.quizparticipationservice.config.QuizClientConfig;
import com.ptit.soa2025.quizparticipationservice.dto.request.ValidateAccessCodeRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizparticipationservice.dto.QuizQuestion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign Client for Quiz Service
 */
@FeignClient(name = "quiz-service", url = "${services.quiz-service.url}", configuration = QuizClientConfig.class)
public interface QuizServiceClient {

    /**
     * Validates access code for a quiz
     * @param quizId ID of the quiz
     * @param request DTO containing studentId and accessCode
     * @return AccessValidationResponse containing validation result
     */
    @PostMapping("/quizzes/access-validate")
    AccessValidationResponse validateAccessCode(@RequestParam("quizId") String quizId,
                                               @RequestBody ValidateAccessCodeRequest request);

    /**
     * Retrieves questions for a quiz
     * @param quizId ID of the quiz
     * @return List of questions
     */
    @GetMapping("/quizzes/{quizId}/questions")
    List<QuizQuestion> getQuestionsForQuiz(@PathVariable("quizId") String quizId);
}