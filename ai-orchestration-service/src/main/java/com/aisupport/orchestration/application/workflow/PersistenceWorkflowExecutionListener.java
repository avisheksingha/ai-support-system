package com.aisupport.orchestration.application.workflow;

import java.time.Instant;
import java.util.HashMap;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(30)
@RequiredArgsConstructor
public class PersistenceWorkflowExecutionListener implements WorkflowExecutionListener {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowCheckpointRepository checkpointRepository;

    @Override
    @Transactional
    public void beforeWorkflow(WorkflowContext context) {
        WorkflowExecutionEntity entity = WorkflowExecutionEntity.builder()
                .id(context.getExecutionId())
                .definitionId(context.getWorkflowId())
                .correlationId(context.getCorrelationId())
                .ticketId(context.getTicketId())
                .conversationId(context.getConversationId())
                .state(WorkflowState.RUNNING)
                .attributes(new HashMap<>(context.getAttributes()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        executionRepository.save(entity);
    }

    @Override
    @Transactional
    public void afterWorkflow(WorkflowContext context) {
        executionRepository.findById(context.getExecutionId()).ifPresent(entity -> {
            entity.setState(WorkflowState.COMPLETED);
            entity.setCurrentStep("COMPLETED");
            entity.setCompletedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());
            entity.setAttributes(new HashMap<>(context.getAttributes()));
            executionRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        executionRepository.findById(context.getExecutionId()).ifPresent(execution -> {
            WorkflowCheckpointEntity checkpoint = WorkflowCheckpointEntity.builder()
                    .execution(execution)
                    .stepName(step.getName())
                    .stateSnapshot(WorkflowState.RUNNING)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }

    @Override
    @Transactional
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        executionRepository.findById(context.getExecutionId()).ifPresent(execution -> {
            execution.setAttributes(new HashMap<>(context.getAttributes()));
            execution.setUpdatedAt(Instant.now());
            executionRepository.save(execution);

            WorkflowCheckpointEntity checkpoint = WorkflowCheckpointEntity.builder()
                    .execution(execution)
                    .stepName(step.getName())
                    .stateSnapshot(WorkflowState.COMPLETED)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }

    @Override
    @Transactional
    public void onFailure(WorkflowContext context, Throwable error) {
        executionRepository.findById(context.getExecutionId()).ifPresent(entity -> {
            entity.setState(WorkflowState.FAILED);
            entity.setUpdatedAt(Instant.now());
            executionRepository.save(entity);

            WorkflowCheckpointEntity checkpoint = WorkflowCheckpointEntity.builder()
                    .execution(entity)
                    .stepName("FAILURE")
                    .stateSnapshot(WorkflowState.FAILED)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }
}
