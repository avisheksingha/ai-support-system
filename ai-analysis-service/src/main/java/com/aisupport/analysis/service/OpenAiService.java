package com.aisupport.analysis.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.exception.AnalysisException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenAiService implements AiProvider {

    private final ChatClient chatClient;

    public OpenAiService(@Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @RateLimiter(name = "openAiRateLimiter")
    @CircuitBreaker(name = "openAiCircuitBreaker", fallbackMethod = "fallbackAnalysis")
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

            normalize(parsed);

            log.info("OpenAI parsed → intent={}, urgency={}, confidence={}",
                    parsed.getIntent(),
                    parsed.getUrgency(),
                    parsed.getConfidenceScore());

            return parsed;

        } catch (Exception e) {
            log.error("Unexpected error during OpenAI analysis", e);
            throw new AnalysisException("OpenAI analysis failed: " + e.getMessage(), e);
        }
    }

    protected ParsedAnalysis fallbackAnalysis(String ignoredSubject, String ignoredMessage, Throwable ex) {

        log.error("OpenAI circuit open or analysis failed - returning fallback analysis", ex);

        return ParsedAnalysis.builder()
                .intent("GENERAL")
                .sentiment("NEUTRAL")
                .urgency("LOW")
                .confidenceScore(0.0)
                .keywords(List.of())
                .suggestedCategory("Fallback")
                .build();
    }

    private void normalize(ParsedAnalysis parsed) {
        if (parsed == null) return;

        if (parsed.getIntent() != null)
            parsed.setIntent(parsed.getIntent().trim().toUpperCase());

        if (parsed.getSentiment() != null)
            parsed.setSentiment(parsed.getSentiment().trim().toUpperCase());

        if (parsed.getUrgency() != null)
            parsed.setUrgency(parsed.getUrgency().trim().toUpperCase());

        if (parsed.getConfidenceScore() != null) {
            double v = parsed.getConfidenceScore();
            parsed.setConfidenceScore(Math.clamp(v, 0.0, 1.0));
        }

        if (parsed.getKeywords() == null)
            parsed.setKeywords(List.of());
    }
}
