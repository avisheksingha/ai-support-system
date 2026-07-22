package com.aisupport.orchestration.application.compliance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.AiDecision;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
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
	
	private static final String TEMPLATE_NAME_KEY = "templateName";
	private static final String UNKNOWN = "Unknown";
	private static final String UNKNOWN_OUTCOME = "UNKNOWN";
	private static final String RECORD_TYPE_AGENT = "AGENT";

    @Value("${info.app.version:1.0.0}")
    private String serviceVersion;

    @Value("${orchestration.prompt.version:1.0}")
    private String promptVersion;

    @Value("${orchestration.agent.version:1.0}")
    private String agentVersion;

    @Value("${orchestration.workflow.version:1.0}")
    private String workflowVersion;

    private final AiExecutionRecordRepository repository;

    @Value("${orchestration.prompt.final.name:final-decision.st}")
    private String finalDecisionTemplateName;

    @Value("${orchestration.prompt.analyze.name:analyze.st}")
    private String analyzeTemplateName;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordExecution(AgentSession session, WorkflowContext context) {
        log.info("Agent Execution Recorded - correlationId={}, modelId={}",
        		context.getCorrelationId(),
        		session.getInitialRequest().getModelProfile().getId());

        // Extract complex logic into helper methods passing the session directly
        String templateName = extractTemplateName(session);
        String tools = buildToolsString(session, templateName);
        String finishReason = extractFinishReason(session);
        String reason = extractReason(session, templateName);
        
        Instant completedAt = session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now();
        long latencyMs = Duration.between(session.getStartedAt(), completedAt).toMillis();

        AiExecutionRecordEntity entity = AiExecutionRecordEntity.builder()
                .id(session.getSessionId())
                .recordType(RECORD_TYPE_AGENT)
                .correlationId(context.getCorrelationId())
                .workflowExecutionId(context.getExecutionId())
                .ticketId(context.getTicketId())
                .workflowVersion(String.valueOf(context.getWorkflowVersion()))
                .definitionVersion(workflowVersion) 
                .agentVersion(agentVersion) 
                .serviceVersion(serviceVersion) 
                .promptTemplate(templateName)
                .promptVersion(session.getInitialRequest().getPromptVersion() != null ? session.getInitialRequest().getPromptVersion() : promptVersion)
                .promptHash(String.valueOf(session.getInitialRequest().getSystemPrompt().hashCode()))
                .modelId(session.getInitialRequest().getModelProfile().getId())
                .promptTokens(session.getTotalUsage() != null ? session.getTotalUsage().getPromptTokens() : 0)
                .completionTokens(session.getTotalUsage() != null ? session.getTotalUsage().getCompletionTokens() : 0)
                .finishReason(finishReason)
                .outcome(determineOutcome(session))
                .toolsInvoked(tools)
                .policyId(session.getPolicyId())
                .policyVersion(session.getPolicyVersion())
                .guardrailId(session.getGuardrailId())
                .guardrailVersion(session.getGuardrailVersion())
                .reason(reason)
                .latencyMs(latencyMs)
                .executedAt(completedAt)
                .build();

        repository.save(entity);
    }

    // Helper method to determine outcome
    private String determineOutcome(AgentSession session) {
        if (session.getFinalResponse() != null) {
            return "SUCCESS";
        }
        return session.getFailureReason() != null ? "BLOCKED" : "FAILED";
    }
	
	
	 // Helper method to extract template name cleanly
	 private String extractTemplateName(AgentSession session) {
	     Map<String, Object> metadata = session.getInitialRequest().getMetadata();
	     if (metadata != null && metadata.containsKey(TEMPLATE_NAME_KEY)) {
	         return (String) metadata.get(TEMPLATE_NAME_KEY);
	     }
	     return UNKNOWN;
	 }
	
	 // Helper method to resolve tools invoked (fixes the nested ternary Sonar issue)
	 private String buildToolsString(AgentSession session, String templateName) {
	     if (session.getToolInvocations().isEmpty()) {
	         if (templateName.equals(analyzeTemplateName)) {
	             return "[\"Analyze\"]";
	         } else if (templateName.equals(finalDecisionTemplateName)) {
	             return "[\"FinalDecision\"]";
	         } else {
	             return "[]";
	         }
	     }
	     
	     return "[" + session.getToolInvocations().stream()
	             .map(t -> "\"" + t.getToolName() + "\"")
	             .reduce((t1, t2) -> t1 + "," + t2)
	             .orElse("") + "]";
	 }
	
	 // Helper method to safely extract finish reason
	 private String extractFinishReason(AgentSession session) { 
	     if (session.getFinalResponse() != null && session.getFinalResponse().getFinishReason() != null) {
	         return session.getFinalResponse().getFinishReason().name();
	     }
	     return UNKNOWN_OUTCOME;
	 }
	
	 // Helper method to safely extract reason
	 private String extractReason(AgentSession session, String templateName) {
	     if (session.getFailureReason() != null) {
	         return session.getFailureReason();
	     }
	     if (!UNKNOWN.equals(templateName)) {
	         return "Executing template: " + templateName;
	     }
	     return null;
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
                .toolsInvoked(deriveToolsInvoked(context))
                .executedAt(Instant.now())
                .build();

        repository.save(entity);
    }

    private String deriveToolsInvoked(WorkflowContext context) {
        List<String> tools = new ArrayList<>();
        if (context.getResource(AnalysisResult.class) != null) tools.add("Analysis");
        if (context.getResource(KnowledgeContext.class) != null) tools.add("RAG");
        if (context.getResource(RoutingDecision.class) != null) tools.add("Routing");
        if (context.getResource(AiDecision.class) != null) tools.add("Final_AI_Decision");
        return String.join(",", tools);
    }
}
