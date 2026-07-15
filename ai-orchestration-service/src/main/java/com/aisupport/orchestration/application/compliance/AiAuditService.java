package com.aisupport.orchestration.application.compliance;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.agent.AgentResponse;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditService {

    private final AiExecutionRecordRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordExecution(AgentSession session, String correlationId, String workflowVersion) {
        log.info("Agent Execution Recorded - correlationId={}, modelId={}", correlationId, session.getInitialRequest().getModelProfile().getId());

        String tools = session.getToolInvocations().stream()
                .map(AgentResponse.ToolCallRequest::getToolName)
                .reduce((t1, t2) -> t1 + "," + t2)
                .orElse("");

        AiExecutionRecordEntity entity = AiExecutionRecordEntity.builder()
                .id(session.getSessionId())
                .recordType("AGENT")
                .correlationId(correlationId)
                .workflowVersion(workflowVersion)
                .definitionVersion("1.0") // Intentional V1 placeholder: Should come from execution context
                .agentVersion("1.0") // Intentional V1 placeholder: agent version static for now
                .serviceVersion("1.0.0") // Build version
                .promptHash(String.valueOf(session.getInitialRequest().getSystemPrompt().hashCode()))
                .modelId(session.getInitialRequest().getModelProfile().getId())
                .promptTokens(session.getTotalUsage() != null ? session.getTotalUsage().getPromptTokens() : 0)
                .completionTokens(session.getTotalUsage() != null ? session.getTotalUsage().getCompletionTokens() : 0)
                .finishReason(session.getFinalResponse() != null ? session.getFinalResponse().getFinishReason().name() : "UNKNOWN")
                .outcome(determineOutcome(session))
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

    private String determineOutcome(AgentSession session) {
        if (session.getFinalResponse() != null) {
            return "SUCCESS";
        }
        return session.getFailureReason() != null ? "BLOCKED" : "FAILED";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWorkflowExecution(WorkflowContext context, String outcome, String serviceVersion) {
        log.info("Workflow Audit Recorded - executionId={}", context.getExecutionId());

        AiExecutionRecordEntity entity = AiExecutionRecordEntity.builder()
                .recordType("WORKFLOW")
                .ticketId(context.getTicketId())
                .workflowExecutionId(context.getExecutionId())
                .correlationId(context.getCorrelationId())
                .outcome(outcome)
                .workflowDurationMs(context.getExecutionDuration())
                .serviceVersion(serviceVersion)
                .executedAt(Instant.now())
                .build();

        repository.save(entity);
    }
}
