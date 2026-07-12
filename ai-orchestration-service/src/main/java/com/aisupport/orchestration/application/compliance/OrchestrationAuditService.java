package com.aisupport.orchestration.application.compliance;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.infrastructure.persistence.entity.OrchestrationAuditLogEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.OrchestrationAuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestrationAuditService {

    private final OrchestrationAuditLogRepository repository;

    public void logStep(Long ticketId, String workflowId, String correlationId, String stepName, String action, String result) {
        log.info("Audit Log - Workflow: {}, Step: {}, Ticket: {}", workflowId, stepName, ticketId);
        
        OrchestrationAuditLogEntity entity = OrchestrationAuditLogEntity.builder()
                .ticketId(ticketId)
                .workflowId(workflowId)
                .correlationId(correlationId)
                .stepName(stepName)
                .action(action)
                .result(result)
                .build();
                
        repository.save(entity);
    }
}
