package com.ptit.soa2025.quizservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.QuizDTO;
import com.ptit.soa2025.quizservice.dto.QuizAccessCodeDTO;
import com.ptit.soa2025.quizservice.entity.Question;
import com.ptit.soa2025.quizservice.entity.Quiz;
import com.ptit.soa2025.quizservice.entity.QuizAccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityMapper {

    private final ObjectMapper objectMapper;
    
    /**
     * Convert Quiz entity to QuizDTO including questions
     * 
     * @param quiz Quiz entity
     * @param questions List of Question entities
     * @return QuizDTO
     */
    public QuizDTO toQuizDTO(Quiz quiz, List<Question> questions) {
        QuizDTO quizDTO = objectMapper.convertValue(quiz, QuizDTO.class);
        
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(question -> {
                    QuestionDTO dto = objectMapper.convertValue(question, QuestionDTO.class);
                    dto.setQuizId(quiz.getId());
                    return dto;
                })
                .collect(Collectors.toList());
        
        quizDTO.setQuestions(questionDTOs);
        return quizDTO;
    }
    
    /**
     * Convert Question entity to QuestionDTO without correct answer
     * 
     * @param question Question entity
     * @param quizId Quiz ID
     * @return QuestionDTO
     */
    public QuestionDTO toQuestionDTO(Question question, UUID quizId) {
        QuestionDTO dto = objectMapper.convertValue(question, QuestionDTO.class);
        dto.setQuizId(quizId);
        // Explicitly set correctOption to null to ensure it's not included
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        return dto;
    }
    
    /**
     * Convert Question entity to AnswerDTO for answers endpoint
     * 
     * @param question Question entity
     * @return AnswerDTO
     */
    public AnswerDTO toAnswerDTO(Question question) {
        return new AnswerDTO(question.getId(), question.getCorrectOption());
    }
    
    /**
     * Convert QuizAccessCode entity to QuizAccessCodeDTO
     * 
     * @param accessCode QuizAccessCode entity
     * @return QuizAccessCodeDTO
     */
    public QuizAccessCodeDTO toQuizAccessCodeDTO(QuizAccessCode accessCode) {
        QuizAccessCodeDTO dto = objectMapper.convertValue(accessCode, QuizAccessCodeDTO.class);
        dto.setQuizId(accessCode.getQuiz().getId());
        return dto;
    }
}