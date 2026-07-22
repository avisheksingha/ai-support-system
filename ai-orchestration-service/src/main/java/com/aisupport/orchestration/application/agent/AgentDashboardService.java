package com.aisupport.orchestration.application.agent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.application.agent.dto.AgentDashboardResponse;
import com.aisupport.orchestration.application.agent.dto.AiRecommendationDTO;
import com.aisupport.orchestration.application.agent.dto.TimelineEventDTO;
import com.aisupport.orchestration.infrastructure.client.ResilientTicketClient;
import com.aisupport.orchestration.infrastructure.client.dto.TicketDashboardSummaryDTO;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDashboardService {

    private final ResilientTicketClient ticketClient;
    private final WorkflowExecutionRepository workflowRepository;

    public AgentDashboardResponse getAgentDashboard(String userEmail, String userName) {
        log.info("Generating dashboard for agent: {}", userEmail);
        
        TicketDashboardSummaryDTO ticketSummary = ticketClient.getAgentSummary(userEmail);
        
        List<WorkflowExecutionEntity> recentWorkflows = workflowRepository.findAll().stream()
                .filter(w -> w.getCompletedAt() != null)
                .sorted((w1, w2) -> w2.getCompletedAt().compareTo(w1.getCompletedAt()))
                .limit(20)
                .toList();

        return AgentDashboardResponse.builder()
                .generatedAt(Instant.now())
                .refreshIntervalMs(60000L)
                .version("v1.0")
                .profile(buildProfile(userName, userEmail))
                .topSummary(buildTopSummary(ticketSummary))
                .myQueue(buildMyQueue(ticketSummary))
                .slaAtRisk(buildSlaAtRisk(ticketSummary))
                .resolvedToday(buildResolvedToday(ticketSummary))
                .myPerformance(buildPerformanceSummary())
                .aiRecommendations(buildAiRecommendations(recentWorkflows))
                .aiActivityToday(buildAiActivityToday(recentWorkflows))
                .aiProcessing(buildAiProcessing(recentWorkflows))
                .recentActivity(buildRecentActivity(recentWorkflows))
                .build();
    }

    private AgentDashboardResponse.AgentProfileDTO buildProfile(String name, String email) {
        String displayName = name != null && !name.isEmpty() ? name : email.split("@")[0];
        // Capitalize first letter
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        
        return AgentDashboardResponse.AgentProfileDTO.builder()
                .name(displayName)
                .team("Tier 1 Support")
                .status("Online")
                .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + email)
                .build();
    }

    private AgentDashboardResponse.TopSummaryDTO buildTopSummary(TicketDashboardSummaryDTO summary) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());
        String currentSla = summary.getAverageRemainingSlaMins() != null 
                ? (summary.getAverageRemainingSlaMins() / 60) + "h " + (summary.getAverageRemainingSlaMins() % 60) + "m"
                : "No active SLAs";
                
        return AgentDashboardResponse.TopSummaryDTO.builder()
                .currentDate(dtf.format(Instant.now()))
                .shift("Morning Shift") // Mock for now
                .assignedToday(summary.getAssignedToday())
                .currentSla(currentSla)
                .build();
    }

    private AgentDashboardResponse.MyQueueSummaryDTO buildMyQueue(TicketDashboardSummaryDTO summary) {
        String waitTime = summary.getAverageWaitTimeMins() != null ? summary.getAverageWaitTimeMins() + "m" : "--";
        String oldest = summary.getOldestTicketAgeMins() != null ? (summary.getOldestTicketAgeMins() / 60) + "h" : "--";
        
        return AgentDashboardResponse.MyQueueSummaryDTO.builder()
                .assignedTickets(summary.getTotalAssigned())
                .critical(summary.getCritical())
                .high(summary.getHigh())
                .medium(summary.getMedium())
                .low(summary.getLow())
                .averageWaitTime(waitTime)
                .oldestTicketAge(oldest)
                .build();
    }

    private AgentDashboardResponse.SlaAtRiskSummaryDTO buildSlaAtRisk(TicketDashboardSummaryDTO summary) {
        String nextBreach = summary.getNextSlaBreachMins() != null ? summary.getNextSlaBreachMins() + "m" : "--";
        String avgRemaining = summary.getAverageRemainingSlaMins() != null 
                ? (summary.getAverageRemainingSlaMins() / 60) + "h " + (summary.getAverageRemainingSlaMins() % 60) + "m" 
                : "--";
                
        return AgentDashboardResponse.SlaAtRiskSummaryDTO.builder()
                .ticketsNearBreach(summary.getNearSlaBreach())
                .nextSlaBreach(nextBreach)
                .averageRemainingTime(avgRemaining)
                .build();
    }

    private AgentDashboardResponse.ResolvedTodaySummaryDTO buildResolvedToday(TicketDashboardSummaryDTO summary) {
        String avgHandle = summary.getAverageHandleTimeMins() != null ? summary.getAverageHandleTimeMins() + "m" : "--";
        String avgFirstResp = summary.getAverageFirstResponseTimeMins() != null ? summary.getAverageFirstResponseTimeMins() + "m" : "--";
        
        return AgentDashboardResponse.ResolvedTodaySummaryDTO.builder()
                .ticketsResolvedToday(summary.getResolvedToday())
                .averageHandleTime(avgHandle)
                .averageFirstResponseTime(avgFirstResp)
                .build();
    }

    private AgentDashboardResponse.PerformanceSummaryDTO buildPerformanceSummary() {
        // Return null so the frontend displays "Performance metrics unavailable" gracefully
        return null;
    }

    private List<AiRecommendationDTO> buildAiRecommendations(List<WorkflowExecutionEntity> workflows) {
        List<AiRecommendationDTO> recommendations = new ArrayList<>();
        
        for (WorkflowExecutionEntity workflow : workflows) {
            if (workflow.getAttributes() != null && workflow.getAttributes().containsKey("aiDecision")) {
                Object intent = workflow.getAttributes().get("intent");
                Object confidence = workflow.getAttributes().get("aiConfidence");
                Object suggestedReply = workflow.getAttributes().get("suggestedReply");
                Object ticketNumberObj = workflow.getAttributes().get("ticketNumber");
                
                String ticketNumber = ticketNumberObj != null ? ticketNumberObj.toString() : "TKT-" + workflow.getTicketId();
                
                recommendations.add(AiRecommendationDTO.builder()
                        .ticketNumber(ticketNumber)
                        .subject(workflow.getAttributes().get("subject") != null ? workflow.getAttributes().get("subject").toString() : "Unknown Ticket")
                        .confidence(confidence instanceof Number ? ((Number) confidence).doubleValue() : 0.85)
                        .intent(intent != null ? intent.toString() : "Unknown")
                        .suggestedAction("Draft Reply Available")
                        .businessReason(suggestedReply != null ? suggestedReply.toString() : "AI has analyzed this ticket and generated a recommendation.")
                        .build());
            }
            if (recommendations.size() >= 3) break; // Limit to top 3 recommendations
        }
        
        return recommendations;
    }

    private AgentDashboardResponse.AiActivityTodaySummaryDTO buildAiActivityToday(List<WorkflowExecutionEntity> workflows) {
        long decisions = workflows.stream().filter(w -> w.getAttributes() != null && w.getAttributes().containsKey("aiDecision")).count();
        long drafts = workflows.stream().filter(w -> w.getAttributes() != null && w.getAttributes().containsKey("suggestedReply")).count();
        long searches = workflows.stream().filter(w -> w.getAttributes() != null && w.getAttributes().containsKey("knowledgeFound")).count();
        
        double avgConfidence = workflows.stream()
                .filter(w -> w.getAttributes() != null && w.getAttributes().get("aiConfidence") instanceof Number)
                .mapToDouble(w -> ((Number) w.getAttributes().get("aiConfidence")).doubleValue())
                .average()
                .orElse(0.0);
        
        return AgentDashboardResponse.AiActivityTodaySummaryDTO.builder()
                .aiDecisionsGenerated(decisions)
                .suggestedRepliesGenerated(drafts)
                .knowledgeSearches(searches)
                .averageAiConfidence(avgConfidence > 0 ? (double) Math.round(avgConfidence * 100) : null)
                .build();
    }
    
    private AgentDashboardResponse.AiProcessingSummaryDTO buildAiProcessing(List<WorkflowExecutionEntity> workflows) {
        if (workflows.isEmpty()) {
            return null;
        }
        
        long totalExecuted = workflows.size();
        
        long totalDurationMs = workflows.stream()
                .filter(w -> w.getCreatedAt() != null && w.getCompletedAt() != null)
                .mapToLong(w -> w.getCompletedAt().toEpochMilli() - w.getCreatedAt().toEpochMilli())
                .sum();
                
        long avgDuration = totalExecuted > 0 ? totalDurationMs / totalExecuted : 0;
        
        long successful = workflows.stream()
                .filter(w -> w.getState() != null && "COMPLETED".equals(w.getState().name()))
                .count();
                
        double successRate = totalExecuted > 0 ? ((double) successful / totalExecuted) * 100 : 0.0;
        
        return AgentDashboardResponse.AiProcessingSummaryDTO.builder()
                .workflowsExecuted(totalExecuted)
                .averageProcessingDurationMs(avgDuration)
                .successRate((double) Math.round(successRate * 10) / 10)
                .primaryModel("gemini-2.5-flash")
                .build();
    }
    
    private List<TimelineEventDTO> buildRecentActivity(List<WorkflowExecutionEntity> workflows) {
        return workflows.stream()
                .limit(5)
                .map(w -> {
                    String ticketNumber = w.getAttributes() != null && w.getAttributes().get("ticketNumber") != null 
                            ? w.getAttributes().get("ticketNumber").toString() 
                            : "TKT-" + w.getTicketId();
                            
                    return TimelineEventDTO.builder()
                            .id(w.getId())
                            .ticketNumber(ticketNumber)
                            .eventType("AI_WORKFLOW_COMPLETED")
                            .timestamp(w.getCompletedAt() != null ? w.getCompletedAt() : w.getCreatedAt())
                            .description("AI Workflow completed for " + ticketNumber)
                            .source("orchestration-service")
                            .build();
                })
                .collect(Collectors.toList());
    }
}
