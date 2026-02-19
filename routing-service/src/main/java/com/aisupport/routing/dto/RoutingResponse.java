package com.aisupport.routing.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingResponse {
    
    private Long ticketId;
    private String ticketNumber;
    private String assignedTo;
    private String priority;
    private Integer slaHours;
    
    // Analysis details
    private String intent;
    private String sentiment;
    private String urgency;
    
    // Rule details
    private String matchedRule;
    private String routingReason;
    
    // Workflow metadata
    private Boolean success;
    private String errorMessage;
    private Long processingTimeMs;
    private LocalDateTime routedAt;
}