package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineProgressDTO {
    private boolean analysisCompleted;
    private boolean knowledgeCompleted;
    private boolean routingCompleted;
    private boolean decisionCompleted;
}
