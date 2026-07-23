package com.aisupport.orchestration.application.admin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.aisupport.common.dto.admin.AdminAnalysisStatsResponse;
import com.aisupport.common.dto.admin.AdminAuthStatsResponse;
import com.aisupport.common.dto.admin.AdminRagStatsResponse;
import com.aisupport.common.dto.admin.AdminTicketStatsResponse;
import com.aisupport.orchestration.application.admin.dto.AdminDashboardResponse;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;
import com.aisupport.orchestration.infrastructure.client.AuthClient;
import com.aisupport.orchestration.infrastructure.client.RagClient;
import com.aisupport.orchestration.infrastructure.client.SystemHealthClient;
import com.aisupport.orchestration.infrastructure.client.TicketClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final TicketClient ticketClient;
    private final AuthClient authClient;
    private final RagClient ragClient;
    private final SystemHealthClient healthClient;
    private final AnalysisClient analysisClient;

    public AdminDashboardResponse getDashboard(String userEmail) {
        log.info("Aggregating admin dashboard stats for {}", userEmail);

        CompletableFuture<AdminTicketStatsResponse> ticketFuture = fetchTicketStats(userEmail);
        CompletableFuture<AdminAuthStatsResponse> authFuture = fetchAuthStats();
        CompletableFuture<AdminRagStatsResponse> ragFuture = fetchRagStats();
        CompletableFuture<AdminAnalysisStatsResponse> analysisFuture = fetchAnalysisStats();

        CompletableFuture.allOf(ticketFuture, authFuture, ragFuture, analysisFuture).join();

        AdminTicketStatsResponse ticketStats = ticketFuture.join();
        AdminAuthStatsResponse authStats = authFuture.join();
        AdminRagStatsResponse ragStats = ragFuture.join();
        AdminAnalysisStatsResponse analysisStats = analysisFuture.join();

        List<AdminDashboardResponse.SystemHealthDTO> systemHealth = fetchSystemHealth();

        return AdminDashboardResponse.builder()
                .platformOverview(buildPlatformOverview(ticketStats, authStats, analysisStats))
                .aiGovernance(buildAiGovernance(ticketStats, analysisStats, ragStats))
                .departmentWorkload(ticketStats.getDepartmentWorkload() != null ? ticketStats.getDepartmentWorkload() : Collections.emptyMap())
                .routingOverview(ticketStats.getRoutingOverview() != null ? ticketStats.getRoutingOverview() : Collections.emptyMap())
                .systemHealth(systemHealth)
                .ragKnowledge(buildRagKnowledge(ragStats))
                .recentEvents(Collections.emptyList()) 
                .myActivity(Collections.emptyList()) 
                .platformInfo(buildPlatformInfo())
                .build();
    }

    private CompletableFuture<AdminTicketStatsResponse> fetchTicketStats(String userEmail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ticketClient.getAdminStats(userEmail);
            } catch (Exception e) {
                log.error("Failed to fetch ticket stats", e);
                return AdminTicketStatsResponse.builder().build();
            }
        });
    }

    private CompletableFuture<AdminAuthStatsResponse> fetchAuthStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return authClient.getAdminStats();
            } catch (Exception e) {
                log.error("Failed to fetch auth stats", e);
                return AdminAuthStatsResponse.builder().build();
            }
        });
    }

    private CompletableFuture<AdminRagStatsResponse> fetchRagStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ragClient.getAdminStats();
            } catch (Exception e) {
                log.error("Failed to fetch rag stats", e);
                return AdminRagStatsResponse.builder().build();
            }
        });
    }

    private CompletableFuture<AdminAnalysisStatsResponse> fetchAnalysisStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var res = analysisClient.getAdminStats();
                if (res.isSuccess()) {
                    return res.getData();
                }
                return AdminAnalysisStatsResponse.builder().build();
            } catch (Exception e) {
                log.error("Failed to fetch analysis stats", e);
                return AdminAnalysisStatsResponse.builder().build();
            }
        });
    }

    private List<AdminDashboardResponse.SystemHealthDTO> fetchSystemHealth() {
        List<CompletableFuture<AdminDashboardResponse.SystemHealthDTO>> healthFutures = List.of(
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("AI-ORCHESTRATION-SERVICE")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("API-GATEWAY")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("TICKET-SERVICE")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("AUTH-SERVICE")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("ROUTING-SERVICE")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("RAG-SERVICE")),
            CompletableFuture.supplyAsync(() -> healthClient.getSystemHealth("AI-ANALYSIS-SERVICE"))
        );
        CompletableFuture.allOf(healthFutures.toArray(new CompletableFuture[0])).join();
        return healthFutures.stream().map(CompletableFuture::join).toList();
    }

    private AdminDashboardResponse.PlatformOverviewDTO buildPlatformOverview(AdminTicketStatsResponse ticketStats, AdminAuthStatsResponse authStats, AdminAnalysisStatsResponse analysisStats) {
        return AdminDashboardResponse.PlatformOverviewDTO.builder()
                .ticketsToday(ticketStats.getTicketsToday())
                .activeTickets(ticketStats.getActiveTickets())
                .resolvedToday(ticketStats.getResolvedToday())
                .aiProcessedToday(analysisStats.getProcessedToday())
                .totalCustomers(authStats.getTotalCustomers())
                .totalAgents(authStats.getTotalAgents())
                .totalAdmins(authStats.getTotalAdmins())
                .build();
    }

    private AdminDashboardResponse.AiGovernanceDTO buildAiGovernance(AdminTicketStatsResponse ticketStats, AdminAnalysisStatsResponse analysisStats, AdminRagStatsResponse ragStats) {
        return AdminDashboardResponse.AiGovernanceDTO.builder()
                .highConfidenceRate(calculateRate(analysisStats.getHighConfidenceAnalyses(), analysisStats.getTotalAnalyses()))
                .assignmentRate(calculateRate(ticketStats.getAssignedTickets(), ticketStats.getTotalTickets()))
                .knowledgeCoverage(calculateRate(ragStats.getSuccessfulRagRequests(), ragStats.getTotalRagRequests()))
                .averageLatency("N/A")
                .build();
    }

    private AdminDashboardResponse.RagKnowledgeDTO buildRagKnowledge(AdminRagStatsResponse ragStats) {
        return AdminDashboardResponse.RagKnowledgeDTO.builder()
                .totalArticles(ragStats.getTotalArticles())
                .embeddedArticles(ragStats.getVectorizedDocuments())
                .embeddingCoverage(calculateRate(ragStats.getVectorizedDocuments(), ragStats.getTotalArticles()))
                .knowledgeCoverage(calculateRate(ragStats.getSuccessfulRagRequests(), ragStats.getTotalRagRequests()))
                .mostUsedArticle(ragStats.getMostAccessedArticle())
                .build();
    }

    private AdminDashboardResponse.PlatformInfoDTO buildPlatformInfo() {
        return AdminDashboardResponse.PlatformInfoDTO.builder()
                .platformName("AI Support System")
                .platformVersion("1.0.0")
                .buildVersion("Local Build")
                .environment("Local")
                .build();
    }

    private String calculateRate(long numerator, long denominator) {
        if (denominator > 0) {
            return ((numerator * 100) / denominator) + "%";
        }
        return "N/A";
    }
}
