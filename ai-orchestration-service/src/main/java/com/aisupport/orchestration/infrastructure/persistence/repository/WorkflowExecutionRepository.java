package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, String> {
    Optional<WorkflowExecutionEntity> findByCorrelationId(String correlationId);
    Optional<WorkflowExecutionEntity> findByTicketId(Long ticketId);
    boolean existsByCorrelationIdAndDefinitionIdAndVersion(String correlationId, String definitionId, Integer version);
}
