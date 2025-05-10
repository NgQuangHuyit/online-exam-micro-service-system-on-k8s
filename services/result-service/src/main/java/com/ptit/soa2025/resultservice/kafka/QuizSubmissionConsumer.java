package com.ptit.soa2025.resultservice.kafka;

import com.ptit.soa2025.resultservice.model.QuizResult;
import com.ptit.soa2025.resultservice.service.GradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizSubmissionConsumer {

    private final GradingService gradingService;
    
    /**
     * Lắng nghe các message từ topic quiz-submissions để chấm điểm
     * 
     * @param message Message dạng JSON từ Kafka
     * @param acknowledgment Để xác nhận đã xử lý message
     */
    @KafkaListener(topics = "${spring.kafka.topics.submissions}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSubmission(String message, Acknowledgment acknowledgment) {
        log.info("Received submission message from Kafka");
        
        try {
            // Xử lý chấm bài
            QuizResult result = gradingService.processSubmission(message);
            log.info("Successfully graded quiz for session: {}, score: {}/{}",
                    result.getExamSessionId(), result.getScore(), result.getTotalQuestions());
                    
            // Xác nhận đã xử lý message thành công
            acknowledgment.acknowledge();
            
            // TODO: Gửi notification event để thông báo kết quả cho sinh viên
            
        } catch (Exception e) {
            log.error("Error processing submission message: {}", e.getMessage(), e);
            // Không xác nhận acknowledgment ở đây để message được xử lý lại sau
            // Trong thực tế nên có chiến lược retry và dead-letter-queue
        }
    }
}