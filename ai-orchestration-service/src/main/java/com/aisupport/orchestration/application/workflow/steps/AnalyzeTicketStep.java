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
        
        // 1. Build Prompt
        AgentRequest request = promptBuilder.buildRequest("analyze.st", context);
        
        // 2. Evaluate Policies
        try {
            policyEngine.evaluatePolicies(context, request);
        } catch (PolicyViolationException e) {
            AgentSession session = AgentSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .initialRequest(request)
                .policyId(e.getPolicyId())
                .policyVersion(e.getPolicyVersion())
                .failureReason(e.getMessage())
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();
            auditService.recordExecution(session, context.getCorrelationId(), "v1");
            log.error("AI Analysis failed due to policy: {}", e.getMessage());
            context.putAttribute("analyzed", false);
            throw e;
        }
        
        // 3. Execute AI Agent
        Result<AgentSession> agentResult = agent.execute(request);
        
        if (agentResult.isSuccess()) {
            AgentSession session = agentResult.getData();
            if (session.getFailureReason() != null) {
                auditService.recordExecution(session, context.getCorrelationId(), "v1");
                context.putAttribute(ATTR_ANALYZE_RESULT, "FAILED");
                throw new PolicyViolationException("AI Guardrail Block: " + session.getFailureReason(), session.getGuardrailId(), session.getGuardrailVersion());
            }
            context.putAttribute(ATTR_ANALYZE_RESULT, "SUCCESS");
            
            // 4. Audit Execution
            auditService.recordExecution(session, context.getCorrelationId(), "v1");
            
            // Extract attributes from agent response
            if (session != null && session.getFinalResponse() != null) {
                // Parse intent, urgency, category if available
                String content = session.getFinalResponse().getContent();
                context.putAttribute("agentAnalysis", content);
                
                try {
                    // Parse the LLM JSON response
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(content);
                    
                    AnalysisResult analysis = new AnalysisResult(
                        node.has("intent") ? node.get("intent").asText() : "Support",
                        node.has("sentiment") ? node.get("sentiment").asText() : "Neutral",
                        node.has("urgency") ? node.get("urgency").asText() : "Medium"
                    );
                    context.putResource(AnalysisResult.class, analysis);
                } catch (Exception e) {
                    log.warn("Failed to parse LLM JSON response, using defaults.");
                    AnalysisResult analysis = new AnalysisResult("Support", "Neutral", "Medium");
                    context.putResource(AnalysisResult.class, analysis);
                }
            }
        } else {
            context.putAttribute(ATTR_ANALYZE_RESULT, "FAILED");
            context.putAttribute("analyzeError", agentResult.getErrorMessage());
            log.warn("Analyze step failed with error: {}", agentResult.getErrorMessage());
        }
    }
}

