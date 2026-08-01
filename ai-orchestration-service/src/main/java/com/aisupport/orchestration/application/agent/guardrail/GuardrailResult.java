package com.aisupport.orchestration.application.agent.guardrail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuardrailResult<T> {
    private final Status status;
    private final T payload;
    private final String reason;

    public enum Status {
        ALLOW,
        MODIFY,
        BLOCK,
        WARN
    }
}
