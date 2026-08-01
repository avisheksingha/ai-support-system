package com.aisupport.orchestration.application.operations.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Aggregated operations and orchestration metrics")
public class OperationsOverviewDTO {
    @Schema(description = "Runtime execution metrics")
    private RuntimeMetrics runtime;
    
    @Schema(description = "AI and LLM token metrics")
    private AiMetrics ai;
    
    @Schema(description = "Governance and policy metrics")
    private GovernanceMetrics governance;
    
    @Schema(description = "Tool execution metrics")
    private List<ProviderMetrics> tools;
    
    @Schema(description = "System health metrics")
    private HealthMetrics health;
    
    @Schema(description = "System deployment information")
    private SystemInfo systemInfo;

    @Data
    @Builder
    public static class RuntimeMetrics {
        private int activeWorkflows;
        private int completedToday;
        private int failedToday;
        private long averageDurationMs;
        private List<TrendData> hourlyThroughput;
    }

    @Data
    @Builder
    public static class AiMetrics {
        private int averageTotalTokens;
        private int averagePromptTokens;
        private int averageCompletionTokens;
        private long averageLatencyMs;
        private List<TrendData> hourlyTokenTrend;
    }

    @Data
    @Builder
    public static class GovernanceMetrics {
        private int policyViolations;
        private int guardrailBlocks;
        private int approvalRequests;
        private List<TrendData> hourlyViolations;
    }

    @Data
    @Builder
    public static class ProviderMetrics {
        private String providerId;
        private int invocations;
        private int successes;
        private int failures;
        private long avgLatencyMs;
        private String lastInvocation;
    }

    @Data
    @Builder
    public static class HealthMetrics {
        private int circuitBreakersOpen;
        private int retries;
        private int timeouts;
        private int outboxQueueSize;
        private long avgQueueDelayMs;
        private boolean kafkaConnected;
        private boolean databaseConnected;
    }

    @Data
    @Builder
    public static class SystemInfo {
        private String serviceVersion;
        private String gitCommit;
        private String buildNumber;
        private String environment;
    }

    @Data
    @Builder
    public static class TrendData {
        private String label; // e.g., "09:00"
        private int value;
    }
}
