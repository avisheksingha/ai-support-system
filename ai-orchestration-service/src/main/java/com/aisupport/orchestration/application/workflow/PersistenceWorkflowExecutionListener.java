package com.aisupport.orchestration.application.workflow;

import java.time.Instant;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.AiDecision;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.PromptMetadata;
import com.aisupport.orchestration.domain.workflow.TicketContextSnapshot;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(30)
@RequiredArgsConstructor
public class PersistenceWorkflowExecutionListener implements WorkflowExecutionListener {
	
	private static final String UNKNOWN_VALUE = "Unknown";

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowCheckpointRepository checkpointRepository;

    @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}")
    private String chatModel;
    
    @Value("${orchestration.prompt.final.path:classpath:prompts/final-decision.st}")
    private String finalDecisionTemplatePath;

    @Value("${orchestration.prompt.version:1.0}")
    private String promptVersion;

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
            entity.setCurrentStep(WorkflowStepConstants.COMPLETED);
            entity.setCompletedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());
            entity.setAttributes(new HashMap<>(context.getAttributes()));
            
            String templateName = finalDecisionTemplatePath.replace("classpath:prompts/", "");

            TicketContextSnapshot snapshot = 
                TicketContextSnapshot.builder()
                    .schemaVersion(1)
                    .ticketId(context.getTicketId())
                    .workflowExecutionId(context.getExecutionId())
                    .analysisResult(context.getResource(AnalysisResult.class))
                    .knowledgeContext(context.getResource(KnowledgeContext.class))
                    .routingDecision(context.getResource(RoutingDecision.class))
                    .prompt(PromptMetadata.builder()
                        .templateName(templateName)
                        .promptVersion(promptVersion)
                        .promptHash(context.getAttribute("promptHash") != null ? (String) context.getAttribute("promptHash") : UNKNOWN_VALUE)
                        .modelId(chatModel)
                        .build())
                    .aiDecision(context.getResource(AiDecision.class))
                    .build();
            entity.setTicketContext(snapshot);

            executionRepository.save(entity);

            WorkflowCheckpointEntity checkpoint = WorkflowCheckpointEntity.builder()
                    .execution(entity)
                    .correlationId(context.getCorrelationId())
                    .stepName(WorkflowStepConstants.PERSISTENCE)
                    .stateSnapshot(WorkflowState.COMPLETED)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }

    @Override
    @Transactional
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        executionRepository.findById(context.getExecutionId()).ifPresent(execution -> {
            WorkflowCheckpointEntity checkpoint = WorkflowCheckpointEntity.builder()
                    .execution(execution)
                    .correlationId(context.getCorrelationId())
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
                    .correlationId(context.getCorrelationId())
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
                    .correlationId(context.getCorrelationId())
                    .stepName(WorkflowStepConstants.FAILURE)
                    .stateSnapshot(WorkflowState.FAILED)
                    .attributesSnapshot(new HashMap<>(context.getAttributes()))
                    .createdAt(Instant.now())
                    .build();
            checkpointRepository.save(checkpoint);
        });
    }
}
