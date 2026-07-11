package com.aisupport.orchestration.application.agent.guardrail;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuardrailContext<T> {
    private final T payload;
    private final Map<String, Object> metadata;
}
