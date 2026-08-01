package com.aisupport.orchestration.application.governance.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogDTO {
    private String id;
    private Instant timestamp;
    private String workflowId;
    private String policyEvaluated;
    private String decision;
    private Long durationMs;
    private String actor;
}
