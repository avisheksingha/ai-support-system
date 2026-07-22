package com.aisupport.orchestration.application.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.common.enums.WorkflowOutcome;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelineEventType;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;

@Component
public class TimelineMapper {

    private static final String STAGE_AI_REASONING = "AI Reasoning";

    @Value("${orchestration.workflow.version:1.0}")
    private String defaultWorkflowVersion;

    public TimelineEvent toEvent(WorkflowExecutionEntity execution) {
        Long latencyMs = null;
        if (execution.getCreatedAt() != null && execution.getCompletedAt() != null) {
            latencyMs = Duration.between(execution.getCreatedAt(), execution.getCompletedAt()).toMillis();
        }

        WorkflowOutcome outcome = null;
        if (execution.getState() != null) {
            switch (execution.getState()) {
                case COMPLETED -> outcome = WorkflowOutcome.SUCCESS;
                case FAILED -> outcome = WorkflowOutcome.FAILED;
                default -> outcome = null;
            }
        }

        return TimelineEvent.builder()
                .eventId(execution.getId())
                .type(TimelineEventType.WORKFLOW)
                .subType(execution.getState() != null ? execution.getState().name() : "COMPLETED")
                .title("Workflow " + (execution.getState() != null ? execution.getState().name() : "COMPLETED"))
                .description("Workflow definition: " + execution.getDefinitionId())
                .severity(execution.getState() == WorkflowState.FAILED ? "ERROR" : "SUCCESS")
                .processingStage("Initialization")
                .timestamp(execution.getCreatedAt())
                .latencyMs(latencyMs)
                .outcome(outcome)
                .workflowVersion(execution.getVersion() != null ? String.valueOf(execution.getVersion()) : defaultWorkflowVersion)
                .build();
    }

    public TimelineEvent toEvent(WorkflowCheckpointEntity checkpoint) {
        String stage = getProcessingStage(checkpoint.getStepName());
        String model = null;
        
        if (checkpoint.getAttributesSnapshot() != null) {
            Object kcObj = checkpoint.getAttributesSnapshot().get("knowledgeContext");
            if (kcObj instanceof java.util.Map<?, ?> kc && kc.get("model") != null) {
                model = kc.get("model").toString();
            }
        }
        
        return TimelineEvent.builder()
                .eventId(checkpoint.getId() != null ? String.valueOf(checkpoint.getId()) : UUID.randomUUID().toString())
                .parentEventId(checkpoint.getExecution().getId())
                .type(TimelineEventType.SYSTEM)
                .subType("CHECKPOINT")
                .title("Step Completed: " + checkpoint.getStepName())
                .description("Saved state snapshot at " + checkpoint.getStepName())
                .severity("INFO")
                .processingStage(stage)
                .timestamp(checkpoint.getCreatedAt())
                .model(model)
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
            String rawTools = aiRecord.getToolsInvoked().replaceAll("[\\[\\]\"\\\\]", "");
            for (String tool : rawTools.split(",")) {
                String trimmed = tool.trim();
                if (!trimmed.isEmpty()) {
                    tools.add(trimmed);
                }
            }
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
                .processingStage(STAGE_AI_REASONING)
                .timestamp(aiRecord.getExecutedAt())
                .tools(tools)
                .latencyMs(aiRecord.getLatencyMs())
                .tokens(aiRecord.getCompletionTokens() != null && aiRecord.getPromptTokens() != null ? 
                        aiRecord.getCompletionTokens() + aiRecord.getPromptTokens() : null)
                .workflowVersion(aiRecord.getWorkflowVersion())
                .promptVersion(aiRecord.getPromptVersion())
                .model(aiRecord.getModelId())
                .outcome(outcome)
                .build();
    }

    private String getProcessingStage(String stepName) {
        if (stepName == null) return "System";
        return switch (stepName) {
            case WorkflowStepConstants.ASSEMBLE_CONTEXT -> "Context Assembly";
            case WorkflowStepConstants.ANALYZE_TICKET -> STAGE_AI_REASONING;
            case WorkflowStepConstants.KNOWLEDGE_SEARCH -> "Tool Execution";
            case WorkflowStepConstants.ROUTE_TICKET -> "Routing";
            case WorkflowStepConstants.FINAL_AI_DECISION -> STAGE_AI_REASONING;
            default -> "Processing";
        };
    }
}
