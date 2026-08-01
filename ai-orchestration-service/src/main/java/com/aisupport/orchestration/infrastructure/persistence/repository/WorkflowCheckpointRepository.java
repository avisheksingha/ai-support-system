package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;

public interface WorkflowCheckpointRepository extends JpaRepository<WorkflowCheckpointEntity, Long> {
    List<WorkflowCheckpointEntity> findByExecutionIdOrderByCreatedAtDesc(String executionId);
}
