package com.aisupport.rag.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
	List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxEvent.Status status);
	
	List<OutboxEvent> findByStatus(OutboxEvent.Status status);

    List<OutboxEvent> findByStatusAndRetryCountLessThan(
    	OutboxEvent.Status status, int maxRetries
    );
}
