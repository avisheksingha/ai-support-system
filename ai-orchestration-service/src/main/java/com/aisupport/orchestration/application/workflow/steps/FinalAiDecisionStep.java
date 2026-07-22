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

    private static final String FIELD_DECISION_REASON = "decisionReason";
    private static final String FIELD_CONFIDENCE = "confidence";
    private static final String FIELD_AI_SUMMARY = "aiSummary";
    private static final String FIELD_SUGGESTED_REPLY = "suggestedReply";
    private static final String FIELD_INTENT = "intent";
    private static final String FIELD_DOC_COUNT = "retrievedDocumentCount";
    private static final String FIELD_ASSIGN_TO_TEAM = "assignToTeam";
    
    private static final String ATTR_ANALYSIS_RESULT = "analysisResult";
    private static final String ATTR_KNOWLEDGE_CONTEXT = "knowledgeContext";
    private static final String ATTR_ROUTING_DECISION = "routingDecision";
    
    private static final String DEFAULT_INTENT = "support issue";
    private static final String DEFAULT_TEAM = "Technical Support";
    private static final String MSG_UNABLE_TO_PROVIDE_SUGGESTION = "Unable to provide suggestion.";

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
                MSG_UNABLE_TO_PROVIDE_SUGGESTION,
                0.0,
                "Execution blocked by system policies."
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
                    MSG_UNABLE_TO_PROVIDE_SUGGESTION,
                    0.0,
                    "Execution blocked by output guardrails."
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
                0.0,
                "Decision explanation unavailable due to system error."
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
            Double confidence = node.has(FIELD_CONFIDENCE) ? node.get(FIELD_CONFIDENCE).asDouble() : 0.85;
            
            String decisionReason = node.has(FIELD_DECISION_REASON) && !node.get(FIELD_DECISION_REASON).asText().isBlank()
                ? node.get(FIELD_DECISION_REASON).asText()
                : generateDecisionReason(context, confidence);

            AiDecision decision = new AiDecision(
                node.has(FIELD_AI_SUMMARY) ? node.get(FIELD_AI_SUMMARY).asText() : "Summary generated.",
                node.has(FIELD_SUGGESTED_REPLY) ? node.get(FIELD_SUGGESTED_REPLY).asText() : "Suggested reply generated.",
                confidence,
                decisionReason
            );
            context.putResource(AiDecision.class, decision);
            context.putAttribute("aiDecision", decision);
            log.info("Final AI Decision generated with confidence {}", decision.confidence());
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response for Final Decision, using defaults. Content: {}", content);
            String fallbackReason = generateDecisionReason(context, 0.5);
            context.putResource(AiDecision.class, new AiDecision("Generated summary.", "Generated reply.", 0.5, fallbackReason));
        }
    }

    private String generateDecisionReason(WorkflowContext context, Double confidence) {
        String intent = DEFAULT_INTENT;
        int docCount = 0;
        String assignedTeam = DEFAULT_TEAM;

        Object analysisObj = context.getAttribute(ATTR_ANALYSIS_RESULT);
        if (analysisObj instanceof java.util.Map<?, ?> map && map.containsKey(FIELD_INTENT)) {
            intent = java.util.Objects.toString(map.get(FIELD_INTENT), DEFAULT_INTENT).replace("_", " ").toLowerCase();
        }

        Object kcObj = context.getAttribute(ATTR_KNOWLEDGE_CONTEXT);
        if (kcObj instanceof java.util.Map<?, ?> map && map.containsKey(FIELD_DOC_COUNT) && map.get(FIELD_DOC_COUNT) instanceof Number n) {
            docCount = n.intValue();
        }

        Object rdObj = context.getAttribute(ATTR_ROUTING_DECISION);
        if (rdObj instanceof java.util.Map<?, ?> map && map.containsKey(FIELD_ASSIGN_TO_TEAM)) {
            assignedTeam = java.util.Objects.toString(map.get(FIELD_ASSIGN_TO_TEAM), DEFAULT_TEAM);
        }

        double confPct = confidence != null ? confidence * 100 : 85.0;
        return String.format("Detected %s intent with high confidence (%.0f%%). Retrieved %d relevant knowledge article(s). Recommended %s routing.",
            intent, confPct, docCount, assignedTeam);
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
