package com.aisupport.rag.service.retrieval;

/**
 * Diagnostic metrics and metadata captured during RAG retrieval
 * for observability, governance, and analytics.
 */
public record RetrievalDiagnostics(
    int retrievedDocumentCount,
    boolean fallbackUsed,
    long retrievalLatencyMs,
    boolean categoryMatched,
    boolean keywordMatched,
    boolean tagMatched,
    Double confidenceScore,
    String retrievalStrategy
) {
    public static RetrievalDiagnostics empty() {
        return new RetrievalDiagnostics(0, false, 0L, false, false, false, 0.0, "NONE");
    }
}
