package com.aisupport.orchestration.application.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI Recommendation for an agent")
public class AiRecommendationDTO {
    
    @Schema(description = "Ticket number for the recommendation")
    private String ticketNumber;
    
    @Schema(description = "Subject of the ticket")
    private String subject;
    
    @Schema(description = "AI confidence score between 0 and 1")
    private Double confidence;
    
    @Schema(description = "Identified intent")
    private String intent;
    
    @Schema(description = "Suggested action to take")
    private String suggestedAction;
    
    @Schema(description = "Business reason for the recommendation")
    private String businessReason;
}
