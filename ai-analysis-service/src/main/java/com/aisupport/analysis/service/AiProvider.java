package com.aisupport.analysis.service;

import com.aisupport.analysis.dto.ParsedAnalysis;

/**
 * Strategy interface for AI-powered ticket analysis.
 *
 * <p>Implementations are provided for each supported AI provider
 * (e.g., Gemini, OpenAI). The active provider is selected at startup
 * via the {@code ai.provider} property — no business logic changes required.
 *
 * <pre>
 * ai.provider=gemini   → GeminiService
 * ai.provider=openai   → OpenAiService
 * </pre>
 */
public interface AiProvider {

    /**
     * Analyze a support ticket and return structured analysis.
     *
     * @param subject the ticket subject
     * @param message the ticket body/message
     * @return a {@link ParsedAnalysis} with intent, sentiment, urgency, etc.
     */
    ParsedAnalysis analyzeTicket(String subject, String message);
}
