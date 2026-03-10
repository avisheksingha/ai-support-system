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
public class GeminiChatProvider implements ChatProvider {

    private final ChatClient chatClient;

    public GeminiChatProvider(@Qualifier("geminiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
	 * Analyzes a support ticket using Gemini. The method is protected by a rate limiter and a circuit breaker.
	 * If the circuit breaker is open or an error occurs, it falls back to a default analysis.
	 *
	 * @param subject the subject of the support ticket
	 * @param message the message body of the support ticket
	 * @return a ParsedAnalysis containing the results of the analysis
	 */
    @RateLimiter(name = "geminiRateLimiter")
    @CircuitBreaker(name = "geminiCircuitBreaker", fallbackMethod = "fallbackAnalysis")
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

            ParsedAnalysis parsed = chatClient.prompt()
                    .user(u -> u.text(promptTemplate)
                            .param("subject", subject)
                            .param("message", message)
                            .param("format", outputConverter.getFormat()))
                    .call()
                    .entity(outputConverter);
            
            log.info("Gemini raw → sentiment={}, intent={}, urgency={}, confidence={}",
            		parsed.getSentiment(),
                    parsed.getIntent(),
                    parsed.getUrgency(),
                    parsed.getConfidenceScore());

            return parsed;

        } catch (Exception e) {
            log.error("Unexpected error during Gemini analysis", e);
            throw new AnalysisException("Gemini analysis failed: " + e.getMessage(), e);
        }
    }

    protected ParsedAnalysis fallbackAnalysis(String ignoredSubject, String ignoredMessage, Throwable ex) {

        log.error("Gemini circuit open or failed - fallback used", ex);

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