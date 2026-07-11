package com.aisupport.orchestration.application.agent.guardrail;

import com.aisupport.orchestration.application.agent.AgentResponse;

public interface OutputGuardrail {
    GuardrailResult<AgentResponse> evaluate(GuardrailContext<AgentResponse> context);
}
