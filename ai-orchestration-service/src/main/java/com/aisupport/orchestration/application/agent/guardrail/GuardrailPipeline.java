package com.aisupport.orchestration.application.agent.guardrail;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.AgentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuardrailPipeline {
    
    private final List<InputGuardrail> inputGuardrails;
    private final List<OutputGuardrail> outputGuardrails;

    public GuardrailResult<AgentRequest> runInputGuardrails(GuardrailContext<AgentRequest> context) {
        AgentRequest currentRequest = context.getPayload();
        for (InputGuardrail guardrail : inputGuardrails) {
            GuardrailContext<AgentRequest> currentContext = GuardrailContext.<AgentRequest>builder()
                    .payload(currentRequest)
                    .metadata(context.getMetadata())
                    .build();
            GuardrailResult<AgentRequest> result = guardrail.evaluate(currentContext);
            if (result.getStatus() == GuardrailResult.Status.BLOCK) {
                log.warn("Input guardrail blocked execution: {}", result.getReason());
                return result;
            }
            if (result.getStatus() == GuardrailResult.Status.MODIFY) {
                currentRequest = result.getPayload();
            }
        }
        return GuardrailResult.<AgentRequest>builder()
                .status(GuardrailResult.Status.ALLOW)
                .payload(currentRequest)
                .build();
    }

    public GuardrailResult<AgentResponse> runOutputGuardrails(GuardrailContext<AgentResponse> context) {
        AgentResponse currentResponse = context.getPayload();
        for (OutputGuardrail guardrail : outputGuardrails) {
            GuardrailContext<AgentResponse> currentContext = GuardrailContext.<AgentResponse>builder()
                    .payload(currentResponse)
                    .metadata(context.getMetadata())
                    .build();
            GuardrailResult<AgentResponse> result = guardrail.evaluate(currentContext);
            if (result.getStatus() == GuardrailResult.Status.BLOCK) {
                log.warn("Output guardrail blocked execution: {}", result.getReason());
                return result;
            }
            if (result.getStatus() == GuardrailResult.Status.MODIFY) {
                currentResponse = result.getPayload();
            }
        }
        return GuardrailResult.<AgentResponse>builder()
                .status(GuardrailResult.Status.ALLOW)
                .payload(currentResponse)
                .build();
    }
}
