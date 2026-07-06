package com.aisupport.analysis.chat;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;

import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.exception.AnalysisException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleGenAiChatProvider implements ChatProvider {

    private final ChatClient chatClient;

    public GoogleGenAiChatProvider(@Qualifier("googleGenAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
	 * Analyzes a support ticket using Google GenAI. The method is protected by a rate limiter and a circuit breaker.
	 * If the circuit breaker is open or an error occurs, it falls back to a default analysis.
	 *
	 * @param subject the subject of the support ticket
	 * @param message the message body of the support ticket
	 * @return a ParsedAnalysis containing the results of the analysis
	 */
    @RateLimiter(name = "googleGenAiRateLimiter")
    @CircuitBreaker(name = "googleGenAiCircuitBreaker", fallbackMethod = "fallbackAnalysis")
    @Override
    public ParsedAnalysis analyzeTicket(String subject, String message) {

        try {
            var outputConverter = new BeanOutputConverter<>(ParsedAnalysis.class);

            String promptTemplate = """
                    You are an AI support ticket analyzer.
                    Subject: {subject}
                    Message: {message}

                    {format}
                    """;

            return chatClient.prompt()
                    .user(u -> u.text(promptTemplate)
                            .param("subject", subject)
                            .param("message", message)
                            .param("format", outputConverter.getFormat()))
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            log.error("Unexpected error during Google GenAI analysis", e);
            throw new AnalysisException("Google GenAI analysis failed: " + e.getMessage(), e);
        }
    }

    protected ParsedAnalysis fallbackAnalysis(String subject, String message, Throwable ex) {

        log.error("Google GenAI circuit open or failed - fallback used for ticket subject: '{}'", subject, ex);
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
