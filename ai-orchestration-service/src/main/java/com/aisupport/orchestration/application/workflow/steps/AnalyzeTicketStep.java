package com.aisupport.orchestration.application.workflow.steps;

import org.springframework.stereotype.Component;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeTicketStep implements WorkflowStep {

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
                .sessionId(java.util.UUID.randomUUID().toString())
                .initialRequest(request)
                .policyId(e.getPolicyId())
                .policyVersion(e.getPolicyVersion())
                .failureReason(e.getMessage())
                .startedAt(java.time.Instant.now())
                .completedAt(java.time.Instant.now())
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
            context.putAttribute("analyzeResult", "SUCCESS");
            
            // 4. Audit Execution
            auditService.recordExecution(session, context.getCorrelationId(), "v1");
            
            // Extract attributes from agent response
            if (session != null && session.getFinalResponse() != null) {
                // Parse intent, urgency, category if available
                context.putAttribute("agentAnalysis", session.getFinalResponse().getContent());
            }
        } else {
            context.putAttribute("analyzeResult", "FAILED");
            context.putAttribute("analyzeError", agentResult.getErrorMessage());
            log.warn("Analyze step failed with error: {}", agentResult.getErrorMessage());
        }
    }
}

