package com.aisupport.orchestration.application.admin.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    
    private PlatformOverviewDTO platformOverview;
    private AiGovernanceDTO aiGovernance;
    private Map<String, Long> departmentWorkload;
    private Map<String, Long> routingOverview;
    private List<SystemHealthDTO> systemHealth;
    private RagKnowledgeDTO ragKnowledge;
    private List<EventDTO> recentEvents;
    private List<ActivityDTO> myActivity;
    private PlatformInfoDTO platformInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformOverviewDTO {
        private long ticketsToday;
        private long activeTickets;
        private long resolvedToday;
        private long aiProcessedToday;
        private long totalCustomers;
        private long totalAgents;
        private long totalAdmins;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiGovernanceDTO {
        private String highConfidenceRate;
        private String assignmentRate;
        private String knowledgeCoverage;
        private String averageLatency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthDTO {
        private String serviceName;
        private String status; // UP, DOWN, DEGRADED
        private String version;
        private String buildVersion;
        private String uptime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RagKnowledgeDTO {
        private long totalArticles;
        private long embeddedArticles;
        private String embeddingCoverage; // e.g. "100%"
        private String knowledgeCoverage; // e.g. "85%"
        private String mostUsedArticle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDTO {
        private String label;
        private String sublabel;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private String label;
        private String time;
        private String color;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformInfoDTO {
        private String platformName;
        private String platformVersion;
        private String buildVersion;
        private String environment;
    }
}
