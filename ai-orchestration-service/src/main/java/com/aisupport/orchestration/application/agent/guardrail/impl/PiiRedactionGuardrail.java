package com.aisupport.orchestration.application.agent.guardrail.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailContext;
import com.aisupport.orchestration.application.agent.guardrail.GuardrailResult;
import com.aisupport.orchestration.application.agent.guardrail.InputGuardrail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PiiRedactionGuardrail implements InputGuardrail {

    @Value("${governance.guardrails.pii.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${governance.guardrails.pii.ssn.enabled:true}")
    private boolean ssnEnabled;
    
    @Value("${governance.guardrails.pii.credit-card.enabled:true}")
    private boolean creditCardEnabled;

    private static final String EMAIL_REGEX = "[a-zA-Z0-9._%+-]++@[a-zA-Z0-9.-]++\\.[a-zA-Z]{2,}";
    private static final String SSN_REGEX = "\\b\\d{3}-\\d{2}-\\d{4}\\b";
    private static final String CC_REGEX = "\\b(?:\\d[ -]*?){13,16}+\\b";

    @Override
    public GuardrailResult<AgentRequest> evaluate(GuardrailContext<AgentRequest> context) {
        AgentRequest request = context.getPayload();
        String originalPrompt = request.getUserPrompt();
        
        if (originalPrompt == null || originalPrompt.isEmpty()) {
            return GuardrailResult.<AgentRequest>builder()
                    .status(GuardrailResult.Status.ALLOW)
                    .payload(request)
                    .build();
        }

        String redacted = redact(originalPrompt);

        if (!originalPrompt.equals(redacted)) {
            // Modify the request with the redacted prompt
            AgentRequest modifiedRequest = AgentRequest.builder()
                    .systemPrompt(request.getSystemPrompt())
                    .userPrompt(redacted)
                    .modelProfile(request.getModelProfile())
                    .allowedCapabilities(request.getAllowedCapabilities())
                    .build();
                    
            return GuardrailResult.<AgentRequest>builder()
                    .status(GuardrailResult.Status.MODIFY)
                    .payload(modifiedRequest)
                    .reason("PiiRedactionGuardrail: Redacted sensitive PII patterns (Email/SSN/CC).")
                    .build();
        }

        return GuardrailResult.<AgentRequest>builder()
                .status(GuardrailResult.Status.ALLOW)
                .payload(request)
                .build();
    }

    private String redact(String input) {
        String redacted = input;

        if (emailEnabled) redacted = redactPattern(redacted, EMAIL_REGEX, "[REDACTED EMAIL]");
        if (ssnEnabled) redacted = redactPattern(redacted, SSN_REGEX, "[REDACTED SSN]");
        if (creditCardEnabled) redacted = redactPattern(redacted, CC_REGEX, "[REDACTED CC]");

        return redacted;
    }

    private String redactPattern(String input, String regex, String replacement) {
        return Pattern.compile(regex).matcher(input).replaceAll(replacement);
    }
}
