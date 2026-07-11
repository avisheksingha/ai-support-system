package com.aisupport.orchestration.application.workflow.context;

import java.util.ArrayList;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.context.ConversationContext;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

@Component
@Order(10)
public class ConversationContextProvider implements ContextProvider {

    @Override
    public boolean supports(String workflowId) {
        return true; // Supports all workflows currently
    }

    @Override
    public void populate(WorkflowContext context) {
        // Will fetch from ConversationEventRepository
        ConversationContext conversationContext = ConversationContext.builder()
                .eventHistory(new ArrayList<>())
                .build();
        context.putResource(ConversationContext.class, conversationContext);
    }
}
