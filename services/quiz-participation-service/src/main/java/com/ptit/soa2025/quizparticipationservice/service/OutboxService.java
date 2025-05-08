package com.ptit.soa2025.quizparticipationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.quizparticipationservice.model.OutboxEvent;
import com.ptit.soa2025.quizparticipationservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.submission}")
    private String submissionTopic;

    /**
     * Lưu một event vào outbox trong cùng transaction với nghiệp vụ
     * 
     * @param aggregateId ID của entity liên quan (ví dụ: sessionId)
     * @param aggregateType Loại của entity (ví dụ: "EXAM_SESSION")
     * @param eventType Loại event (ví dụ: "QUIZ_SUBMISSION_CREATED")
     * @param payload Dữ liệu event
     * @return OutboxEvent đã được lưu
     */
    @Transactional
    public OutboxEvent saveEvent(String aggregateId, String aggregateType, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .topic(submissionTopic)
                    .payload(payloadJson)
                    .status(OutboxEvent.EventStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            
            return outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            log.error("Error serializing payload for outbox: {}", e.getMessage(), e);
            throw new RuntimeException("Could not save event to outbox", e);
        }
    }
}