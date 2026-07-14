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
            return block(response, "JsonSchemaValidationGuardrail: Output is empty.");
        }

        try {
            // 1. Strip markdown code blocks if the LLM added them
            String cleanJson = stripMarkdown(rawContent);
            
            // 2. Check valid JSON
            JsonNode jsonNode = objectMapper.readTree(cleanJson);
            
            // 3. Check version if present
            if (jsonNode.has("version")) {
                String version = jsonNode.get("version").asText();
                if (!"1.0".equals(version) && !"1.1".equals(version)) {
                    return block(response, "json-schema-guardrail:1.0: Unsupported schema version: " + version);
                }
            }

            // 4. Check responseType
            if (jsonNode.has("responseType")) {
                String responseType = jsonNode.get("responseType").asText();
                if (!isValidResponseType(responseType)) {
                    return block(response, "json-schema-guardrail:1.0: Invalid responseType: " + responseType);
                }
            }

            // If we reach here, it is valid JSON
            return GuardrailResult.<AgentResponse>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(response)
                    .build();

        } catch (Exception e) {
        	return block(response, "JsonSchemaValidationGuardrail: Output is not valid JSON. Error: " + e.getMessage());
        }
    }

    private GuardrailResult<AgentResponse> block(AgentResponse response, String reason) {
        return GuardrailResult.<AgentResponse>builder()
                .status(GuardrailResult.Status.BLOCK)
                .payload(response)
                .reason(reason)
                .build();
    }

    private boolean isValidResponseType(String type) {
        try {
            AgentResponse.ResponseType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Helper to remove ```json ... ``` wrappers
    private String stripMarkdown(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
