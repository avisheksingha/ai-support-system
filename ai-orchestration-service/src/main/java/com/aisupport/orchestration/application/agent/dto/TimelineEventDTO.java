package com.aisupport.orchestration.application.agent.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Timeline event representation")
public class TimelineEventDTO {
    
    @Schema(description = "Unique ID of the event")
    private String id;
    
    @Schema(description = "Type of the event")
    private String eventType;
    
    @Schema(description = "Associated ticket number")
    private String ticketNumber;
    
    @Schema(description = "Event occurrence timestamp")
    private Instant timestamp;
    
    @Schema(description = "Description of the event")
    private String description;
    
    @Schema(description = "Source of the event")
    private String source;
}
