package com.aisupport.ticket.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.common.enums.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
	List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
	
	List<OutboxEvent> findByStatus(OutboxStatus status);

    List<OutboxEvent> findByStatusAndRetryCountLessThan(
    		OutboxStatus status, int maxRetries
    );
}
