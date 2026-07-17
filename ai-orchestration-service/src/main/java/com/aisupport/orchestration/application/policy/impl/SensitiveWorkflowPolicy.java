package com.aisupport.orchestration.application.policy.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.policy.AiPolicy;
import com.aisupport.orchestration.application.policy.PolicyResult;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

@Component
public class SensitiveWorkflowPolicy implements AiPolicy {

    @Value("${orchestration.policy.version:1.0}")
    private String policyVersion;

    @Override
    public PolicyResult evaluate(WorkflowContext context, AgentRequest request) {
        // Example: If the context contains high-risk variables (like "delete-user" or "refund-issued")
        // we might require explicit human approval rather than allowing the AI to automatically complete it.
        
        Boolean isHighRisk = (Boolean) context.getAttributes().get("isHighRisk");
        
        if (Boolean.TRUE.equals(isHighRisk)) {
            return PolicyResult.requireApproval("sensitive-workflow-policy", policyVersion, "SensitiveWorkflowPolicy: High risk operation requires human approval.");
        }
        
        return PolicyResult.allow();
    }
}
