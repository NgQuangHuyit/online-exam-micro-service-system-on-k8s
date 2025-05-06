package com.ptit.soa2025.quizparticipationservice.service;

import com.ptit.soa2025.quizparticipationservice.client.QuizServiceClient;
import com.ptit.soa2025.quizparticipationservice.constant.ErrorCode;
import com.ptit.soa2025.quizparticipationservice.dto.QuizInfo;
import com.ptit.soa2025.quizparticipationservice.dto.event.QuizSubmissionEvent;
import com.ptit.soa2025.quizparticipationservice.dto.request.StartParticipationSessionRequest;
import com.ptit.soa2025.quizparticipationservice.dto.request.SubmitQuizRequest;
import com.ptit.soa2025.quizparticipationservice.dto.request.ValidateAccessCodeRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.AccessValidationResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.ExamSessionResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.QuestionResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.SubmitQuizResponse;
import com.ptit.soa2025.quizparticipationservice.exception.QuizParticipationException;
import com.ptit.soa2025.quizparticipationservice.exception.QuizServiceException;
import com.ptit.soa2025.quizparticipationservice.model.ParticipationSession;
import com.ptit.soa2025.quizparticipationservice.repository.ParticipationSessionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamParticipationService {

    private final QuizServiceClient quizServiceClient;
    private final ParticipationSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaProducerService kafkaProducerService;
    
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
    public ExamSessionResponse startQuizWithCode(StartParticipationSessionRequest request)  {
        log.info("Starting quiz with code request for student: {}, quiz: {}", 
                request.getStudentId(), request.getQuizId());


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
        }
        catch (FeignException e) {
            log.error("Error calling quiz service: {}", e.getMessage());
            throw new QuizServiceException("Error validating access code", e);
        }
        // Kiểm tra phiên làm bài đã tồn tại chưa
        Optional<ParticipationSession> existingSession =
                sessionRepository.findByStudentIdAndQuizId(request.getStudentId(), request.getQuizId());

        // Nếu đã có phiên làm bài và đã nộp, trả về thông báo
        if (existingSession.isPresent() && existingSession.get().isSubmitted()) {
            ParticipationSession session = existingSession.get();
            log.info("Student already completed this quiz. Session ID: {}", session.getId());
            throw  new QuizParticipationException(ErrorCode.QUIZ_PARTICIPATION_SUBMITTED);
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
            LocalDateTime endTime = startTime.plus(Duration.ofMinutes(validationResponse.getQuizInfo().getDuration()));
            
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
        
        return buildExamSessionResponse(session, questions, validationResponse.getQuizInfo());
    }
    
    /**
     * Submits a quiz and sends an event to Kafka for grading
     * 
     * @param sessionId ID of the session
     * @param request The submission containing answers
     * @return SubmitQuizResponse with submission details
     * @throws QuizParticipationException if submission is invalid
     */
    @Transactional
    public SubmitQuizResponse submitQuiz(String sessionId, SubmitQuizRequest request) {
        log.info("Processing quiz submission for session: {}", sessionId);

        LocalDateTime submissionTime = LocalDateTime.now();
        // 1. Find and validate session
        ParticipationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new QuizParticipationException(ErrorCode.QUIZ_NOT_FOUND, 
                        "Không tìm thấy phiên làm bài với ID: " + sessionId));
        
//        // 2. Check if already submitted
//        if (session.isSubmitted()) {
//            throw new QuizParticipationException(ErrorCode.QUIZ_PARTICIPATION_SUBMITTED);
//        }
//
//        // 3. Check if session is still valid (not expired)
//        if (!session.isValid(submissionTime)) {
//            throw new QuizParticipationException(ErrorCode.QUIZ_PARTICIPATION_TIME_OVER);
//        }

        // 4. Create and send Kafka event
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("clientIp", "127.0.0.1");
        metadata.put("userAgent", "ExamClient/1.0");
        
        QuizSubmissionEvent event = QuizSubmissionEvent.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType("QUIZ_SUBMISSION_CREATED")
                .payload(QuizSubmissionEvent.PayloadData.builder()
                        .examSessionId(sessionId)
                        .startTime(session.getStartTime())
                        .submissionTime(session.getEndTime())
                        .endTime(session.getEndTime())
                        .studentId(session.getStudentId())
                        .quizId(session.getQuizId())
                        .submissionTime(submissionTime)
                        .answers(request.getAnswers())
                        .metadata(metadata)
                        .build())
                .build();
        
        try {
            SendResult<String, String> result = kafkaProducerService.sendQuizSubmissionEvent(event)
                    .get(); // Block until sent

            log.info("Successfully sent Kafka message: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            session.markAsSubmitted();
            sessionRepository.save(session);
        } catch (Exception e) {
            log.error("Failed to send quiz submission to Kafka: {}", e.getMessage(), e);
            // We don't throw exception here as we want submission to succeed even if Kafka is down
            // In a production app, consider implementing retry mechanism or store events in a local queue
        }
        
        // 6. Return submission response
        return SubmitQuizResponse.builder()
                .sessionId(sessionId)
                .message("Bài thi đã được nộp thành công và đang được chấm điểm")
                .submittedAt(submissionTime)
                .totalAnswers(request.getAnswers().size())
                .build();
    }
    
    /**
     * Gets questions for a quiz, with Redis caching
     */
    @SuppressWarnings("unchecked")
    private List<QuestionResponse> getQuestionsForQuiz(String quizId) {
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
    private ExamSessionResponse buildExamSessionResponse(ParticipationSession session, List<QuestionResponse> questions, QuizInfo quizInfo) {
        return ExamSessionResponse.builder()
                .sessionId(session.getId())
                .quizInfo(quizInfo)
                .studentId(session.getStudentId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .questions(questions)
                .build();
    }
}