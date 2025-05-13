package com.ptit.soa2025.quizservice.controller;

import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.request.AccessValidationRequest;
import com.ptit.soa2025.quizservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizservice.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{quizId}/allowed-students")
    public ResponseEntity<List<String>> getAllowedStudentsForQuiz(@PathVariable UUID quizId) {
        List<String> allowedStudents = quizService.getAllowedStudentsForQuiz(quizId);
        return ResponseEntity.ok(allowedStudents);
    }
    
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<?> getQuizQuestions(@PathVariable UUID quizId) {
        try {
            List<QuestionDTO> questions = quizService.getQuizQuestions(quizId);
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Quiz not found with id: " + quizId);
        }
    }
    
    @GetMapping("/{quizId}/answers")
    public ResponseEntity<?> getQuizAnswers(@PathVariable UUID quizId) {
        try {
            List<AnswerDTO> answers = quizService.getQuizAnswers(quizId);
            return ResponseEntity.ok(answers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Quiz not found with id: " + quizId);
        }
    }

    @PostMapping("/access-validate")
    public ResponseEntity<?> validateQuizAccess( @RequestParam("quizId") UUID quizId,
                                                 @RequestBody AccessValidationRequest request) {
        AccessValidationResponse response = quizService.validateQuizAccess(request, quizId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quizId}/info")
    public ResponseEntity<?> getQuizInfo(@PathVariable UUID quizId) {
        try {
            AccessValidationResponse.QuizInfo info = quizService.getQuizInfo(quizId);
            return ResponseEntity.ok(info);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Quiz not found with id: " + quizId);
        }
    }
}
