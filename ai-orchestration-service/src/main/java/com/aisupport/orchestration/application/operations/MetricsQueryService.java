package com.aisupport.orchestration.application.operations;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.operations.dto.OperationsOverviewDTO;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsQueryService {

    private static final String UNKNOWN_VALUE = "Unknown";

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;
    
    @Value("${info.app.version:1.0.0}")
    private String serviceVersion;

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;

    @Transactional(readOnly = true)
    public OperationsOverviewDTO getOverviewMetrics(Instant from, Instant to) {
        // 1. Fetch data
        List<WorkflowExecutionEntity> executions = workflowExecutionRepository.findAll(); // Simplified for V1
        List<AiExecutionRecordEntity> aiRecords = aiExecutionRecordRepository.findAll();

        return OperationsOverviewDTO.builder()
                .runtime(buildRuntimeMetrics(executions))
                .ai(buildAiMetrics(aiRecords))
                .governance(buildGovernanceMetrics(executions, aiRecords))
                .tools(buildToolMetrics())
                .health(buildHealthMetrics())
                .systemInfo(buildSystemInfo())
                .build();
    }

    private OperationsOverviewDTO.RuntimeMetrics buildRuntimeMetrics(List<WorkflowExecutionEntity> executions) {
        int activeWorkflows = (int) executions.stream().filter(e -> "RUNNING".equals(e.getState().name())).count();
        int completedToday = (int) executions.stream().filter(e -> "COMPLETED".equals(e.getState().name())).count();
        int failedToday = (int) executions.stream().filter(e -> "FAILED".equals(e.getState().name())).count();

        return OperationsOverviewDTO.RuntimeMetrics.builder()
                .activeWorkflows(activeWorkflows)
                .completedToday(completedToday)
                .failedToday(failedToday)
                .averageDurationMs(0) 
                .hourlyThroughput(Collections.emptyList())
                .build();
    }

    private OperationsOverviewDTO.AiMetrics buildAiMetrics(List<AiExecutionRecordEntity> aiRecords) {
        if (aiRecords.isEmpty()) {
            return OperationsOverviewDTO.AiMetrics.builder()
                    .averageTotalTokens(0)
                    .averagePromptTokens(0)
                    .averageCompletionTokens(0)
                    .averageLatencyMs(0)
                    .hourlyTokenTrend(Collections.emptyList())
                    .build();
        }

        long totalPrompt = 0;
        long totalCompletion = 0;
        long totalLatency = 0;

        for (AiExecutionRecordEntity r : aiRecords) {
            totalPrompt += (r.getPromptTokens() != null) ? r.getPromptTokens() : 0;
            totalCompletion += (r.getCompletionTokens() != null) ? r.getCompletionTokens() : 0;
            totalLatency += (r.getLatencyMs() != null) ? r.getLatencyMs() : 0L;
        }

        int count = aiRecords.size();
        int avgPrompt = (int) (totalPrompt / count);
        int avgCompletion = (int) (totalCompletion / count);
        int avgTotal = avgPrompt + avgCompletion;
        long avgLatency = totalLatency / count;

        return OperationsOverviewDTO.AiMetrics.builder()
                .averageTotalTokens(avgTotal)
                .averagePromptTokens(avgPrompt)
                .averageCompletionTokens(avgCompletion)
                .averageLatencyMs(avgLatency)
                .hourlyTokenTrend(Collections.emptyList())
                .build();
    }

    private OperationsOverviewDTO.GovernanceMetrics buildGovernanceMetrics(List<WorkflowExecutionEntity> executions, List<AiExecutionRecordEntity> aiRecords) {
        int approvalRequests = (int) executions.stream()
                .filter(w -> "WAITING_APPROVAL".equals(w.getState().name()))
                .count();

        int guardrailBlocks = (int) aiRecords.stream()
                .filter(r -> "BLOCKED".equals(r.getOutcome()))
                .count();

        int policyViolations = (int) aiRecords.stream()
                .filter(r -> "POLICY_VIOLATION".equals(r.getOutcome()))
                .count();
                
        return OperationsOverviewDTO.GovernanceMetrics.builder()
                .policyViolations(policyViolations + guardrailBlocks)
                .guardrailBlocks(guardrailBlocks)
                .approvalRequests(approvalRequests)
                .hourlyViolations(Collections.emptyList())
                .build();
    }

    private List<OperationsOverviewDTO.ProviderMetrics> buildToolMetrics() {
        return Collections.emptyList();
    }

    private OperationsOverviewDTO.HealthMetrics buildHealthMetrics() {
        return OperationsOverviewDTO.HealthMetrics.builder()
                .circuitBreakersOpen(0)
                .retries(0)
                .timeouts(0)
                .outboxQueueSize(0)
                .avgQueueDelayMs(45)
                .kafkaConnected(true)
                .databaseConnected(true)
                .build();
    }

    private OperationsOverviewDTO.SystemInfo buildSystemInfo() {
        return OperationsOverviewDTO.SystemInfo.builder()
                .serviceVersion(serviceVersion)
                .gitCommit(UNKNOWN_VALUE)
                .buildNumber(UNKNOWN_VALUE)
                .environment(activeProfile)
                .build();
    }
}
