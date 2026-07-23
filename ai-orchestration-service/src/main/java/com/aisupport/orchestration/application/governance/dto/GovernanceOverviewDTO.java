package com.aisupport.orchestration.application.governance.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GovernanceOverviewDTO {
    private Integer policyViolations;
    private Integer guardrailBlocks;
    private Integer approvalRequests;
    private Long avgEvaluationTimeMs;
    private List<TrendData> hourlyViolations;

    @Data
    @Builder
    public static class TrendData {
        private String label;
        private Integer value;
    }
}
