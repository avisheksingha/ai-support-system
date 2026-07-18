package com.aisupport.common.event;

public record KnowledgeContext(
    String knowledgeSummary,
    boolean knowledgeFound,
    String model
) {}
