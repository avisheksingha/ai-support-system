package com.aisupport.analysis.llm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

import com.aisupport.analysis.dto.response.ParsedAnalysis;
import com.aisupport.analysis.exception.AnalysisException;
import com.aisupport.common.event.SupportIntentVocabulary;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for AI Chat Providers.
 * 
 * Contains the shared logic for rendering prompts, calling the chat client, 
 * and handling fallbacks. Concrete implementations only need to supply the 
 * specific ChatClient bean and apply resilience annotations.
 */
@Slf4j
public abstract class AbstractChatProvider implements ChatProvider {
    
    protected final ChatClient chatClient;
    protected final PromptTemplate ticketAnalysisPromptTemplate;
    private final String providerName;

    /**
     * Constructs the base provider with required dependencies.
     * 
     * @param chatClient The specific ChatClient bean (e.g., OpenAI, Google GenAI)
     * @param ticketAnalysisPromptTemplate The shared template for prompt generation
     * @param providerName The name of the provider (used for logging and exceptions)
     */
    protected AbstractChatProvider(ChatClient chatClient,
                                   PromptTemplate ticketAnalysisPromptTemplate,
                                   String providerName) {
        this.chatClient = chatClient;
        this.ticketAnalysisPromptTemplate = ticketAnalysisPromptTemplate;
        this.providerName = providerName;
    }

    /**
     * Shared implementation for analyzing support tickets.
     * Converts the AI response into a structured ParsedAnalysis object.
     */
    @Override
    public ParsedAnalysis analyzeTicket(String subject, String message) {
        try {
            // Initialize the converter to structure the AI output into a Java object
            BeanOutputConverter<ParsedAnalysis> outputConverter = new BeanOutputConverter<>(ParsedAnalysis.class);

            // Use HashMap instead of Map.of() to safely handle potential null values for subject/message
            Map<String, Object> promptVariables = new HashMap<>();
            promptVariables.put("subject", subject == null ? "" : subject);
            promptVariables.put("message", message == null ? "" : message);
            promptVariables.put("allowedIntents", String.join(", ", SupportIntentVocabulary.ALLOWED_INTENTS));
            promptVariables.put("format", outputConverter.getFormat());

            // Render the prompt with the variables
            String promptContent = ticketAnalysisPromptTemplate.render(promptVariables);

            // Call the chat client and bind the output to the converter
            return chatClient.prompt()
                    .user(promptContent)
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            log.error("Unexpected error during {} analysis", providerName, e);
            throw new AnalysisException(providerName + " analysis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback method invoked when the Circuit Breaker is open or an exception occurs.
     * Returns a safe, default ParsedAnalysis object to keep the application running.
     * 
     * @param subject the original ticket subject
     * @param message the original ticket message
     * @param ex the exception that triggered the fallback
     * @return a default ParsedAnalysis object
     */
    public ParsedAnalysis fallbackAnalysis(String subject, String message, Throwable ex) {

        log.error("{} circuit open or failed - fallback used for ticket subject: '{}'", providerName, subject, ex);
        log.trace("Original ticket message that caused failure: {}", message);

        return ParsedAnalysis.builder()
                .intent("GENERAL")
                .sentiment("NEUTRAL")
                .urgency("LOW")
                .confidenceScore(0.0)
                .keywords(List.of())
                .suggestedCategory("Fallback")
                .build();
    }
}