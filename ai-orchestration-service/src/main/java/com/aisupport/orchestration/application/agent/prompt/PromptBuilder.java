package com.aisupport.orchestration.application.agent.prompt;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

public interface PromptBuilder {
    AgentRequest buildRequest(String templateName, WorkflowContext context);
}
