package com.aisupport.orchestration.application.policy;

public class PolicyViolationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String policyId;
    private final String policyVersion;

    public PolicyViolationException(String policyId, String policyVersion, String message) {
        super(message);
        this.policyId = policyId;
        this.policyVersion = policyVersion;
    }

    public String getPolicyId() {
        return policyId;
    }
    
    public String getPolicyVersion() {
        return policyVersion;
    }
}
