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

    @Value("${orchestration.prompt.analyze.path:classpath:prompts/analyze.st}")
    private Resource analyzeTemplate;

    @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}")
    private String chatModel;
    
    @Value("${chat.provider:google-genai}")
    private String chatProvider;

    @Value("${spring.ai.google.genai.chat.temperature:0.5}")
    private Double chatTemperature;

    @Value("${spring.ai.google.genai.chat.max-output-tokens:8192}")
    private Integer chatMaxOutputTokens;

    @Value("${chat.max-context-tokens:1048576}")
    private Integer chatMaxContextTokens;

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
                .id(chatModel)
                .provider(chatProvider)
                .name(chatModel)
                .maxContextTokens(chatMaxContextTokens)
                .maxOutputTokens(chatMaxOutputTokens)
                .defaultTemperature(chatTemperature)
                .supportsToolCalling(true)
                .supportsStreaming(true)
                .supportsVision(true)
                .supportsStructuredOutput(true)
                .build();

        String subject = context.getAttribute("subject") != null ? (String) context.getAttribute("subject") : "";
        String message = context.getAttribute("message") != null ? (String) context.getAttribute("message") : "";
        String userPrompt = "Analyze the current state of this ticket.\nSubject: " + subject + "\nMessage: " + message;

        return AgentRequest.builder()
                .promptVersion("1.0")
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
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
