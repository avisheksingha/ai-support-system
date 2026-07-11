package com.aisupport.orchestration.application.agent.prompt.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.application.agent.prompt.PromptBuilder;
import com.aisupport.orchestration.application.agent.prompt.PromptRenderer;
import com.aisupport.orchestration.domain.context.ConversationContext;
import com.aisupport.orchestration.domain.context.KnowledgeContext;
import com.aisupport.orchestration.domain.model.ModelProfile;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptBuilderImpl implements PromptBuilder {

    @Value("classpath:prompts/analyze.st")
    private Resource analyzeTemplate;

    private final PromptRenderer promptRenderer;

    @Override
    public AgentRequest buildRequest(String templateName, WorkflowContext context) {
        String templateContent = loadTemplate(templateName);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("ticketId", context.getTicketId() != null ? context.getTicketId().toString() : "unknown");

        String systemPrompt = promptRenderer.render(templateContent, variables);

        ConversationContext conversation = context.getResource(ConversationContext.class);
        KnowledgeContext knowledge = context.getResource(KnowledgeContext.class);
        
        ModelProfile modelProfile = ModelProfile.builder()
                .id("gemini-2.5-flash")
                .provider("google")
                .name("gemini-2.5-flash")
                .maxContextTokens(1048576)
                .maxOutputTokens(8192)
                .defaultTemperature(0.7)
                .supportsToolCalling(true)
                .supportsStreaming(true)
                .supportsVision(true)
                .supportsStructuredOutput(true)
                .build();

        return AgentRequest.builder()
                .promptVersion("v1.0.0")
                .systemPrompt(systemPrompt)
                .userPrompt("Analyze the current state of this ticket.")
                .conversation(conversation)
                .knowledge(knowledge)
                .modelProfile(modelProfile)
                .parameters(new HashMap<>())
                .metadata(new HashMap<>())
                .build();
    }

    private String loadTemplate(String templateName) {
        try {
            if ("analyze.st".equals(templateName)) {
                return StreamUtils.copyToString(analyzeTemplate.getInputStream(), StandardCharsets.UTF_8);
            }
            return "Default system prompt for {ticketId}";
        } catch (IOException e) {
            log.error("Failed to load prompt template: {}", templateName, e);
            return "Error loading template.";
        }
    }
}
