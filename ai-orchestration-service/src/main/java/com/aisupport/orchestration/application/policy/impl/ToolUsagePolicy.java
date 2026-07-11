package com.aisupport.orchestration.application.policy.impl;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.policy.AiPolicy;
import com.aisupport.orchestration.application.policy.PolicyResult;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

@Component
public class ToolUsagePolicy implements AiPolicy {

    @Override
    public PolicyResult evaluate(WorkflowContext context, AgentRequest request) {
        // If this is a public ticket workflow, we might not want to allow postgres querying
        if ("public-ticket-workflow".equals(context.getWorkflowId())) {
            
            // Note: Since AgentRequest contains requested tools, we can check if it explicitly requests restricted tools
            // However, tool usage is generally handled by which tools are injected into the context. 
            // The policy provides a second layer of defense.
            
            // For this generic policy, let's just say any workflow explicitly marked as "restricted" 
            // cannot use these tools.
            return PolicyResult.deny("tool-usage-policy", "1.0", "ToolUsagePolicy: Database tools are blocked for public workflows.");
        }
        
        return PolicyResult.allow();
    }
}

