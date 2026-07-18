package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowMetadataDTO {
    private String workflowExecutionId;
    private String workflowState;
    private Long workflowDurationMs;
}
