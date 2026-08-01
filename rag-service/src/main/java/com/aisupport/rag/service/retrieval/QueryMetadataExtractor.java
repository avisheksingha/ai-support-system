package com.aisupport.rag.service.retrieval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Extracts structured metadata (category, keywords, intent, confidence score)
 * from raw or formatted query strings without altering public DTO contracts.
 */
@Component
public class QueryMetadataExtractor {

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("(?i)Suggested\\s+Category\\s*:\\s*(.*)");
    private static final Pattern KEYWORDS_PATTERN = Pattern.compile("(?i)Keywords\\s*:\\s*(.*)");
    private static final Pattern INTENT_PATTERN = Pattern.compile("(?i)Intent\\s*:\\s*(.*)");
    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("(?i)Confidence(?:\\s+Score)?\\s*:\\s*([0-9.]+)");

    public RetrievalContext extract(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new RetrievalContext("", "", null, Collections.emptyList(), null, null);
        }

        String suggestedCategory = extractSingle(CATEGORY_PATTERN, rawQuery);
        if ("None".equalsIgnoreCase(suggestedCategory)) {
            suggestedCategory = null;
        }

        String keywordsRaw = extractSingle(KEYWORDS_PATTERN, rawQuery);
        List<String> keywords = keywordsRaw != null
                ? Arrays.stream(keywordsRaw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !"none".equalsIgnoreCase(s))
                        .toList()
                : Collections.emptyList();

        String intent = extractSingle(INTENT_PATTERN, rawQuery);
        if ("None".equalsIgnoreCase(intent)) {
            intent = null;
        }

        Double confidenceScore = null;
        String confRaw = extractSingle(CONFIDENCE_PATTERN, rawQuery);
        if (confRaw != null) {
            try {
                confidenceScore = Double.parseDouble(confRaw.trim());
            } catch (NumberFormatException ignored) {
                // Ignore invalid numbers
            }
        }

        String cleanedQuery = buildCleanedQuery(rawQuery);

        return new RetrievalContext(
                rawQuery,
                cleanedQuery,
                suggestedCategory,
                keywords,
                intent,
                confidenceScore
        );
    }

    private String extractSingle(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String val = matcher.group(1).trim();
            return val.isEmpty() ? null : val;
        }
        return null;
    }

    private String buildCleanedQuery(String rawQuery) {
        String[] lines = rawQuery.split("\\r?\\n");
        List<String> cleanedLines = new ArrayList<>();
        boolean isFormatted = false;

        for (String line : lines) {
            if (processAndAddLine(line.trim(), cleanedLines)) {
                isFormatted = true;
            }
        }

        if (!isFormatted || cleanedLines.isEmpty()) {
            return rawQuery.trim();
        }

        return String.join(" ", cleanedLines).trim();
    }

    private boolean processAndAddLine(String trimmed, List<String> cleanedLines) {
        if (trimmed.isEmpty()) {
            return false;
        }
        if (isMetadataControlLine(trimmed)) {
            return true;
        }
        String processed = processLine(trimmed);
        if (!processed.isEmpty()) {
            cleanedLines.add(processed);
        }
        return !processed.equals(trimmed);
    }

    private boolean isMetadataControlLine(String trimmed) {
        return CATEGORY_PATTERN.matcher(trimmed).find() || CONFIDENCE_PATTERN.matcher(trimmed).find();
    }

    private String processLine(String trimmed) {
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("customer message:")) {
            return trimmed.substring("customer message:".length()).trim();
        }
        if (lower.startsWith("subject:")) {
            return trimmed.substring("subject:".length()).trim();
        }
        if (lower.startsWith("issue:")) {
            return trimmed.substring("issue:".length()).trim();
        }
        if (lower.startsWith("keywords:")) {
            String kw = trimmed.substring("keywords:".length()).trim();
            return "none".equalsIgnoreCase(kw) ? "" : kw;
        }
        if (lower.startsWith("intent:")) {
            String intVal = trimmed.substring("intent:".length()).trim();
            return "none".equalsIgnoreCase(intVal) ? "" : intVal;
        }
        return trimmed;
    }
}
