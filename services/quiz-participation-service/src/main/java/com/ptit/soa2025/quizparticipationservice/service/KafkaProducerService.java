package com.ptit.soa2025.quizparticipationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.quizparticipationservice.dto.event.QuizSubmissionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.submission}")
    private String submissionTopic;

    /**
     * Send quiz submission event to Kafka
     *
     * @param event Quiz submission event
     * @return CompletableFuture of sending result
     */
    public CompletableFuture<SendResult<String, String>> sendQuizSubmissionEvent(QuizSubmissionEvent event) {
        String key = event.getPayload().getExamSessionId();
        String value;
        
        try {
            value = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }

        log.info("Sending quiz submission event to topic {}: key={}, payload={}", 
                submissionTopic, key, value);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(submissionTopic, key, value);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent message: topic={}, partition={}, offset={}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message: {}", ex.getMessage(), ex);
            }
        });

        return future;
    }
}