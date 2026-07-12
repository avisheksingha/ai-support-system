package com.aisupport.orchestration.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orchestration_audit_log")
public class OrchestrationAuditLogEntity {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "correlation_id")
    private String correlationId;
    
    @Column(name = "ticket_id")
    private Long ticketId;
    
    @Column(name = "workflow_id")
    private String workflowId;
    
    @Column(name = "step_name")
    private String stepName;
    
    @Column(name = "action")
    private String action;
    
    @Column(name = "result", columnDefinition = "text")
    private String result;
    
    @Column(name = "executed_at")
    @Builder.Default
    private Instant executedAt = Instant.now();
}
