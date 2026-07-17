package com.aisupport.orchestration.application.workflow.steps;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.common.event.AiDecision;
import com.aisupport.orchestration.application.agent.Agent;
import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.application.agent.prompt.PromptBuilder;
import com.aisupport.orchestration.application.compliance.AiAuditService;
import com.aisupport.orchestration.application.policy.PolicyEngine;
import com.aisupport.orchestration.application.policy.PolicyViolationException;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinalAiDecisionStep implements WorkflowStep {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PromptBuilder promptBuilder;
    private final PolicyEngine policyEngine;
    private final Agent agent;
    private final AiAuditService auditService;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowCheckpointRepository checkpointRepository;

    @Value("${orchestration.prompt.final.name:final-decision.st}")
    private String promptTemplateName;

    @Override
    public String getName() {
        return WorkflowStepConstants.FINAL_AI_DECISION;
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Executing Final AI Decision step for Ticket ID: {}", context.getTicketId());

        // Build prompt incorporating all intermediate context
        AgentRequest request = promptBuilder.buildRequest(promptTemplateName, context);
        context.putAttribute("promptHash", String.valueOf(request.getSystemPrompt().hashCode()));
        saveCheckpoint(context, WorkflowStepConstants.PROMPT_BUILDER);

        try {
            policyEngine.evaluatePolicies(context, request);
            saveCheckpoint(context, WorkflowStepConstants.GUARDRAILS);
        } catch (PolicyViolationException e) {
            recordPolicyFailure(context, request, e);
            log.error("AI Decision failed due to policy: {}", e.getMessage());
            context.putResource(AiDecision.class, new AiDecision(
                "AI execution blocked by guardrails.",
                "Unable to provide suggestion.",
                0.0
            ));
            return;
        }

        Result<AgentSession> agentResult = agent.execute(request);

        if (agentResult.isSuccess()) {
            AgentSession session = agentResult.getData();
            if (session.getFailureReason() != null) {
                auditService.recordExecution(session, context);
                log.error("AI Guardrail Block: {}", session.getFailureReason());
                context.putResource(AiDecision.class, new AiDecision(
                    "AI execution blocked by output guardrails.",
                    "Unable to provide suggestion.",
                    0.0
                ));
            } else {
                auditService.recordExecution(session, context);
                if (session.getFinalResponse() != null) {
                    extractAndStoreDecision(context, session.getFinalResponse().getContent());
                }
            }
        } else {
            log.warn("Final AI Decision step failed with error: {}", agentResult.getErrorMessage());
            context.putResource(AiDecision.class, new AiDecision(
                "Error generating AI decision.",
                "Error generating suggestion.",
                0.0
            ));
        }
    }

    private void recordPolicyFailure(WorkflowContext context, AgentRequest request, PolicyViolationException e) {
        AgentSession session = AgentSession.builder()
            .sessionId(UUID.randomUUID().toString())
            .initialRequest(request)
            .policyId(e.getPolicyId())
            .policyVersion(e.getPolicyVersion())
            .failureReason(e.getMessage())
            .startedAt(Instant.now())
            .completedAt(Instant.now())
            .build();
        auditService.recordExecution(session, context);
    }

    private void extractAndStoreDecision(WorkflowContext context, String content) {
        try {
            // Strip markdown JSON block if present
            if (content.startsWith("```json")) {
                content = content.substring(7);
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
            }
            JsonNode node = OBJECT_MAPPER.readTree(content.trim());
            AiDecision decision = new AiDecision(
                node.has("aiSummary") ? node.get("aiSummary").asText() : "Summary generated.",
                node.has("suggestedReply") ? node.get("suggestedReply").asText() : "Suggested reply generated.",
                node.has("confidence") ? node.get("confidence").asDouble() : 0.85
            );
            context.putResource(AiDecision.class, decision);
            context.putAttribute("aiDecision", decision);
            log.info("Final AI Decision generated with confidence {}", decision.confidence());
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response for Final Decision, using defaults. Content: {}", content);
            context.putResource(AiDecision.class, new AiDecision("Generated summary.", "Generated reply.", 0.5));
        }
    }

    private void saveCheckpoint(WorkflowContext context, String stepName) {
        executionRepository.findById(context.getExecutionId()).ifPresent(execution -> {
            WorkflowCheckpointEntity checkpoint = 
                WorkflowCheckpointEntity.builder()
                    .execution(execution)
                    .correlationId(context.getCorrelationId())
                    .stepName(stepName)
                    .stateSnapshot(WorkflowState.COMPLETED)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }
}
