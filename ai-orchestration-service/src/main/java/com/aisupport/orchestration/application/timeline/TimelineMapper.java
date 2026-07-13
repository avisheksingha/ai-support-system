package com.aisupport.orchestration.application.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aisupport.common.enums.WorkflowOutcome;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelineEventType;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;

@Component
public class TimelineMapper {

    public TimelineEvent toEvent(WorkflowExecutionEntity execution) {
        return TimelineEvent.builder()
                .eventId(execution.getId())
                .type(TimelineEventType.WORKFLOW)
                .subType(execution.getState().name())
                .title("Workflow " + execution.getState().name())
                .description("Workflow definition: " + execution.getDefinitionId())
                .severity("SUCCESS")
                .processingStage("Initialization")
                .timestamp(execution.getCreatedAt())
                .workflowVersion("1.0")
                .build();
    }

    public TimelineEvent toEvent(WorkflowCheckpointEntity checkpoint) {
        String stage = getProcessingStage(checkpoint.getStepName());
        
        return TimelineEvent.builder()
                .eventId(UUID.randomUUID().toString()) // Checkpoints don't have UUIDs, generate one or use ID
                .parentEventId(checkpoint.getExecution().getId())
                .type(TimelineEventType.SYSTEM)
                .subType("CHECKPOINT")
                .title("Step Completed: " + checkpoint.getStepName())
                .description("Saved state snapshot at " + checkpoint.getStepName())
                .severity("INFO")
                .processingStage(stage)
                .timestamp(checkpoint.getCreatedAt())
                .build();
    }

    public TimelineEvent toEvent(AiExecutionRecordEntity aiRecord) {
        WorkflowOutcome outcome = null;
        try {
            if (aiRecord.getOutcome() != null) {
                outcome = WorkflowOutcome.valueOf(aiRecord.getOutcome());
            }
        } catch (IllegalArgumentException e) {
            // ignore
        }

        List<String> tools = new ArrayList<>();
        if (aiRecord.getToolsInvoked() != null && !aiRecord.getToolsInvoked().isBlank()) {
            tools = Arrays.asList(aiRecord.getToolsInvoked().split(","));
        }

        String severity = "SUCCESS";
        if (outcome == WorkflowOutcome.FAILED) {
            severity = "ERROR";
        } else if (outcome == WorkflowOutcome.PARTIAL_SUCCESS) {
            severity = "WARNING";
        }

        return TimelineEvent.builder()
                .eventId(aiRecord.getId())
                .parentEventId(aiRecord.getWorkflowExecutionId())
                .type(TimelineEventType.AI)
                .subType("AI_REASONING")
                .title("AI Execution Completed")
                .description(aiRecord.getReason() != null ? aiRecord.getReason() : "AI reasoning completed")
                .severity(severity)
                .processingStage("AI Reasoning")
                .timestamp(aiRecord.getExecutedAt())
                .tools(tools)
                .latencyMs(aiRecord.getLatencyMs())
                .tokens(aiRecord.getCompletionTokens() != null && aiRecord.getPromptTokens() != null ? 
                        aiRecord.getCompletionTokens() + aiRecord.getPromptTokens() : null)
                .workflowVersion(aiRecord.getWorkflowVersion())
                .promptVersion(aiRecord.getDefinitionVersion())
                .model(aiRecord.getModelId())
                .outcome(outcome)
                .build();
    }

    private String getProcessingStage(String stepName) {
        if (stepName == null) return "System";
        return switch (stepName) {
            case "AssembleContextStep" -> "Context Assembly";
            case "AnalyzeTicketStep" -> "AI Reasoning";
            case "KnowledgeSearchStep" -> "Tool Execution";
            case "RouteTicketStep" -> "Routing";
            default -> "Processing";
        };
    }
}
