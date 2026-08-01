package com.aisupport.orchestration.application.agent.guardrail;

import com.aisupport.orchestration.application.agent.AgentRequest;

public interface InputGuardrail {
    GuardrailResult<AgentRequest> evaluate(GuardrailContext<AgentRequest> context);
}
