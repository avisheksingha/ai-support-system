package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aisupport.orchestration.infrastructure.persistence.entity.OrchestrationAuditLogEntity;

public interface OrchestrationAuditLogRepository extends JpaRepository<OrchestrationAuditLogEntity, String> {
    List<OrchestrationAuditLogEntity> findByCorrelationId(String correlationId);
    List<OrchestrationAuditLogEntity> findByTicketId(Long ticketId);
}
