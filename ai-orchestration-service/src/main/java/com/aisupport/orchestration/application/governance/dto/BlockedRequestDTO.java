package com.aisupport.orchestration.application.governance.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockedRequestDTO {
    private String id;
    private String workflowId;
    private Long ticketId;
    private String guardrail;
    private String reason;
    private String actor;
    private Instant blockedAt;
}
