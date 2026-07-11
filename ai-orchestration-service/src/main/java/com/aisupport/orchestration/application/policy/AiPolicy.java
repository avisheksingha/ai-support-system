package com.aisupport.orchestration.application.policy;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

public interface AiPolicy {
    PolicyResult evaluate(WorkflowContext context, AgentRequest request);
}
