package com.aisupport.orchestration.application.policy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyEngine {
    
    private final List<AiPolicy> policies;
    
    public void evaluatePolicies(WorkflowContext context, AgentRequest request) {
        for (AiPolicy policy : policies) {
            PolicyResult result = policy.evaluate(context, request);
            if (!result.isAllowed()) {
                log.warn("Policy violation detected: {} (v{}) - {}", result.getPolicyId(), result.getPolicyVersion(), result.getReason());
                throw new PolicyViolationException(result.getPolicyId(), result.getPolicyVersion(), result.getReason());
            }
        }
    }
}
