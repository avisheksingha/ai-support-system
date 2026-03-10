package com.aisupport.rag.embedding;

import java.util.List;

/**
 * Strategy interface for AI-powered ticket analysis.
 *
 * Implementations are provided for each supported Embedding provider
 * (e.g., Gemini, OpenAI). The active provider is selected at startup
 * via the {@code embedding.provider} property — no business logic changes required.
 *
 * Example configuration:
 * embedding.provider=gemini → GeminiEmbeddingProvider
 * embedding.provider=openai → OpenAiEmbeddingProvider
 * 
 */
public interface EmbeddingProvider {

	List<Float> embed(String text);

}
