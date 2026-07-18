package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDataResponse {
    private AIInsightResponse analysis;
    private KnowledgeInsightDTO knowledge;
    private RoutingInsightDTO routing;
    private AiDecisionDTO aiDecision;
    private WorkflowMetadataDTO workflowMetadata;
    private PipelineProgressDTO pipelineProgress;
}
