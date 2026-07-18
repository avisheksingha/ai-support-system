package com.aisupport.orchestration.application.timeline.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeInsightDTO {
    private String knowledgeSummary;
    private Double confidence;
    private List<KnowledgeSourceDTO> sources;
    private boolean knowledgeFound;
}
