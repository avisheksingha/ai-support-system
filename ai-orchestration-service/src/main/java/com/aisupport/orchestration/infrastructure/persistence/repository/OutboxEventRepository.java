package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.orchestration.infrastructure.persistence.entity.OutboxEventEntity;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {
    List<OutboxEventEntity> findByAggregateTypeOrderByCreatedAtAsc(String aggregateType);
}
