package com.aisupport.orchestration.application.policy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PolicyResult {
    private final PolicyDecision decision;
    private final String reason;
    private final String policyId;
    private final String policyVersion;
    
    public static PolicyResult allow() {
        return PolicyResult.builder().decision(PolicyDecision.ALLOW).build();
    }
    
    public static PolicyResult deny(String policyId, String policyVersion, String reason) {
        return PolicyResult.builder().decision(PolicyDecision.DENY).policyId(policyId).policyVersion(policyVersion).reason(reason).build();
    }
    
    public static PolicyResult requireApproval(String policyId, String policyVersion, String reason) {
        return PolicyResult.builder().decision(PolicyDecision.REQUIRE_APPROVAL).policyId(policyId).policyVersion(policyVersion).reason(reason).build();
    }

    public static PolicyResult reroute(String policyId, String policyVersion, String reason) {
        return PolicyResult.builder().decision(PolicyDecision.REROUTE).policyId(policyId).policyVersion(policyVersion).reason(reason).build();
    }

    public boolean isAllowed() {
        return this.decision == PolicyDecision.ALLOW;
    }
}
