package com.aisupport.orchestration.application.agent.guardrail.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailContext;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailResult;
import com.aisupport.orchestration.application.agent.guardrail.InputGuardrail;

@Component
public class PromptSizeValidationGuardrail implements InputGuardrail {

    @Value("${governance.guardrails.prompt.max-size:500000}")
    private int maxPromptSize;
    // TODO: In a future iteration, this should be token-based rather than character-based.

    @Override
    public GuardrailResult<AgentRequest> evaluate(GuardrailContext<AgentRequest> context) {
        AgentRequest request = context.getPayload();
        
        int systemLength = request.getSystemPrompt() != null ? request.getSystemPrompt().length() : 0;
        int userLength = request.getUserPrompt() != null ? request.getUserPrompt().length() : 0;
        int totalLength = systemLength + userLength;

        if (totalLength > maxPromptSize) {
            return GuardrailResult.<AgentRequest>builder()
                    .status(GuardrailResult.Status.BLOCK)
                    .payload(request)
                    .reason("PromptSizeValidationGuardrail: Payload exceeded maximum allowed length of " + maxPromptSize + " characters.")
                    .build();
        }

        return GuardrailResult.<AgentRequest>builder()
                .status(GuardrailResult.Status.ALLOW)
                .payload(request)
                .build();
    }
}
