package com.aisupport.orchestration.application.timeline.dto;

import java.time.Instant;
import java.util.List;

import com.aisupport.common.enums.WorkflowOutcome;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimelineEvent {
    private String eventId;
    private String parentEventId;
    
    private TimelineEventType type;
    private String subType;
    
    private String title;
    private String description;
    private String severity; // INFO, WARNING, ERROR, SUCCESS
    private String processingStage;
    
    private Instant timestamp;
    
    // Execution Details
    private List<String> tools;
    private Long latencyMs;
    private Integer tokens;
    private String workflowVersion;
    private String promptVersion;
    private String model;
    private WorkflowOutcome outcome;
}
