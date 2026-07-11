package com.aisupport.orchestration.application.agent.guardrail.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentResponse;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailContext;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailResult;
import com.aisupport.orchestration.application.agent.guardrail.OutputGuardrail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequiredFieldsValidationGuardrail implements OutputGuardrail {

    private final ObjectMapper objectMapper;
    private static final List<String> REQUIRED_FIELDS = List.of("recommendation");

    @Override
    public GuardrailResult<AgentResponse> evaluate(GuardrailContext<AgentResponse> context) {
        AgentResponse response = context.getPayload();
        
        if (response.isToolCall()) {
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getContent());
            
            for (String field : REQUIRED_FIELDS) {
                if (!jsonNode.has(field) || jsonNode.get(field).isNull()) {
                    return GuardrailResult.<AgentResponse>builder()
                            .status(GuardrailResult.Status.BLOCK)
                            .payload(response)
                            .reason("RequiredFieldsValidationGuardrail: Missing required field '" + field + "'.")
                            .build();
                }
            }

            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();
        } catch (Exception e) {
            // If it can't be parsed, it will be caught by JsonSchemaValidationGuardrail anyway
            // but we can pass it or block it here safely.
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();
        }
    }
}
