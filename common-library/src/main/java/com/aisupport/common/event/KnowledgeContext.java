package com.aisupport.common.event;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KnowledgeContext(
    String knowledgeSummary,
    boolean knowledgeFound,
    String model,
    Integer retrievedDocumentCount,
    List<String> matchedArticleTitles,
    List<Map<String, Object>> sources
) {
    public KnowledgeContext(String knowledgeSummary, boolean knowledgeFound, String model, Integer retrievedDocumentCount, List<String> matchedArticleTitles) {
        this(knowledgeSummary, knowledgeFound, model, retrievedDocumentCount, matchedArticleTitles, null);
    }
}
