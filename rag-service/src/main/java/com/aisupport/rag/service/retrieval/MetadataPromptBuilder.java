package com.aisupport.rag.service.retrieval;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

/**
 * Constructs a structured metadata prompt context string from retrieved knowledge documents.
 * Replaces raw text injection with rich headers (Category, Tags, Title, Content)
 * to maximize LLM grounding and precision.
 */
@Component
public class MetadataPromptBuilder {

    private static final String UNKNOWN_TITLE = "Unknown";
    private static final String DEFAULT_CATEGORY = "General";

    public RetrievalResult build(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return RetrievalResult.empty();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String category = extractMeta(doc, "category", DEFAULT_CATEGORY);
            String tags = extractMeta(doc, "tags", "None");
            String title = extractMeta(doc, "title", UNKNOWN_TITLE);
            String rawText = doc.getText();
            String content = rawText != null ? rawText.trim() : "";

            sb.append("--- Knowledge Article ").append(i + 1).append(" ---\n");
            sb.append("Category: ").append(category).append("\n");
            sb.append("Tags: ").append(tags).append("\n");
            sb.append("Title: ").append(title).append("\n");
            sb.append("Content: ").append(content).append("\n");
            sb.append("---------------------------\n");
            if (i < documents.size() - 1) {
                sb.append("\n");
            }
        }

        List<String> matchedTitles = documents.stream()
                .map(d -> extractMeta(d, "title", UNKNOWN_TITLE))
                .filter(t -> !UNKNOWN_TITLE.equals(t))
                .distinct()
                .toList();

        String titlesStr = String.join(",", matchedTitles);

        return new RetrievalResult(
                documents,
                sb.toString(),
                documents.size(),
                titlesStr
        );
    }

    private String extractMeta(Document doc, String key, String defaultValue) {
        Object val = doc.getMetadata().get(key);
        if (val == null || val.toString().isBlank()) {
            return defaultValue;
        }
        return val.toString().trim();
    }
}
