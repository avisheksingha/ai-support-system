package com.aisupport.common.event;

public record KnowledgeSource(
    String id,
    String title,
    Double similarityScore
) {}
