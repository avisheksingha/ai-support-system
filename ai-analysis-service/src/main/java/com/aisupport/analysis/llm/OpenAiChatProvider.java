package com.aisupport.analysis.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import com.aisupport.analysis.dto.response.ParsedAnalysis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

/**
 * OpenAI implementation of the ChatProvider strategy.
 * 
 * Utilizes Resilience4j for rate limiting and circuit breaking to ensure
 * system stability during OpenAI API outages or rate limit breaches.
 */
public class OpenAiChatProvider extends AbstractChatProvider {

    /**
     * Injects the OpenAI-specific ChatClient and the shared PromptTemplate.
     * 
     * @param chatClient The OpenAI ChatClient bean
     * @param ticketAnalysisPromptTemplate The shared prompt template
     */
    public OpenAiChatProvider(ChatClient chatClient,
                              PromptTemplate ticketAnalysisPromptTemplate) {
        super(chatClient, ticketAnalysisPromptTemplate, "OpenAI");
    }

    /**
     * Analyzes a support ticket using OpenAI's chat model.
     * The method is protected by a rate limiter and a circuit breaker to ensure resilience.
     * If the OpenAI service is unavailable or fails, it falls back to a default analysis.
     *
     * @param subject the subject of the support ticket
     * @param message the message content of the support ticket
     * @return a ParsedAnalysis object containing the analysis results
     */
    @RateLimiter(name = "openAiRateLimiter")
    @CircuitBreaker(name = "openAiCircuitBreaker", fallbackMethod = "fallbackAnalysis")
    @Override
    public ParsedAnalysis analyzeTicket(String subject, String message) {
        // Delegates to the shared implementation in AbstractChatProvider
        return super.analyzeTicket(subject, message);
    }
}