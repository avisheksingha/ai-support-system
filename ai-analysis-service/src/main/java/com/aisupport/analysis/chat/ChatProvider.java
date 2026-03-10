package com.aisupport.analysis.chat;

import com.aisupport.analysis.dto.ParsedAnalysis;

/**
 * Strategy interface for AI-powered ticket analysis.
 *
 * Implementations are provided for each supported Chat provider
 * (e.g., Gemini, OpenAI). The active provider is selected at startup
 * via the {@code chat.provider} property — no business logic changes required.
 *
 * Example configuration:
 * chat.provider=gemini → GeminiChatProvider
 * chat.provider=openai → OpenAiChatProvider
 * 
 */
public interface ChatProvider {

    /**
     * Analyze a support ticket and return structured analysis.
     *
     * @param subject the ticket subject
     * @param message the ticket body/message
     * @return a {@link ParsedAnalysis} with intent, sentiment, urgency, etc.
     */
    ParsedAnalysis analyzeTicket(String subject, String message);
}
