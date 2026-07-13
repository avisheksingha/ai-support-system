package com.aisupport.common.event;

import java.util.List;

public record KnowledgeContext(
    String knowledgeSummary,
    List<String> sources,
    Double confidence
) {}
