package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.OutboxStatus;
import com.aisupport.orchestration.infrastructure.persistence.entity.OutboxEventEntity;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {	

	List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
	
	List<OutboxEventEntity> findByStatus(OutboxStatus status);

    List<OutboxEventEntity> findByStatusAndRetryCountLessThan(
    		OutboxStatus status, int maxRetries
    );@Transactional
    @Modifying
    int deleteByStatusAndProcessedAtBefore(OutboxStatus status, Instant cutoff);
    
    List<OutboxEventEntity> findByAggregateTypeOrderByCreatedAtAsc(String aggregateType);
}
