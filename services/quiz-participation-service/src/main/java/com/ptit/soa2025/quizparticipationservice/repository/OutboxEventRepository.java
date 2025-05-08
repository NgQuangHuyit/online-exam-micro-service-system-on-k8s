package com.ptit.soa2025.quizparticipationservice.repository;

import com.ptit.soa2025.quizparticipationservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents();
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries AND e.createdAt > :since ORDER BY e.createdAt ASC")
    List<OutboxEvent> findRetryableFailedEvents(@Param("maxRetries") int maxRetries, @Param("since") LocalDateTime since);
    
    List<OutboxEvent> findByAggregateIdAndEventType(String aggregateId, String eventType);
}