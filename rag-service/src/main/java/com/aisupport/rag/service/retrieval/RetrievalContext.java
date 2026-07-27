package com.aisupport.rag.service.retrieval;

import java.util.List;

/**
 * Structured context extracted from an incoming RAG search query.
 * Acts as the bridge between raw text queries and intelligent metadata-aware retrieval.
 */
public record RetrievalContext(
    String rawQuery,
    String cleanedQuery,
    String suggestedCategory,
    List<String> keywords,
    String intent,
    Double confidenceScore
) {
    public boolean hasCategory() {
        return suggestedCategory != null && !suggestedCategory.isBlank() && !"None".equalsIgnoreCase(suggestedCategory);
    }

    public boolean isHighConfidence() {
        return confidenceScore == null || confidenceScore >= 0.70;
    }
}
