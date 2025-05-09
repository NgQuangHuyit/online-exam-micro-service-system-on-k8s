package com.ptit.soa2025.quizservice.service.impl;

import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.QuizDTO;
import com.ptit.soa2025.quizservice.entity.Question;
import com.ptit.soa2025.quizservice.entity.Quiz;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import com.ptit.soa2025.quizservice.repository.QuestionRepository;
import com.ptit.soa2025.quizservice.repository.QuizAccessCodeRepository;
import com.ptit.soa2025.quizservice.repository.QuizRepository;
import com.ptit.soa2025.quizservice.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAccessCodeRepository accessCodeRepository;

    @Autowired
    public QuizServiceImpl(QuizRepository quizRepository, 
                         QuestionRepository questionRepository,
                         QuizAccessCodeRepository accessCodeRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.accessCodeRepository = accessCodeRepository;
    }

    

    @Override
    public QuizDTO getQuizById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        List<Question> questions = questionRepository.findByQuizId(id);
        return convertToQuizDTO(quiz, questions);
    }

    @Override
    public List<QuizDTO> getAllQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();
        return quizzes.stream()
                .map(quiz -> {
                    List<Question> questions = questionRepository.findByQuizId(quiz.getId());
                    return convertToQuizDTO(quiz, questions);
                })
                .collect(Collectors.toList());
    }

    

    
    
    @Override
    public List<String> getAllowedStudentsForQuiz(UUID quizId) {
        // Check if quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }
        
        // Get all active access codes for the quiz
        return accessCodeRepository.findByQuizId(quizId)
                .stream()
                .map(QuizAccessCode::getStudentId)
                .distinct()
                .collect(Collectors.toList());
    }

    private QuizDTO convertToQuizDTO(Quiz quiz, List<Question> questions) {
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setId(quiz.getId());
        quizDTO.setTitle(quiz.getTitle());
        quizDTO.setDescription(quiz.getDescription());
        quizDTO.setValidFrom(quiz.getValidFrom());
        quizDTO.setValidUntil(quiz.getValidUntil());
        
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(question -> {
                    QuestionDTO questionDTO = new QuestionDTO();
                    questionDTO.setId(question.getId());
                    questionDTO.setQuizId(quiz.getId());
                    questionDTO.setContent(question.getContent());
                    questionDTO.setOptionA(question.getOptionA());
                    questionDTO.setOptionB(question.getOptionB());
                    questionDTO.setOptionC(question.getOptionC());
                    questionDTO.setOptionD(question.getOptionD());
                    return questionDTO;
                })
                .collect(Collectors.toList());
        
        quizDTO.setQuestions(questionDTOs);
        return quizDTO;
    }
}