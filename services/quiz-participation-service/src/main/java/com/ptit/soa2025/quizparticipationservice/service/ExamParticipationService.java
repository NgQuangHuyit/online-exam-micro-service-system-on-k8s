package com.ptit.soa2025.quizparticipationservice.service;

import com.ptit.soa2025.quizparticipationservice.client.QuizServiceClient;
import com.ptit.soa2025.quizparticipationservice.dto.request.StartParticipationSessionRequest;
import com.ptit.soa2025.quizparticipationservice.dto.request.ValidateAccessCodeRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.ExamSessionResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.QuestionResponse;
import com.ptit.soa2025.quizparticipationservice.exception.QuizServiceException;
import com.ptit.soa2025.quizparticipationservice.model.ParticipationSession;
import com.ptit.soa2025.quizparticipationservice.repository.ParticipationSessionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamParticipationService {

    private final QuizServiceClient quizServiceClient;
    private final ParticipationSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Thời gian làm bài (60 phút)
    private static final Duration DEFAULT_EXAM_DURATION = Duration.ofMinutes(60);
    
    // Thời gian cache câu hỏi (24 giờ)
    private static final long CACHE_DURATION = 24 * 60 * 60;

    /**
     * Starts a quiz session using an access code
     * 
     * @param request Request containing quiz ID, student ID and access code
     * @return ExamSessionResponse with session details and questions
     */
    @Transactional
    public ExamSessionResponse startQuizWithCode(StartParticipationSessionRequest request) {
        log.info("Starting quiz with code request for student: {}, quiz: {}", 
                request.getStudentId(), request.getQuizId());
                
        // Kiểm tra phiên làm bài đã tồn tại chưa
        Optional<ParticipationSession> existingSession = 
                sessionRepository.findByStudentIdAndQuizId(request.getStudentId(), request.getQuizId());
        
        // Nếu đã có phiên làm bài và đã nộp, trả về thông báo
        if (existingSession.isPresent() && existingSession.get().isSubmitted()) {
            ParticipationSession session = existingSession.get();
            log.info("Student already completed this quiz. Session ID: {}", session.getId());
            return buildExamSessionResponse(session, List.of());
        }
        
        // Xác thực mã code với Quiz Service thông qua Feign Client
        AccessValidationResponse validationResponse;
        try {
            ValidateAccessCodeRequest validateRequest = new ValidateAccessCodeRequest(
                    request.getStudentId(), 
                    request.getAccessCode());
            
            validationResponse = quizServiceClient.validateAccessCode(
                    request.getQuizId(), validateRequest);
                    
            if (validationResponse == null || !validationResponse.isValid()) {
                log.warn("Invalid access code validation for student: {}, quiz: {}", 
                        request.getStudentId(), request.getQuizId());
                throw new IllegalArgumentException("Invalid access code or credentials");
            }
        } catch (FeignException.Unauthorized e) {
            log.error("Unauthorized access: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid access code or credentials");
        } catch (FeignException e) {
            log.error("Error calling quiz service: {}", e.getMessage());
            throw new QuizServiceException("Error validating access code", e);
        }
        
        // Xử lý phiên làm bài
        ParticipationSession session;
        if (existingSession.isPresent()) {
            // Sử dụng phiên hiện có
            session = existingSession.get();
            log.info("Reusing existing session: {}", session.getId());
        } else {
            // Tạo phiên mới
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = startTime.plus(DEFAULT_EXAM_DURATION);
            
            session = ParticipationSession.createSession(
                    request.getStudentId(), 
                    request.getQuizId(), 
                    startTime, 
                    endTime);
            
            session = sessionRepository.save(session);
            log.info("Created new session: {}", session.getId());
        }
        
        // Lấy câu hỏi cho bài thi
        List<QuestionResponse> questions = getQuestionsForQuiz(request.getQuizId());
        
        return buildExamSessionResponse(session, questions);
    }
    
    /**
     * Gets questions for a quiz, with Redis caching
     */
    @SuppressWarnings("unchecked")
    private List<QuestionResponse> getQuestionsForQuiz(UUID quizId) {
        // Tạo cache key
        String cacheKey = "quiz_questions:" + quizId;
        
        // Kiểm tra cache
        List<QuestionResponse> cachedQuestions = null;
        try {
            cachedQuestions = (List<QuestionResponse>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("Error getting questions from cache", e);
        }
        
        if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
            log.info("Cache hit for quiz questions: {}", quizId);
            return cachedQuestions;
        }
        
        // Cache miss, gọi đến Quiz Service thông qua Feign Client
        log.info("Cache miss for quiz questions: {}, fetching from Quiz Service", quizId);
        List<QuestionResponse> questions;
        try {
            questions = quizServiceClient.getQuestionsForQuiz(quizId);
            
            // Lưu vào cache
            if (questions != null && !questions.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, questions, CACHE_DURATION, TimeUnit.SECONDS);
            }
            
            return questions;
        } catch (FeignException e) {
            log.error("Error getting questions from Quiz Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Builds the response DTO from a session and questions
     */
    private ExamSessionResponse buildExamSessionResponse(ParticipationSession session, List<QuestionResponse> questions) {
        return ExamSessionResponse.builder()
                .sessionId(session.getId())
                .quizId(session.getQuizId())
                .studentId(session.getStudentId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .questions(questions)
                .build();
    }
}