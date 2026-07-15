package com.aisupport.orchestration.application.workflow.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.aisupport.common.constant.Correlation;
import com.aisupport.orchestration.application.workflow.WorkflowEngine;
import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.application.workflow.WorkflowExecutionResult;
import com.aisupport.orchestration.application.workflow.WorkflowFactory;
import com.aisupport.orchestration.application.workflow.WorkflowStatus;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowDefinition;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEngineImpl implements WorkflowEngine {

    private final WorkflowFactory workflowFactory;
    private final StepExecutor stepExecutor;
    private final List<WorkflowExecutionListener> listeners;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowCheckpointRepository checkpointRepository;

    @Override
    public WorkflowExecutionResult resume(String executionId) {
        log.info("Resuming execution: {}", executionId);

        WorkflowExecutionEntity execution =
            executionRepository.findById(executionId).orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        if (!WorkflowState.FAILED.equals(execution.getState()) &&
            !WorkflowState.CREATED.equals(execution.getState())) {
            throw new IllegalStateException("Can only resume FAILED or CREATED executions. Current state: " + execution.getState());
        }

        List<WorkflowCheckpointEntity> checkpoints =
            checkpointRepository.findByExecutionIdOrderByCreatedAtDesc(executionId);

        WorkflowCheckpointEntity lastCheckpoint = checkpoints.isEmpty() ? null : checkpoints.get(0);

        String lastCompletedStep = lastCheckpoint != null ? lastCheckpoint.getStepName() : null;

        WorkflowContext context = WorkflowContext.builder()
                .workflowId(execution.getDefinitionId())
                .executionId(executionId)
                .correlationId(execution.getCorrelationId())
                .ticketId(execution.getTicketId())
                .conversationId(execution.getConversationId())
                .build();

        if (lastCheckpoint != null && lastCheckpoint.getAttributesSnapshot() != null) {
            lastCheckpoint.getAttributesSnapshot().forEach(context::putAttribute);
        } else if (execution.getAttributes() != null) {
            execution.getAttributes().forEach(context::putAttribute);
        }

        execution.setRecoveryCount(execution.getRecoveryCount() + 1);
        executionRepository.save(execution);

        return executeInternal(execution.getDefinitionId(), context, lastCompletedStep);
    }

    @Override
    public WorkflowExecutionResult execute(String workflowId, WorkflowContext context) {
        log.info("Starting execution of workflow: {} for correlationId: {}", workflowId, context.getCorrelationId());

        if (executionRepository.existsByCorrelationIdAndDefinitionIdAndVersion(
                context.getCorrelationId(), workflowId, 1)) { // Assume v1
            log.warn("Idempotency check failed. Workflow {} for correlationId {} already processed.",
                     workflowId, context.getCorrelationId());
            return WorkflowExecutionResult.builder()
                    .workflowId(workflowId)
                    .executionId(context.getExecutionId())
                    .status(WorkflowStatus.SKIPPED)
                    .duration(Duration.ZERO)
                    .build();
        }
        return executeInternal(workflowId, context, null);
    }

    private WorkflowExecutionResult executeInternal(String workflowId, WorkflowContext context, String skipUpToStep) {
        initializeContext(workflowId, context);

        Instant start = Instant.now();

        WorkflowDefinition definition = workflowFactory.create(workflowId);
        if (definition == null) {
            log.error("Workflow definition not found for id: {}", workflowId);
            return buildResult(workflowId, context, WorkflowStatus.FAILED, "Definition not found", 0, start);
        }

        context.setWorkflowVersion(definition.getVersion());

        if (skipUpToStep == null) {
            dispatchBeforeWorkflow(context);
        }

        AtomicInteger stepsExecuted = new AtomicInteger(0);
        try {
            runSteps(definition, context, skipUpToStep, stepsExecuted);
            dispatchAfterWorkflow(context);
            return buildResult(workflowId, context, WorkflowStatus.SUCCESS, null, stepsExecuted.get(), start);
        } catch (Exception e) {
            log.error("Workflow {} failed", workflowId, e);
            dispatchOnFailure(context, e);
            return buildResult(workflowId, context, WorkflowStatus.FAILED, e.getMessage(), stepsExecuted.get(), start);
        }
    }

    private void initializeContext(String workflowId, WorkflowContext context) {
        context.setWorkflowId(workflowId);
        if (context.getExecutionId() == null) {
            context.setExecutionId(UUID.randomUUID().toString());
        }
        if (context.getCorrelationId() == null) {
            String mdcCorrelationId = MDC.get(Correlation.MDC_KEY);
            context.setCorrelationId(mdcCorrelationId != null ? mdcCorrelationId : UUID.randomUUID().toString());
        }
    }

    private void runSteps(WorkflowDefinition definition, WorkflowContext context, String skipUpToStep, AtomicInteger stepsExecuted) {
        boolean skip = skipUpToStep != null;

        for (WorkflowStep step : definition.getSteps()) {
            if (skip) {
                skip = !step.getName().equals(skipUpToStep); // stop skipping once we pass the last completed step
                continue;
            }

            if (step.supports(context)) {
                dispatchBeforeStep(context, step);
                stepExecutor.executeStep(step, context);
                dispatchAfterStep(context, step);
                stepsExecuted.incrementAndGet();
            } else {
                log.debug("Step {} skipped as it does not support current context", step.getName());
            }
        }
    }

    private WorkflowExecutionResult buildResult(String workflowId, WorkflowContext context, WorkflowStatus status,
                                                 String error, int stepsExecuted, Instant start) {
        return WorkflowExecutionResult.builder()
                .workflowId(workflowId)
                .executionId(context.getExecutionId())
                .status(status)
                .error(error)
                .stepsExecuted(stepsExecuted)
                .duration(Duration.between(start, Instant.now()))
                .build();
    }

    private void dispatchBeforeWorkflow(WorkflowContext context) {
        listeners.forEach(l -> {
            try {
                l.beforeWorkflow(context);
            } catch (Exception e) {
                log.error("Listener error in beforeWorkflow", e);
            }
        });
    }

    private void dispatchAfterWorkflow(WorkflowContext context) {
        listeners.forEach(l -> {
            try {
                l.afterWorkflow(context);
            } catch (Exception e) {
                log.error("Listener error in afterWorkflow", e);
            }
        });
    }

    private void dispatchBeforeStep(WorkflowContext context, WorkflowStep step) {
        listeners.forEach(l -> {
            try {
                l.beforeStep(context, step);
            } catch (Exception e) {
                log.error("Listener error in beforeStep", e);
            }
        });
    }

    private void dispatchAfterStep(WorkflowContext context, WorkflowStep step) {
        listeners.forEach(l -> {
            try {
                l.afterStep(context, step);
            } catch (Exception e) {
                log.error("Listener error in afterStep", e);
            }
        });
    }

    private void dispatchOnFailure(WorkflowContext context, Throwable error) {
        listeners.forEach(l -> {
            try {
                l.onFailure(context, error);
            } catch (Exception e) {
                log.error("Listener error in onFailure", e);
            }
        });
    }
}