package com.aisupport.orchestration.application.workflow.steps;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.orchestration.application.agent.Agent;
import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.AgentSession;
import com.aisupport.orchestration.application.agent.prompt.PromptBuilder;
import com.aisupport.orchestration.application.compliance.AiAuditService;
import com.aisupport.orchestration.application.policy.PolicyEngine;
import com.aisupport.orchestration.application.policy.PolicyViolationException;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeTicketStep implements WorkflowStep {

    private static final String ATTR_ANALYZE_RESULT = "analyzeResult";
    private static final String AUDIT_VERSION = "1.0";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PromptBuilder promptBuilder;
    private final PolicyEngine policyEngine;
    private final Agent agent;
    private final AiAuditService auditService;

    @Override
    public String getName() {
        return "Analyze Ticket";
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Executing Analyze Ticket step for Ticket ID: {}", context.getTicketId());

        AgentRequest request = promptBuilder.buildRequest("analyze.st", context);

        evaluatePolicies(context, request);

        Result<AgentSession> agentResult = agent.execute(request);

        if (agentResult.isSuccess()) {
            handleSuccess(context, agentResult.getData());
        } else {
            handleAgentFailure(context, agentResult);
        }
    }

    private void evaluatePolicies(WorkflowContext context, AgentRequest request) {
        try {
            policyEngine.evaluatePolicies(context, request);
        } catch (PolicyViolationException e) {
            recordPolicyFailure(context, request, e);
            context.putAttribute("analyzed", false);
            log.error("AI Analysis failed due to policy: {}", e.getMessage());
            throw e;
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
        auditService.recordExecution(session, context.getCorrelationId(), AUDIT_VERSION);
    }

    private void handleSuccess(WorkflowContext context, AgentSession session) {
        if (session.getFailureReason() != null) {
            auditService.recordExecution(session, context.getCorrelationId(), AUDIT_VERSION);
            context.putAttribute(ATTR_ANALYZE_RESULT, "FAILED");
            throw new PolicyViolationException(
                "AI Guardrail Block: " + session.getFailureReason(),
                session.getGuardrailId(),
                session.getGuardrailVersion());
        }

        context.putAttribute(ATTR_ANALYZE_RESULT, "SUCCESS");
        auditService.recordExecution(session, context.getCorrelationId(), AUDIT_VERSION);

        if (session.getFinalResponse() != null) {
            extractAndStoreAnalysis(context, session.getFinalResponse().getContent());
        }
    }

    private void extractAndStoreAnalysis(WorkflowContext context, String content) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(content);
            AnalysisResult analysis = new AnalysisResult(
                node.has("intent") ? node.get("intent").asText() : "Support",
                node.has("sentiment") ? node.get("sentiment").asText() : "Neutral",
                node.has("urgency") ? node.get("urgency").asText() : "Medium"
            );
            context.putResource(AnalysisResult.class, analysis);
            context.putAttribute("agentAnalysis", analysis); // Store typed object instead of raw string
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response, using defaults.");
            context.putResource(AnalysisResult.class, new AnalysisResult("Support", "Neutral", "Medium"));
        }
    }

    private void handleAgentFailure(WorkflowContext context, Result<AgentSession> agentResult) {
        context.putAttribute(ATTR_ANALYZE_RESULT, "FAILED");
        context.putAttribute("analyzeError", agentResult.getErrorMessage());
        log.warn("Analyze step failed with error: {}", agentResult.getErrorMessage());
    }
}