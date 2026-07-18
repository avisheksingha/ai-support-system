package com.aisupport.orchestration.application.workflow.steps;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeTicketStep implements WorkflowStep {

    private static final String ATTR_ANALYSIS_RESULT = "analysisResult";
    
    private static final String ATTR_ANALYZE_RESULT = "analyzeResult";

    private final AnalysisClient analysisClient;

    @Override
    public String getName() {
        return WorkflowStepConstants.ANALYZE_TICKET;
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Executing Analyze Ticket step via internal domain service for Ticket ID: {}", context.getTicketId());

        String subject = context.getAttribute("subject") != null ? (String) context.getAttribute("subject") : "";
        String message = context.getAttribute("message") != null ? (String) context.getAttribute("message") : "";

        Result<AnalysisResult> result = analysisClient.analyze(context.getTicketId(), "Subject: " + subject + "\nMessage: " + message);

        if (result.isSuccess()) {
            AnalysisResult analysis = result.getData();
            context.putResource(AnalysisResult.class, analysis);
            context.putAttribute(ATTR_ANALYSIS_RESULT, analysis);
            context.putAttribute(ATTR_ANALYZE_RESULT, "SUCCESS");
            log.info("Analysis completed successfully: Intent={}, Urgency={}", analysis.intent(), analysis.urgency());
        } else {
            log.error("Failed to analyze ticket: {}", result.getErrorMessage());
            context.putAttribute(ATTR_ANALYZE_RESULT, "FAILED");
            context.putAttribute("analyzeError", result.getErrorMessage());
            
            // Fallback for resiliency
            AnalysisResult fallback = new AnalysisResult("GENERAL", "NEUTRAL", "MEDIUM", 0.0, Collections.emptyList(), "Fallback");
            context.putResource(AnalysisResult.class, fallback);
            context.putAttribute(ATTR_ANALYSIS_RESULT, fallback);
        }
    }
}