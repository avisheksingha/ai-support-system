package com.aisupport.rag.service.retrieval;

import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Responsible for constructing the final system prompt by combining the base prompt template
 * with the structured metadata-aware retrieval context.
 * Separates prompt assembly concerns from workflow orchestration.
 */
@Component
@RequiredArgsConstructor
public class RagPromptFactory {

    private static final String NO_KNOWLEDGE_FOUND = "No relevant knowledge article found.";
    private final PromptTemplate ragSystemPromptTemplate;

    public String buildFullSystemPrompt(RetrievalResult retrievalResult) {
        String baseSystemPrompt = ragSystemPromptTemplate.render(
                Map.of("noKnowledgeFound", NO_KNOWLEDGE_FOUND));

        String contextContent = (retrievalResult == null || retrievalResult.formattedContext() == null || retrievalResult.formattedContext().isEmpty())
                ? NO_KNOWLEDGE_FOUND
                : retrievalResult.formattedContext();

        return baseSystemPrompt + "\n\nRetrieved Knowledge Base Context:\n" + contextContent;
    }
}
