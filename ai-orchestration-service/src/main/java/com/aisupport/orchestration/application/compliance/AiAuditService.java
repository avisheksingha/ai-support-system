package com.aisupport.orchestration.application.compliance;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.application.agent.AgentResponse;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditService {

    private final AiExecutionRecordRepository repository;

    public void recordExecution(AgentSession session, String correlationId, String workflowVersion) {
        log.info("Recording AI Execution for session: {}", session.getSessionId());

        String tools = session.getToolInvocations().stream()
                .map(AgentResponse.ToolCallRequest::getToolName)
                .reduce((t1, t2) -> t1 + "," + t2)
                .orElse("");

        AiExecutionRecordEntity entity = AiExecutionRecordEntity.builder()
                .id(session.getSessionId())
                .correlationId(correlationId)
                .workflowVersion(workflowVersion)
                .definitionVersion("v1.0") // Should come from execution context, static for now
                .agentVersion("v1.0")
                .promptHash(String.valueOf(session.getInitialRequest().getSystemPrompt().hashCode()))
                .modelId(session.getInitialRequest().getModelProfile().getId())
                .promptTokens(session.getTotalUsage() != null ? session.getTotalUsage().getPromptTokens() : 0)
                .completionTokens(session.getTotalUsage() != null ? session.getTotalUsage().getCompletionTokens() : 0)
                .finishReason(session.getFinalResponse() != null ? session.getFinalResponse().getFinishReason().name() : "UNKNOWN")
                .outcome(session.getFinalResponse() != null ? "SUCCESS" : (session.getFailureReason() != null ? "BLOCKED" : "FAILED"))
                .toolsInvoked(tools)
                .policyId(session.getPolicyId())
                .policyVersion(session.getPolicyVersion())
                .guardrailId(session.getGuardrailId())
                .guardrailVersion(session.getGuardrailVersion())
                .reason(session.getFailureReason())
                .latencyMs(Duration.between(session.getStartedAt(), session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now()).toMillis())
                .executedAt(session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now())
                .build();

        repository.save(entity);
    }
}
