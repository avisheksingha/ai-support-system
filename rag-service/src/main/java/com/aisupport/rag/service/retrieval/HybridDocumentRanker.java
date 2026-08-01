package com.aisupport.rag.service.retrieval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Ranks candidate documents using a configurable hybrid scoring model:
 * Total Score = (Vector Similarity * W_vector) + (Tag Match * W_tags) + (Category Match * W_category) + (Title Match * W_title).
 *
 * Treats tags as a first-class ranking signal alongside category and vector similarity.
 */
@Slf4j
@Component
public class HybridDocumentRanker {

    private final double vectorWeight;
    private final double tagsWeight;
    private final double categoryWeight;
    private final double titleWeight;

    public HybridDocumentRanker(
            @Value("${rag.ranking.weights.vector:0.50}") double vectorWeight,
            @Value("${rag.ranking.weights.tags:0.20}") double tagsWeight,
            @Value("${rag.ranking.weights.category:0.15}") double categoryWeight,
            @Value("${rag.ranking.weights.title:0.15}") double titleWeight) {
        this.vectorWeight = vectorWeight;
        this.tagsWeight = tagsWeight;
        this.categoryWeight = categoryWeight;
        this.titleWeight = titleWeight;
    }

    public List<Document> rankAndSelectTopK(List<Document> candidates, RetrievalContext context, int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<String> searchTerms = getSearchTerms(context);

        List<DocumentScore> scoredDocs = new ArrayList<>();
        for (Document doc : candidates) {
            double vScore = doc.getScore() != null ? doc.getScore() : 0.50;
            double cScore = calculateCategoryScore(doc, context);
            double tagScore = calculateTagScore(doc, searchTerms);
            double tScore = calculateTitleScore(doc, searchTerms);

            double total = (vScore * vectorWeight) + (tagScore * tagsWeight) + (cScore * categoryWeight) + (tScore * titleWeight);

            Map<String, Object> newMeta = new HashMap<>(doc.getMetadata());
            newMeta.put("hybridScore", total);
            newMeta.put("vectorScore", vScore);
            newMeta.put("tagScore", tagScore);
            newMeta.put("categoryScore", cScore);
            
            String reason = String.format("Vector: %.2f, Category: %.2f, Tags: %.2f, Title: %.2f", vScore, cScore, tagScore, tScore);
            newMeta.put("rankingReason", reason);

            List<String> matchedWords = searchTerms.stream()
                    .filter(term -> {
                        String t = (doc.getMetadata().get("title") != null ? doc.getMetadata().get("title").toString() : "").toLowerCase();
                        String tg = (doc.getMetadata().get("tags") != null ? doc.getMetadata().get("tags").toString() : "").toLowerCase();
                        return t.contains(term.toLowerCase()) || tg.contains(term.toLowerCase());
                    })
                    .toList();
            if (!matchedWords.isEmpty()) {
                newMeta.put("matchedKeywords", String.join(", ", matchedWords));
            }

            Document updatedDoc = Document.builder()
                    .id(doc.getId())
                    .text(doc.getText())
                    .metadata(newMeta)
                    .score(total)
                    .build();

            scoredDocs.add(new DocumentScore(updatedDoc, total));
        }

        scoredDocs.sort(Comparator.comparingDouble(DocumentScore::score).reversed());

        int limit = Math.min(topK, scoredDocs.size());
        List<Document> result = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Document d = scoredDocs.get(i).document();
            log.debug("Ranked Doc ID={}, title='{}', hybridScore={}", d.getId(), d.getMetadata().get("title"), d.getScore());
            result.add(d);
        }

        return result;
    }

    private double calculateCategoryScore(Document doc, RetrievalContext context) {
        if (!context.hasCategory()) {
            return 0.0;
        }
        Object catObj = doc.getMetadata().get("category");
        if (catObj != null && context.suggestedCategory().equalsIgnoreCase(catObj.toString().trim())) {
            return 1.0;
        }
        return 0.0;
    }

    private double calculateTagScore(Document doc, List<String> searchTerms) {
        if (searchTerms.isEmpty()) {
            return 0.0;
        }
        Object tagsObj = doc.getMetadata().get("tags");
        if (tagsObj == null || tagsObj.toString().isBlank()) {
            return 0.0;
        }

        Set<String> docTags = Arrays.stream(tagsObj.toString().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (docTags.isEmpty()) {
            return 0.0;
        }

        long matches = searchTerms.stream()
                .filter(term -> docTags.stream().anyMatch(tag -> tag.contains(term) || term.contains(tag)))
                .count();

        return Math.min(1.0, (double) matches / Math.max(1, searchTerms.size()));
    }

    private double calculateTitleScore(Document doc, List<String> searchTerms) {
        if (searchTerms.isEmpty()) {
            return 0.0;
        }
        Object titleObj = doc.getMetadata().get("title");
        if (titleObj == null) {
            return 0.0;
        }
        String titleLower = titleObj.toString().toLowerCase();

        long matches = searchTerms.stream()
                .filter(titleLower::contains)
                .count();

        return Math.min(1.0, (double) matches / Math.max(1, searchTerms.size()));
    }

    private List<String> getSearchTerms(RetrievalContext context) {
        if (context.keywords() != null && !context.keywords().isEmpty()) {
            return context.keywords().stream()
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .filter(s -> s.length() >= 2)
                    .toList();
        }

        if (context.cleanedQuery() != null && !context.cleanedQuery().isBlank()) {
            return Arrays.stream(context.cleanedQuery().split("\\W+"))
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .filter(s -> s.length() >= 3)
                    .distinct()
                    .toList();
        }

        return List.of();
    }

    private record DocumentScore(Document document, double score) {}
}
