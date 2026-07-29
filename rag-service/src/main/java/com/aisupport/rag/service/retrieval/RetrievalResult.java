package com.aisupport.rag.service.retrieval;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.document.Document;

/**
 * Encapsulates the complete result of a metadata-aware retrieval execution,
 * including ranked documents, formatted prompt context, and observability metrics.
 */
public record RetrievalResult(
    List<Document> documents,
    String formattedContext,
    int retrievedDocumentCount,
    String matchedArticleTitles,
    RetrievalDiagnostics diagnostics
) {
    public RetrievalResult(List<Document> documents, String formattedContext, int retrievedDocumentCount, String matchedArticleTitles) {
        this(documents, formattedContext, retrievedDocumentCount, matchedArticleTitles, RetrievalDiagnostics.empty());
    }

    public static RetrievalResult empty() {
        return new RetrievalResult(List.of(), "", 0, "", RetrievalDiagnostics.empty());
    }

    public List<Long> matchedArticleIds() {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        return documents.stream()
                .map(d -> {
                    Object val = d.getMetadata().get("articleId");
                    if (val == null) {
                        val = d.getId();
                    }
                    if (val instanceof Number n) {
                        return n.longValue();
                    }
                    try {
                        return Long.parseLong(val.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
