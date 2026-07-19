package com.aisupport.common.event;

import java.util.List;

public record KnowledgeContext(
    String knowledgeSummary,
    boolean knowledgeFound,
    String model,
    Integer retrievedDocumentCount,
    List<String> matchedArticleTitles
) {}
