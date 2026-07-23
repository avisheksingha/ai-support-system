package com.aisupport.orchestration.application.governance.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalRequestDTO {
    private String id;
    private String workflowId;
    private String correlationId;
    private Long ticketId;
    private String intent;
    private Double confidence;
    private String triggeredPolicy;
    private String reason;
    private String recommendedAction;
    private String status; 
    private Instant createdAt;
}
