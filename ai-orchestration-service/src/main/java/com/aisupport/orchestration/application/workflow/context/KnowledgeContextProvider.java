package com.aisupport.orchestration.application.workflow.context;

import java.util.ArrayList;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.context.KnowledgeContext;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

@Component
@Order(20)
public class KnowledgeContextProvider implements ContextProvider {

    @Override
    public boolean supports(String workflowId) {
        return "analyze-workflow".equals(workflowId);
    }

    @Override
    public void populate(WorkflowContext context) {
        // Will fetch from RAG service Client
        KnowledgeContext knowledgeContext = KnowledgeContext.builder()
                .retrievedArticles(new ArrayList<>())
                .build();
        context.putResource(KnowledgeContext.class, knowledgeContext);
    }
}
