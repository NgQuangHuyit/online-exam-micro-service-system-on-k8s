package com.ptit.soa2025.quizservice.service.impl;

import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.dto.request.AccessValidationRequest;
import com.ptit.soa2025.quizservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizservice.entity.Question;
import com.ptit.soa2025.quizservice.entity.Quiz;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import com.ptit.soa2025.quizservice.exception.AccessTimeInvalidException;
import com.ptit.soa2025.quizservice.exception.InvalidAccessException;
import com.ptit.soa2025.quizservice.exception.QuizNotFoundException;
import com.ptit.soa2025.quizservice.mapper.EntityMapper;
import com.ptit.soa2025.quizservice.repository.QuestionRepository;
import com.ptit.soa2025.quizservice.repository.QuizAccessCodeRepository;
import com.ptit.soa2025.quizservice.repository.QuizRepository;
import com.ptit.soa2025.quizservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

//    @Override
//    public QuizInfoDTO getQuizById(UUID id) {
//        Quiz quiz = quizRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
//        List<Question> questions = questionRepository.findByQuizId(id);
//        return entityMapper.toQuizDTO(quiz, questions);
//    }
//
//    @Override
//    public List<QuizInfoDTO> getAllQuizzes() {
//        List<Quiz> quizzes = quizRepository.findAll();
//        return quizzes.stream()
//                .map(quiz -> {
//                    List<Question> questions = questionRepository.findByQuizId(quiz.getId());
//                    return entityMapper.toQuizDTO(quiz, questions);
//                })
//                .collect(Collectors.toList());
//    }

    @Override
    public List<String> getAllowedStudentsForQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        
        // Get all active access codes for the quiz
        return accessCodeRepository.findByQuiz(quiz)
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

        // Convert to answer DTOs with the correct options
        return questions.stream()
                .map(entityMapper::toQuestionDTO)
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

    @Override
    public AccessValidationResponse validateQuizAccess(AccessValidationRequest accessValidationRequest, UUID quizId) {
        // check if quiz exists
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

//        QuizAccessCode accessCode = accessCodeRepository.e(quizId, accessValidationRequest.getStudentId())
//                .orElseThrow(() -> new RuntimeException("Access code not found for quiz: " + quizId + " and student: " + accessValidationRequest.getStudentId()));
//        if (accessCode.getAccessCode() != accessValidationRequest.getAccessCode()) {
//            throw new RuntimeException("Access code mismatch");
//        }
        QuizAccessCode accessCode = accessCodeRepository.findQuizAccessCodesByQuizIdAndStudentId(quizId, accessValidationRequest.getStudentId());
        if (accessCode == null) {
            throw new InvalidAccessException("Access not allow for quiz: " + quizId + " and student: " + accessValidationRequest.getStudentId());
        }
        if (!accessCode.getAccessCode().equals(accessValidationRequest.getAccessCode())) {
            throw new InvalidAccessException("Access code mismatch");
        }
        if (accessCode.getValidFrom() != null && (accessCode.getValidFrom().isAfter(LocalDateTime.now()) && accessCode.getValidUntil() != null || accessCode.getValidUntil().isBefore(LocalDateTime.now()))) {
            throw new AccessTimeInvalidException("Ngoai thoi gian cho phep");
        }
//        boolean valid = accessCodeRepository.existsQuizAccessCodeByQuizAndStudentIdAndAccessCode(quiz, accessValidationRequest.getStudentId(), accessValidationRequest.getAccessCode());
        return AccessValidationResponse.builder()
                .studentId(accessValidationRequest.getStudentId())
                .valid(true)
                .quizInfo(AccessValidationResponse.QuizInfo.builder()
                        .quizId(quiz.getId())
                        .title(quiz.getTitle())
                        .description(quiz.getDescription())
                        .duration(quiz.getDuration())
                        .build())
                .build();

        // check if access code is valid



    }

    @Override
    public AccessValidationResponse.QuizInfo getQuizInfo(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException("Quiz not found with id: " + quizId));
        return AccessValidationResponse.QuizInfo.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .duration(quiz.getDuration())
                .build();
    }
}