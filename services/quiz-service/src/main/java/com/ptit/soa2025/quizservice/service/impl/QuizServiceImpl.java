package com.ptit.soa2025.quizservice.service.impl;

import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.QuizDTO;
import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.entity.Question;
import com.ptit.soa2025.quizservice.entity.Quiz;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import com.ptit.soa2025.quizservice.mapper.EntityMapper;
import com.ptit.soa2025.quizservice.repository.QuestionRepository;
import com.ptit.soa2025.quizservice.repository.QuizAccessCodeRepository;
import com.ptit.soa2025.quizservice.repository.QuizRepository;
import com.ptit.soa2025.quizservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAccessCodeRepository accessCodeRepository;
    private final EntityMapper entityMapper;

    @Override
    public QuizDTO getQuizById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        List<Question> questions = questionRepository.findByQuizId(id);
        return entityMapper.toQuizDTO(quiz, questions);
    }

    @Override
    public List<QuizDTO> getAllQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();
        return quizzes.stream()
                .map(quiz -> {
                    List<Question> questions = questionRepository.findByQuizId(quiz.getId());
                    return entityMapper.toQuizDTO(quiz, questions);
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

    @Override
    public List<QuestionDTO> getQuizQuestions(UUID quizId) {
        // Check if quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }
        
        // Retrieve all questions for the quiz
        List<Question> questions = questionRepository.findByQuizId(quizId);
        
        // Convert to DTOs without including the correct option using our mapper
        return questions.stream()
            .map(question -> entityMapper.toQuestionDTO(question, quizId))
            .collect(Collectors.toList());
    }

    @Override
    public List<AnswerDTO> getQuizAnswers(UUID quizId) {
        // Check if quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }
        
        // Retrieve all questions for the quiz
        List<Question> questions = questionRepository.findByQuizId(quizId);
        
        // Convert to answer DTOs with the correct options
        return questions.stream()
            .map(entityMapper::toAnswerDTO)
            .collect(Collectors.toList());
    }
}