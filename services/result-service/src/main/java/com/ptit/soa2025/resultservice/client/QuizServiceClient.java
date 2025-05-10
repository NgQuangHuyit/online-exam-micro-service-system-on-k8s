package com.ptit.soa2025.resultservice.client;

import com.ptit.soa2025.resultservice.dto.QuizAnswer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "quiz-service", url = "${services.quiz-service.url}")
public interface QuizServiceClient {
    
    @GetMapping("/quizzes/{quizId}/answers")
    List<QuizAnswer> getQuizAnswers(@PathVariable("quizId") String quizId);
}