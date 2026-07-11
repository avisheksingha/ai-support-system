package com.aisupport.orchestration.application.agent.guardrail.impl;

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
public class JsonSchemaValidationGuardrail implements OutputGuardrail {

    private final ObjectMapper objectMapper;

    @Override
    public GuardrailResult<AgentResponse> evaluate(GuardrailContext<AgentResponse> context) {
        AgentResponse response = context.getPayload();
        
        // If it's a tool call response, skip output JSON validation (it's internal format)
        if (response.isToolCall()) {
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();
        }

        String rawContent = response.getContent();
        if (rawContent == null || rawContent.isEmpty()) {
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.BLOCK)
                    .payload(response)
                    .reason("JsonSchemaValidationGuardrail: Output is empty.")
                    .build();
        }

        try {
            // Check valid JSON
            JsonNode jsonNode = objectMapper.readTree(rawContent);
            
            // Check version if present (simple check)
            if (jsonNode.has("version")) {
                String version = jsonNode.get("version").asText();
                if (!"1.0".equals(version) && !"1.1".equals(version)) {
                    return GuardrailResult.<AgentResponse>builder()
                            .status(GuardrailResult.Status.BLOCK)
                            .payload(response)
                            .reason("json-schema-guardrail:1.0: Unsupported schema version: " + version)
                            .build();
                }
            }

            // Check responseType
            if (jsonNode.has("responseType")) {
                String responseType = jsonNode.get("responseType").asText();
                if (!isValidResponseType(responseType)) {
                    return GuardrailResult.<AgentResponse>builder()
                            .status(GuardrailResult.Status.BLOCK)
                            .payload(response)
                            .reason("json-schema-guardrail:1.0: Invalid responseType: " + responseType)
                            .build();
                }
            }

            // In a real enterprise system, we would validate against a strict JSON Schema definition here.
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();

        } catch (Exception e) {
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.BLOCK)
                    .payload(response)
                    .reason("JsonSchemaValidationGuardrail: Output is not valid JSON. Error: " + e.getMessage())
                    .build();
        }
    }

    private boolean isValidResponseType(String type) {
        try {
            AgentResponse.ResponseType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
