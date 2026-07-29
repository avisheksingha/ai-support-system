package com.aisupport.rag.service.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.aisupport.rag.service.retrieval.RetrievalDiagnostics;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dedicated serializer responsible for converting document metadata and retrieval diagnostics
 * into structured JSON strings for persistence and event transport.
 * Keeps core orchestration services focused on workflow logic rather than JSON formatting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SourceMetadataSerializer {

    private final ObjectMapper objectMapper;

    /**
     * Serializes a list of retrieved documents into a rich JSON array representation.
     * Captures enhanced metadata including scores, ranking reasons, and matched keywords.
     */
    public String serializeSources(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, Object>> list = documents.stream().map(d -> {
                Map<String, Object> map = new HashMap<>();
                Object articleIdObj = d.getMetadata().getOrDefault("articleId", d.getId());
                map.put("id", String.valueOf(articleIdObj));
                map.put("articleId", String.valueOf(articleIdObj));
                map.put("title", String.valueOf(d.getMetadata().getOrDefault("title", "Unknown")));
                map.put("category", String.valueOf(d.getMetadata().getOrDefault("category", "General")));
                map.put("tags", String.valueOf(d.getMetadata().getOrDefault("tags", "None")));
                
                double fallbackScore = d.getScore() != null ? d.getScore() : 0.0;
                map.put("similarityScore", fallbackScore);
                map.put("vectorScore", d.getMetadata().get("vectorScore") instanceof Number n ? n.doubleValue() : fallbackScore);
                map.put("hybridScore", d.getMetadata().get("hybridScore") instanceof Number n ? n.doubleValue() : fallbackScore);
                
                if (d.getMetadata().containsKey("chunkIndex")) {
                    map.put("chunkIndex", d.getMetadata().get("chunkIndex"));
                }
                if (d.getMetadata().containsKey("matchedKeywords")) {
                    map.put("matchedKeywords", d.getMetadata().get("matchedKeywords"));
                }
                if (d.getMetadata().containsKey("rankingReason")) {
                    map.put("rankingReason", d.getMetadata().get("rankingReason"));
                }
                return map;
            }).toList();
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Failed to serialize document sources to JSON", e);
            return null;
        }
    }

    /**
     * Serializes retrieval diagnostics into a JSON string representation.
     */
    public String serializeDiagnostics(RetrievalDiagnostics diagnostics) {
        if (diagnostics == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(diagnostics);
        } catch (Exception e) {
            log.warn("Failed to serialize retrieval diagnostics to JSON", e);
            return null;
        }
    }
}
