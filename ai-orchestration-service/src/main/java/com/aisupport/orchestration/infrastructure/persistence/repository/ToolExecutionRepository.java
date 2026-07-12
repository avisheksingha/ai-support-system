package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.orchestration.infrastructure.persistence.entity.ToolExecutionEntity;

public interface ToolExecutionRepository extends JpaRepository<ToolExecutionEntity, Long> {
    List<ToolExecutionEntity> findByExecutionIdOrderByExecutedAtAsc(String executionId);
}
