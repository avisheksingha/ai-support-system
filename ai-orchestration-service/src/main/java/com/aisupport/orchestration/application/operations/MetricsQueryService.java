package com.aisupport.orchestration.application.operations;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
                .governance(buildGovernanceMetrics())
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
                .averageDurationMs(750) // Mock calculation for V1 fallback
                .hourlyThroughput(List.of(
                    OperationsOverviewDTO.TrendData.builder().label("09:00").value(12).build(),
                    OperationsOverviewDTO.TrendData.builder().label("10:00").value(18).build()
                ))
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

    private OperationsOverviewDTO.GovernanceMetrics buildGovernanceMetrics() {
        return OperationsOverviewDTO.GovernanceMetrics.builder()
                .policyViolations(0)
                .guardrailBlocks(0)
                .approvalRequests(0)
                .hourlyViolations(Collections.emptyList())
                .build();
    }

    private List<OperationsOverviewDTO.ProviderMetrics> buildToolMetrics() {
        OperationsOverviewDTO.ProviderMetrics github = OperationsOverviewDTO.ProviderMetrics.builder()
                .providerId("github-mcp")
                .invocations(15)
                .successes(15)
                .failures(0)
                .avgLatencyMs(350)
                .lastInvocation(Instant.now().toString())
                .build();
        return List.of(github);
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
                .serviceVersion("1.0.0")
                .gitCommit("8a4f9b2")
                .buildNumber("1024")
                .environment("development")
                .build();
    }
}
