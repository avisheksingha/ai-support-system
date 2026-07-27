package com.aisupport.rag.service.retrieval;

import java.util.List;

import org.springframework.ai.document.Document;

/**
 * Encapsulates the complete result of a metadata-aware retrieval execution,
 * including ranked documents, formatted prompt context, and observability metrics.
 */
public record RetrievalResult(
    List<Document> documents,
    String formattedContext,
    int retrievedDocumentCount,
    String matchedArticleTitles
) {
    public static RetrievalResult empty() {
        return new RetrievalResult(List.of(), "", 0, "");
    }
}
