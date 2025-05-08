package com.ptit.soa2025.quizparticipationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.soa2025.quizparticipationservice.model.OutboxEvent;
import com.ptit.soa2025.quizparticipationservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_WINDOW_HOURS = 24;

    /**
     * Scheduled job để xử lý các event đang chờ xử lý trong outbox
     * Chạy mỗi 10 giây
     */
    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay:10000}")
    public void processOutbox() {
        log.debug("Starting outbox relay process");
        
        // Lấy các event đang chờ xử lý
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
        
        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error processing outbox event {}: {}", event.getId(), e.getMessage(), e);
            }
        }
        
        // Xử lý các event đã thất bại nhưng có thể thử lại
        LocalDateTime since = LocalDateTime.now().minusHours(RETRY_WINDOW_HOURS);
        List<OutboxEvent> failedEvents = outboxEventRepository.findRetryableFailedEvents(MAX_RETRIES, since);
        
        for (OutboxEvent event : failedEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error retrying outbox event {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Xử lý một event cụ thể trong outbox
     * 
     * @param event Event cần xử lý
     */
    @Transactional
    public void processEvent(OutboxEvent event) {
        try {
            // Cập nhật trạng thái thành PROCESSING
            event.setStatus(OutboxEvent.EventStatus.PROCESSING);
            outboxEventRepository.save(event);
            
            // Gửi message đến Kafka
            String key = event.getAggregateId();
            String value = event.getPayload();
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    event.getTopic(), 
                    key, 
                    value
            );
            
            // Chờ đợi phản hồi hoặc timeout sau 5 giây
            future.thenAccept(result -> {
                markAsProcessed(event, result);
            }).exceptionally(e -> {
                markAsFailed(event, e);
                return null;
            }).get(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            markAsFailed(event, e);
        }
    }
    
    /**
     * Đánh dấu event là đã xử lý thành công
     */
    @Transactional
    public void markAsProcessed(OutboxEvent event, SendResult<String, String> result) {
        event.setStatus(OutboxEvent.EventStatus.PROCESSED);
        event.setProcessedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
        
        log.info("Successfully processed outbox event {}: topic={}, partition={}, offset={}", 
                event.getId(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }
    
    /**
     * Đánh dấu event xử lý thất bại và tăng số lần thử
     */
    @Transactional
    public void markAsFailed(OutboxEvent event, Throwable e) {
        event.setStatus(OutboxEvent.EventStatus.FAILED);
        event.setErrorMessage(e.getMessage());
        event.setRetryCount(event.getRetryCount() + 1);
        outboxEventRepository.save(event);
        
        log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage());
    }
}