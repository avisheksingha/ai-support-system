package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSourceDTO {
    private String id;
    private String title;
    private Double similarityScore;
    private String category;
    private String tags;
    private Double vectorScore;
    private Double hybridScore;
}
