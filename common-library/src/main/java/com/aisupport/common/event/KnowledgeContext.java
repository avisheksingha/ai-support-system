package com.aisupport.common.event;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KnowledgeContext(
    String knowledgeSummary,
    boolean knowledgeFound,
    String model,
    Integer retrievedDocumentCount,
    List<String> matchedArticleTitles
) {}
