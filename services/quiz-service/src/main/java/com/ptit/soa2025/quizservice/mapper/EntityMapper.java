package com.ptit.soa2025.quizservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.quizservice.dto.AnswerDTO;
import com.ptit.soa2025.quizservice.dto.QuestionDTO;
import com.ptit.soa2025.quizservice.dto.QuizInfoDTO;
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
     * Convert Quiz entity to QuizInfoDTO including questions
     * 
     * @param quiz Quiz entity
     * @param questions List of Question entities
     * @return QuizInfoDTO
     */
//    public QuizInfoDTO toQuizDTO(List<Question> questions) {
//
//        List<QuestionDTO> questionDTOs = questions.stream()
//                .map(question -> {
//                    QuestionDTO dto = objectMapper.convertValue(question, QuestionDTO.class);
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        return quizDTO;
//    }
    
    /**
     * Convert Question entity to QuestionDTO without correct answer
     * 
     * @param question Question entity
     * @return QuestionDTO
     */
    public QuestionDTO toQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(question.getId());
        dto.setContent(question.getContent());
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