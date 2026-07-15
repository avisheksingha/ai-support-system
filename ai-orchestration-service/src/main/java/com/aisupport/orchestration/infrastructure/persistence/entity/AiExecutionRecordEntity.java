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
@Table(name = "ai_execution_records")
public class AiExecutionRecordEntity {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "record_type")
    private String recordType; // AGENT or WORKFLOW
    
    @Column(name = "correlation_id")
    private String correlationId;
    
    @Column(name = "workflow_version")
    private String workflowVersion;
    
    @Column(name = "definition_version")
    private String definitionVersion;
    
    @Column(name = "agent_version")
    private String agentVersion;
    
    @Column(name = "prompt_hash")
    private String promptHash;
    
    @Column(name = "model_id")
    private String modelId;
    
    @Column(name = "prompt_tokens")
    private Integer promptTokens;
    
    @Column(name = "completion_tokens")
    private Integer completionTokens;
    
    @Column(name = "finish_reason")
    private String finishReason;
    
    @Column(name = "outcome")
    private String outcome;
    
    @Column(name = "tools_invoked", columnDefinition = "text")
    private String toolsInvoked;
    
    @Column(name = "policy_id")
    private String policyId;
    
    @Column(name = "policy_version")
    private String policyVersion;
    
    @Column(name = "guardrail_id")
    private String guardrailId;
    
    @Column(name = "guardrail_version")
    private String guardrailVersion;
    
    @Column(name = "reason", columnDefinition = "text")
    private String reason;
    
    @Column(name = "latency_ms")
    private Long latencyMs;
    
    @Column(name = "executed_at")
    private Instant executedAt;
    
    @Column(name = "ticket_id")
    private Long ticketId;
    
    @Column(name = "workflow_execution_id")
    private String workflowExecutionId;
    
    @Column(name = "service_version")
    private String serviceVersion;
    
    @Column(name = "workflow_duration_ms")
    private Long workflowDurationMs;
}
