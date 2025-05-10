package com.ptit.soa2025.resultservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.resultservice.client.QuizServiceClient;
import com.ptit.soa2025.resultservice.dto.QuizAnswer;
import com.ptit.soa2025.resultservice.dto.StudentAnswer;
import com.ptit.soa2025.resultservice.dto.event.QuizSubmissionEvent;
import com.ptit.soa2025.resultservice.model.QuizResult;
import com.ptit.soa2025.resultservice.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class GradingService {

    private final QuizServiceClient quizServiceClient;
    private final QuizResultRepository resultRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Điểm cho mỗi câu hỏi đúng (có thể tùy chỉnh sau)
    // Thời gian cache đáp án (24 giờ)
    private static final long CACHE_DURATION = 24 * 60 * 60;

    /**
     * Xử lý thông điệp từ Kafka và chấm điểm bài thi
     * @param message Thông điệp JSON từ Kafka
     * @return QuizResult kết quả đã được chấm điểm
     */
    public QuizResult processSubmission(String message) {
        try {
            // Parse thông điệp
            QuizSubmissionEvent event = objectMapper.readValue(message, QuizSubmissionEvent.class);
            log.info("Processing submission for session: {}", event.getPayload().getExamSessionId());
            
            // Lấy thông tin cần thiết từ event
            String quizId = event.getPayload().getQuizId();
            String examSessionId = event.getPayload().getExamSessionId();
            String studentId = event.getPayload().getStudentId();
            LocalDateTime submissionTime = event.getPayload().getSubmissionTime();
            List<StudentAnswer> submittedAnswers = event.getPayload().getAnswers();
            Map<String, Object> metadata = event.getPayload().getMetadata();
            
            // Kiểm tra nếu đã chấm điểm rồi thì không chấm lại
            Optional<QuizResult> existingResult = resultRepository.findByExamSessionId(examSessionId);
            if (existingResult.isPresent()) {
                log.info("Result already exists for session: {}", examSessionId);
                return existingResult.get();
            }
            
            // Lấy đáp án đúng từ quiz service (với cache)
            List<QuizAnswer> correctAnswers = getCorrectAnswers(quizId);
            Map<String, String> studentAnswersMap = event.getPayload().getAnswers().stream()
                    .collect(Collectors.toMap(StudentAnswer::getQuestionId, StudentAnswer::getStudentAnswer));

            // Chấm điểm
            List<QuizResult.ScoringDetail> scoringDetails = new ArrayList<>();
            double totalScore = 0.0;
            int correctCount = 0;
            
            for (QuizAnswer answer : correctAnswers) {
                String questionId = answer.getQuestionId();
                String correctAnswer = answer.getCorrectAnswer();
                String studentAnswer = studentAnswersMap.get(questionId);
                
                boolean isCorrect = correctAnswer != null && correctAnswer.equals(studentAnswer) ;

                
                if (isCorrect) {
                    correctCount++;
                }
                

                
                scoringDetails.add(QuizResult.ScoringDetail.builder()
                        .questionId(questionId)
                        .selectedOption(studentAnswer)
                        .correctOption(correctAnswer)
                        .isCorrect(isCorrect)
                        .build());
            }

            double rawScore = (double) correctCount / correctAnswers.size() * 10;
            totalScore = BigDecimal.valueOf(rawScore)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            // Tạo kết quả
            LocalDateTime now = LocalDateTime.now();
            QuizResult result = QuizResult.builder()
                    .examSessionId(examSessionId)
                    .studentId(studentId)
                    .quizId(quizId)
                    .score(totalScore)
                    .totalQuestions(submittedAnswers.size())
                    .scoringDetails(scoringDetails)
                    .submissionTime(submissionTime)
                    .gradedAt(now)
                    .status("GRADED")
                    .metadata(metadata)
                    .createdAt(now)
                    .build();
            
            // Lưu vào MongoDB
            result = resultRepository.save(result);
            log.info("Grading completed for session: {}, score: {}/{}", 
                    examSessionId, totalScore, submittedAnswers.size());
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing submission message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process submission", e);
        } catch (Exception e) {
            log.error("Error processing submission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process submission", e);
        }
    }
    
    /**
     * Lấy đáp án đúng từ Quiz Service với cache
     * @param quizId ID của bài thi
     * @return Map các đáp án đúng: questionId -> correctOption
     */
    private List<QuizAnswer> getCorrectAnswers(String quizId) {
        String cacheKey = "quiz_answers:" + quizId;
        
        // Kiểm tra cache
        List<QuizAnswer> cachedAnswers = (List<QuizAnswer>) redisTemplate.opsForValue().get(cacheKey);
//        String cachedAnswers = """
//                  {
//                      "questionId": "q1-uuid-123456",
//                      "correctAnswer": "B"
//                    },
//                    {
//                      "questionId": "q2-uuid-234567",
//                      "correctAnswer": "B"
//                    },
//                   {
//                      "questionId": "q3-uuid-345678",
//                      "correctAnswer": "B"
//                    },
//                    {
//                      "questionId": "q3-uuid-3456781",
//                      "correctAnswer": "B"
//                    }""";
        if (cachedAnswers != null) {
            try {
                log.info("Using cached answers for quiz: {}", quizId);
                return cachedAnswers;
            } catch (Exception e) {
                log.error("Error parsing cached answers: {}", e.getMessage());
                // Nếu có lỗi khi parse cache, tiếp tục lấy từ service
            }
        }

        // Cache miss hoặc parse error, lấy từ Quiz Service
        log.info("Fetching answers from Quiz Service for quiz: {}", quizId);
        List<QuizAnswer> answers = quizServiceClient.getQuizAnswers(quizId);
        
        // Lưu vào cache
        try {
//            String answersJson = objectMapper.writeValueAsString(answers);
            redisTemplate.opsForValue().set(cacheKey, answers, CACHE_DURATION, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error caching answers: {}", e.getMessage());
        }
        return answers;
    }
}