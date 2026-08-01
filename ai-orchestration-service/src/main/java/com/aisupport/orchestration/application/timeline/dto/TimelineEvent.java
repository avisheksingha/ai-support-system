package com.aisupport.orchestration.application.timeline.dto;

import java.time.Instant;
import java.util.List;

import com.aisupport.common.enums.WorkflowOutcome;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "A single event in the orchestration timeline")
public class TimelineEvent {
    @Schema(description = "Unique ID of the timeline event")
    private String eventId;
    
    @Schema(description = "Parent event ID if this is a sub-event")
    private String parentEventId;
    
    @Schema(description = "Type of the event")
    private TimelineEventType type;
    
    @Schema(description = "Specific subtype of the event")
    private String subType;
    
    @Schema(description = "Title of the event")
    private String title;
    
    @Schema(description = "Detailed description")
    private String description;
    
    @Schema(description = "Severity level: INFO, WARNING, ERROR, SUCCESS", example = "INFO")
    private String severity;
    
    @Schema(description = "The processing stage this event belongs to")
    private String processingStage;
    
    @Schema(description = "Timestamp when the event occurred")
    private Instant timestamp;
    
    // Execution Details
    @Schema(description = "Tools invoked during this event")
    private List<String> tools;
    
    @Schema(description = "Latency of the operation in milliseconds")
    private Long latencyMs;
    
    @Schema(description = "Token usage if applicable")
    private Integer tokens;
    
    @Schema(description = "Workflow version")
    private String workflowVersion;
    
    @Schema(description = "Prompt version")
    private String promptVersion;
    
    @Schema(description = "Model ID used")
    private String model;
    
    @Schema(description = "Outcome of the operation")
    private WorkflowOutcome outcome;
}
