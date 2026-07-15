package com.aisupport.orchestration.application.workflow.steps;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KnowledgeSearchStep implements WorkflowStep {
    @Override
    public String getName() {
        return "Knowledge Search";
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Searching Knowledge...");
        context.putAttribute("knowledgeSearched", true);
        
        AnalysisResult analysis = context.getResource(AnalysisResult.class);
        String intent = (analysis != null && analysis.intent() != null) ? analysis.intent().toLowerCase() : "unknown";
        
        String summary;
        String source;
        if (intent.contains("billing") || intent.contains("refund")) {
            summary = "Knowledge base suggests processing a pro-rated refund based on usage or checking billing history.";
            source = "KB-201-BILLING";
        } else if (intent.contains("login") || intent.contains("password")) {
            summary = "Knowledge base suggests performing a password reset and checking account lock status.";
            source = "KB-101-AUTH";
        } else if (intent.contains("api") || intent.contains("rate")) {
            summary = "Knowledge base suggests checking the API rate limit tier and suggesting an upgrade.";
            source = "KB-429-API";
        } else if (intent.contains("gdpr") || intent.contains("privacy") || intent.contains("export")) {
            summary = "Knowledge base suggests initiating a standard data export request via the privacy dashboard.";
            source = "KB-500-PRIVACY";
        } else if (intent.contains("subscription") || intent.contains("upgrade")) {
            summary = "Knowledge base suggests redirecting the user to the self-serve subscription portal.";
            source = "KB-301-SUBSCRIPTION";
        } else {
            summary = "Knowledge base suggests escalating to L2 support for further investigation.";
            source = "KB-999-GENERAL";
        }

        KnowledgeContext knowledge = new KnowledgeContext(
            summary,
            Collections.singletonList(source),
            0.85
        );
        context.putResource(KnowledgeContext.class, knowledge);
        log.info("Knowledge Retrieved Sources={} Confidence={}", knowledge.sources().size(), knowledge.confidence());
    }
}
