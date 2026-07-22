package com.aisupport.orchestration.application.agent.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for Agent Dashboard")
public class AgentDashboardResponse {
    
    @Schema(description = "Timestamp when the dashboard was generated")
    private Instant generatedAt;
    
    @Schema(description = "Suggested refresh interval in milliseconds")
    private Long refreshIntervalMs;
    
    @Schema(description = "Version of the dashboard layout")
    private String version;

    @Schema(description = "Agent's profile summary")
    private AgentProfileDTO profile;
    
    @Schema(description = "Top level KPI summary")
    private TopSummaryDTO topSummary;
    
    @Schema(description = "Queue statistics summary")
    private MyQueueSummaryDTO myQueue;
    
    @Schema(description = "SLA risk statistics")
    private SlaAtRiskSummaryDTO slaAtRisk;
    
    @Schema(description = "Metrics for tickets resolved today")
    private ResolvedTodaySummaryDTO resolvedToday;
    
    @Schema(description = "Agent performance metrics")
    private PerformanceSummaryDTO myPerformance;
    
    @Schema(description = "Actionable AI recommendations")
    private List<AiRecommendationDTO> aiRecommendations;
    
    @Schema(description = "Summary of AI activity today")
    private AiActivityTodaySummaryDTO aiActivityToday;
    
    @Schema(description = "AI processing and orchestration metrics")
    private AiProcessingSummaryDTO aiProcessing;
    
    @Schema(description = "Recent activity timeline")
    private List<TimelineEventDTO> recentActivity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent Profile Data")
    public static class AgentProfileDTO {
        @Schema(description = "Display name of the agent")
        private String name;
        @Schema(description = "Assigned team")
        private String team;
        @Schema(description = "Current availability status")
        private String status;
        @Schema(description = "Avatar URL")
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Top Level Summary Data")
    public static class TopSummaryDTO {
        @Schema(description = "Current formatted date")
        private String currentDate;
        @Schema(description = "Current shift")
        private String shift;
        @Schema(description = "Number of tickets assigned today")
        private Long assignedToday;
        @Schema(description = "Current SLA percentage or summary")
        private String currentSla;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Queue Summary Data")
    public static class MyQueueSummaryDTO {
        @Schema(description = "Total assigned tickets")
        private Long assignedTickets;
        @Schema(description = "Critical priority count")
        private Long critical;
        @Schema(description = "High priority count")
        private Long high;
        @Schema(description = "Medium priority count")
        private Long medium;
        @Schema(description = "Low priority count")
        private Long low;
        @Schema(description = "Average wait time formatted string")
        private String averageWaitTime;
        @Schema(description = "Oldest ticket age formatted string")
        private String oldestTicketAge;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "SLA Risk Summary Data")
    public static class SlaAtRiskSummaryDTO {
        @Schema(description = "Tickets nearing SLA breach")
        private Long ticketsNearBreach;
        @Schema(description = "Time until next SLA breach")
        private String nextSlaBreach;
        @Schema(description = "Average remaining SLA time")
        private String averageRemainingTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resolved Today Summary Data")
    public static class ResolvedTodaySummaryDTO {
        @Schema(description = "Total tickets resolved today")
        private Long ticketsResolvedToday;
        @Schema(description = "Average handle time for resolved tickets")
        private String averageHandleTime;
        @Schema(description = "Average first response time")
        private String averageFirstResponseTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent Performance Data")
    public static class PerformanceSummaryDTO {
        @Schema(description = "Customer Satisfaction score")
        private Integer csat;
        @Schema(description = "Resolution rate percentage")
        private Integer resolutionRate;
        @Schema(description = "Quality score")
        private Integer qualityScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AI Activity Summary Data")
    public static class AiActivityTodaySummaryDTO {
        @Schema(description = "Total AI decisions generated")
        private Long aiDecisionsGenerated;
        @Schema(description = "Total suggested replies generated")
        private Long suggestedRepliesGenerated;
        @Schema(description = "Total knowledge searches performed")
        private Long knowledgeSearches;
        @Schema(description = "Average AI confidence score")
        private Double averageAiConfidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AI Processing and Orchestration Metrics")
    public static class AiProcessingSummaryDTO {
        @Schema(description = "Total workflows executed")
        private Long workflowsExecuted;
        @Schema(description = "Average processing duration in milliseconds")
        private Long averageProcessingDurationMs;
        @Schema(description = "Workflow success rate as a percentage")
        private Double successRate;
        @Schema(description = "Primary AI model used")
        private String primaryModel;
    }
}
