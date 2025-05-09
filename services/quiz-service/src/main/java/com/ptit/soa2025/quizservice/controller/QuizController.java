package com.ptit.soa2025.quizservice.controller;


import com.ptit.soa2025.quizservice.dto.QuizDTO;
import com.ptit.soa2025.quizservice.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/quizzes")
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
}
