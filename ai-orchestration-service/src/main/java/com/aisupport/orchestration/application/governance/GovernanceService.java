package com.aisupport.orchestration.application.governance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.governance.dto.ActiveGuardrailDTO;
import com.aisupport.orchestration.application.governance.dto.ApprovalRequestDTO;
import com.aisupport.orchestration.application.governance.dto.AuditLogDTO;
import com.aisupport.orchestration.application.governance.dto.BlockedRequestDTO;
import com.aisupport.orchestration.application.governance.dto.GovernanceOverviewDTO;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;

    private static final String WAITING_APPROVAL_STATE = "WAITING_APPROVAL";
    private static final String BLOCKED_OUTCOME = "BLOCKED";
    private static final String ENFORCING_STATUS = "Enforcing";

    @Transactional(readOnly = true)
    public CompletableFuture<GovernanceOverviewDTO> getOverview() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<WorkflowExecutionEntity> workflows = workflowExecutionRepository.findAll();
                List<AiExecutionRecordEntity> records = aiExecutionRecordRepository.findAll();

                int approvalRequests = (int) workflows.stream()
                        .filter(w -> WAITING_APPROVAL_STATE.equals(w.getState().name()))
                        .count();

                int guardrailBlocks = (int) records.stream()
                        .filter(r -> BLOCKED_OUTCOME.equals(r.getOutcome()))
                        .count();

                int policyViolations = (int) records.stream()
                        .filter(r -> "POLICY_VIOLATION".equals(r.getOutcome()))
                        .count();
                        
                long totalLatency = 0;
                int countWithLatency = 0;
                for (AiExecutionRecordEntity r : records) {
                    if (r.getLatencyMs() != null) {
                        totalLatency += r.getLatencyMs();
                        countWithLatency++;
                    }
                }
                long avgTime = countWithLatency > 0 ? totalLatency / countWithLatency : 0;

                return GovernanceOverviewDTO.builder()
                        .policyViolations(policyViolations + guardrailBlocks) // Simplified aggregation
                        .guardrailBlocks(guardrailBlocks)
                        .approvalRequests(approvalRequests)
                        .avgEvaluationTimeMs(avgTime)
                        .hourlyViolations(Collections.emptyList()) // Future enhancement
                        .build();
            } catch (Exception e) {
                log.error("Failed to fetch governance overview metrics", e);
                return GovernanceOverviewDTO.builder()
                        .policyViolations(0).guardrailBlocks(0).approvalRequests(0).avgEvaluationTimeMs(0L)
                        .hourlyViolations(Collections.emptyList())
                        .build();
            }
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<ApprovalRequestDTO>> getApprovalQueue() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return workflowExecutionRepository.findAll().stream()
                        .filter(w -> WAITING_APPROVAL_STATE.equals(w.getState().name()))
                        .map(this::mapToApprovalRequest)
                        .sorted(Comparator.comparing(ApprovalRequestDTO::getCreatedAt).reversed())
                        .toList();
            } catch (Exception e) {
                log.error("Failed to fetch approval queue", e);
                return Collections.emptyList();
            }
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<BlockedRequestDTO>> getBlockedRequests() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return aiExecutionRecordRepository.findAll().stream()
                        .filter(r -> BLOCKED_OUTCOME.equals(r.getOutcome()))
                        .map(this::mapToBlockedRequest)
                        .sorted(Comparator.comparing(BlockedRequestDTO::getBlockedAt).reversed())
                        .toList();
            } catch (Exception e) {
                log.error("Failed to fetch blocked requests", e);
                return Collections.emptyList();
            }
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<AuditLogDTO>> getAuditLogs() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return aiExecutionRecordRepository.findAll().stream()
                        .filter(r -> r.getGuardrailId() != null || r.getPolicyId() != null || BLOCKED_OUTCOME.equals(r.getOutcome()))
                        .map(this::mapToAuditLog)
                        .sorted(Comparator.comparing(AuditLogDTO::getTimestamp).reversed())
                        .limit(100) // limit for UI
                        .toList();
            } catch (Exception e) {
                log.error("Failed to fetch audit logs", e);
                return Collections.emptyList();
            }
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<ActiveGuardrailDTO>> getActiveGuardrails() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // In a full implementation, this would query the Policy Engine.
                // For Phase 1, we map existing implemented guardrails and count hits from DB.
                List<AiExecutionRecordEntity> records = aiExecutionRecordRepository.findAll();
                
                int piiHits = (int) records.stream().filter(r -> "PiiRedactionGuardrail".equals(r.getGuardrailId())).count();
                int promptSizeHits = (int) records.stream().filter(r -> "PromptSizeValidationGuardrail".equals(r.getGuardrailId())).count();
                
                List<ActiveGuardrailDTO> guardrails = new ArrayList<>();
                guardrails.add(ActiveGuardrailDTO.builder().name("PII Redaction").type("Security").status(ENFORCING_STATUS).count(piiHits).build());
                guardrails.add(ActiveGuardrailDTO.builder().name("Prompt Size Validation").type("Security").status(ENFORCING_STATUS).count(promptSizeHits).build());
                guardrails.add(ActiveGuardrailDTO.builder().name("Manual Approval Required").type("Workflow").status(ENFORCING_STATUS).count(
                        (int) workflowExecutionRepository.findAll().stream().filter(w -> WAITING_APPROVAL_STATE.equals(w.getState().name())).count()
                ).build());
                
                return guardrails;
            } catch (Exception e) {
                log.error("Failed to fetch active guardrails", e);
                return Collections.emptyList();
            }
        });
    }

    private ApprovalRequestDTO mapToApprovalRequest(WorkflowExecutionEntity entity) {
        String intent = "Unknown";
        Double confidence = 0.0;
        
        if (entity.getTicketContext() != null && entity.getTicketContext().getAnalysisResult() != null) {
            intent = entity.getTicketContext().getAnalysisResult().intent();
            confidence = entity.getTicketContext().getAnalysisResult().confidenceScore();
        }
        
        return ApprovalRequestDTO.builder()
                .id("APR-" + entity.getId().substring(0, 8))
                .workflowId(entity.getId())
                .correlationId(entity.getCorrelationId())
                .ticketId(entity.getTicketId())
                .intent(intent != null ? intent : "Unknown")
                .confidence(confidence != null ? confidence : 0.0)
                .triggeredPolicy("Manual Approval Policy")
                .reason("Requires human verification.")
                .recommendedAction("Review and approve")
                .status("PENDING")
                .createdAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt())
                .build();
    }

    private BlockedRequestDTO mapToBlockedRequest(AiExecutionRecordEntity entity) {
        return BlockedRequestDTO.builder()
                .id("BLK-" + entity.getId().substring(0, 8))
                .workflowId(entity.getWorkflowExecutionId())
                .ticketId(entity.getTicketId())
                .guardrail(entity.getGuardrailId() != null ? entity.getGuardrailId() : "Unknown Guardrail")
                .reason(entity.getReason() != null ? entity.getReason() : "Blocked by security policy")
                .actor("SYSTEM")
                .blockedAt(entity.getExecutedAt())
                .build();
    }

    private AuditLogDTO mapToAuditLog(AiExecutionRecordEntity entity) {
        String decision = "ALLOWED";
        if (BLOCKED_OUTCOME.equals(entity.getOutcome())) {
            decision = BLOCKED_OUTCOME;
        }
        
        String policy = "System Policy";
        if (entity.getGuardrailId() != null) {
            policy = entity.getGuardrailId();
        } else if (entity.getPolicyId() != null) {
            policy = entity.getPolicyId();
        }
                       
        return AuditLogDTO.builder()
                .id("AUD-" + entity.getId().substring(0, 8))
                .timestamp(entity.getExecutedAt())
                .workflowId(entity.getWorkflowExecutionId())
                .policyEvaluated(policy)
                .decision(decision)
                .durationMs(entity.getLatencyMs() != null ? entity.getLatencyMs() : 0)
                .actor("SYSTEM")
                .build();
    }
}
