package com.aisupport.analysis.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import com.aisupport.analysis.dto.response.ParsedAnalysis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

/**
 * Google GenAI implementation of the ChatProvider strategy.
 * 
 * Utilizes Resilience4j for rate limiting and circuit breaking to ensure
 * system stability during Google GenAI API outages or rate limit breaches.
 */
public class GoogleGenAiChatProvider extends AbstractChatProvider {

    /**
     * Injects the Google GenAI-specific ChatClient and the shared PromptTemplate.
     * Passes them up to the parent class for initialization.
     * 
     * @param chatClient The Google GenAI ChatClient bean
     * @param ticketAnalysisPromptTemplate The shared prompt template
     */
    public GoogleGenAiChatProvider(ChatClient chatClient,
                                   PromptTemplate ticketAnalysisPromptTemplate) {
        super(chatClient, ticketAnalysisPromptTemplate, "Google GenAI");
    }

    /**
     * Analyzes a support ticket using Google GenAI's chat model.
     * The method is protected by a rate limiter and a circuit breaker to ensure resilience.
     * If the Google GenAI service is unavailable or fails, it falls back to a default analysis.
     *
     * @param subject the subject of the support ticket
     * @param message the message body of the support ticket
     * @return a ParsedAnalysis containing the results of the analysis
     */
    @RateLimiter(name = "googleGenAiRateLimiter")
    @CircuitBreaker(name = "googleGenAiCircuitBreaker", fallbackMethod = "fallbackAnalysis")
    @Override
    public ParsedAnalysis analyzeTicket(String subject, String message) {
        // Delegates to the shared implementation in AbstractChatProvider
        return super.analyzeTicket(subject, message);
    }
}