package com.aisupport.orchestration.application.workflow.steps;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.client.RagClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSearchStep implements WorkflowStep {

    private final RagClient ragClient;

    @Override
    public String getName() {
        return WorkflowStepConstants.KNOWLEDGE_SEARCH;
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Searching Knowledge via internal domain service for Ticket ID: {}", context.getTicketId());
        context.putAttribute("knowledgeSearched", true);
        
        String subject = context.getAttribute("subject") != null ? (String) context.getAttribute("subject") : "";
        String message = context.getAttribute("message") != null ? (String) context.getAttribute("message") : "";
        
        AnalysisResult analysis = context.getResource(AnalysisResult.class);
        
        String query = message;
        if (analysis != null) {
            query = String.format("Issue: %s%nSubject: %s%nKeywords: %s%nIntent: %s", 
                message, 
                subject, 
                String.join(", ", analysis.keywords() != null ? analysis.keywords() : Collections.emptyList()), 
                analysis.intent());
        }
        
        Result<KnowledgeContext> result = ragClient.searchKnowledge(context.getTicketId(), query);
        
        if (result.isSuccess()) {
            KnowledgeContext knowledge = result.getData();
            context.putResource(KnowledgeContext.class, knowledge);
            context.putAttribute("knowledgeContext", knowledge);
            log.info("Knowledge Retrieved KnowledgeFound={} Model={}", knowledge.knowledgeFound(), knowledge.model());
        } else {
            log.error("Failed to search knowledge: {}", result.getErrorMessage());
            KnowledgeContext emptyContext = new KnowledgeContext("No relevant knowledge article found.", false, "Unknown");
            context.putResource(KnowledgeContext.class, emptyContext);
            context.putAttribute("knowledgeContext", emptyContext);
        }
    }
}
