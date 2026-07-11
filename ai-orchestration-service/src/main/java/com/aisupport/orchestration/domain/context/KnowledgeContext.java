package com.aisupport.orchestration.domain.context;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeContext {
    private final List<String> retrievedArticles;
}
