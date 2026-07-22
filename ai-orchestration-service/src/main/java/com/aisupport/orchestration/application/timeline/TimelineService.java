package com.aisupport.orchestration.application.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.timeline.dto.AIInsightResponse;
import com.aisupport.orchestration.application.timeline.dto.AiDecisionDTO;
import com.aisupport.orchestration.application.timeline.dto.KnowledgeInsightDTO;
import com.aisupport.orchestration.application.timeline.dto.PipelineProgressDTO;
import com.aisupport.orchestration.application.timeline.dto.RoutingInsightDTO;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
import com.aisupport.orchestration.application.timeline.dto.WorkflowMetadataDTO;
import com.aisupport.orchestration.application.timeline.dto.WorkspaceDataResponse;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

	private static final String MODEL_KEY = "model";
    private static final String DEFAULT_MODEL_ID = "unavailable";
    
    private static final String KNOWLEDGE_CONTEXT_KEY = "knowledgeContext";
    private static final String ROUTING_DECISION_KEY = "routingDecision";
    private static final String AI_DECISION_KEY = "aiDecision";
    private static final String ANALYSIS_RESULT_KEY = "analysisResult";
    private static final String CONFIDENCE_KEY = "confidence";
    private static final String KNOWLEDGE_SUMMARY_KEY = "knowledgeSummary";
    private static final String ASSIGN_TO_TEAM_KEY = "assignToTeam";
    private static final String PRIORITY_KEY = "priority";
    private static final String SLA_HOURS_KEY = "slaHours";
    private static final String AI_SUMMARY_KEY = "aiSummary";
    private static final String SUGGESTED_REPLY_KEY = "suggestedReply";
    private static final String INTENT_KEY = "intent";
    private static final String SENTIMENT_KEY = "sentiment";
    private static final String URGENCY_KEY = "urgency";
    
	private static final String MATCHED_ARTICLE_TITLES_KEY = "matchedArticleTitles";

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowCheckpointRepository workflowCheckpointRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;
    private final TimelineMapper timelineMapper;

    @Transactional(readOnly = true)
    public TimelinePageResponse getTimelineForTicket(Long ticketId, int page, int size) {
        List<TimelineEvent> allEvents = new ArrayList<>();

        workflowExecutionRepository.findByTicketId(ticketId).ifPresent(execution -> {
            // 1. Add Workflow Execution Event
            allEvents.add(timelineMapper.toEvent(execution));

            // 2. Add Checkpoint Events
            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            for (WorkflowCheckpointEntity checkpoint : checkpoints) {
                allEvents.add(timelineMapper.toEvent(checkpoint));
            }

            // 3. Add AI Execution Events
            List<AiExecutionRecordEntity> aiRecords = aiExecutionRecordRepository
                    .findByCorrelationId(execution.getCorrelationId());
            for (AiExecutionRecordEntity aiRecord : aiRecords) {
                allEvents.add(timelineMapper.toEvent(aiRecord));
            }
        });

        // Sort chronologically
        allEvents.sort(Comparator.comparing(TimelineEvent::getTimestamp));

        // In-memory pagination
        int totalElements = allEvents.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<TimelineEvent> pageContent = allEvents.subList(start, end);

        return TimelinePageResponse.builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(end >= totalElements)
                .build();
    }

    @Transactional(readOnly = true)
    public TimelinePageResponse getTimelineForWorkflowId(String workflowId, int page, int size) {
        List<TimelineEvent> allEvents = new ArrayList<>();

        workflowExecutionRepository.findById(workflowId).ifPresent(execution -> {
            // 1. Add Workflow Execution Event
            allEvents.add(timelineMapper.toEvent(execution));

            // 2. Add Checkpoint Events
            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            for (WorkflowCheckpointEntity checkpoint : checkpoints) {
                allEvents.add(timelineMapper.toEvent(checkpoint));
            }

            // 3. Add AI Execution Events
            List<AiExecutionRecordEntity> aiRecords = aiExecutionRecordRepository
                    .findByCorrelationId(execution.getCorrelationId());
            for (AiExecutionRecordEntity aiRecord : aiRecords) {
                allEvents.add(timelineMapper.toEvent(aiRecord));
            }
        });

        // Sort chronologically
        allEvents.sort(Comparator.comparing(TimelineEvent::getTimestamp));

        // In-memory pagination
        int totalElements = allEvents.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<TimelineEvent> pageContent = allEvents.subList(start, end);

        return TimelinePageResponse.builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(end >= totalElements)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<AIInsightResponse> getTicketInsights(Long ticketId) {
        return fetchTicketInsights(ticketId);
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceDataResponse> getWorkspaceData(Long ticketId) {
        try {
            Optional<WorkflowExecutionEntity> executionOpt = workflowExecutionRepository.findByTicketId(ticketId);
            if (executionOpt.isEmpty()) {
                return Optional.empty();
            }

            WorkflowExecutionEntity execution = executionOpt.get();

            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            if (checkpoints.isEmpty()) {
                return Optional.empty();
            }

            WorkflowCheckpointEntity latestCheckpoint = checkpoints.get(0);
            Map<String, Object> attributes = latestCheckpoint.getAttributesSnapshot();

            WorkspaceDataResponse.WorkspaceDataResponseBuilder responseBuilder = WorkspaceDataResponse.builder();

            // --- Analysis ---
            fetchTicketInsights(ticketId).ifPresent(responseBuilder::analysis);

            // --- Knowledge ---
            if (attributes.containsKey(KNOWLEDGE_CONTEXT_KEY)) {
                responseBuilder.knowledge(mapKnowledgeInsight(attributes));
            }

            // --- Routing ---
            if (attributes.containsKey(ROUTING_DECISION_KEY)) {
                responseBuilder.routing(mapRoutingInsight(attributes));
            }

            // --- AI Decision ---
            if (attributes.containsKey(AI_DECISION_KEY)) {
                responseBuilder.aiDecision(mapAiDecision(attributes));
            }

            // --- Workflow Metadata ---
            responseBuilder.workflowMetadata(mapWorkflowMetadata(execution));

            // --- Pipeline Progress ---
            responseBuilder.pipelineProgress(mapPipelineProgress(attributes));

            return Optional.of(responseBuilder.build());
        } catch (Exception e) {
            log.error("Failed to build workspace data for ticketId={}", ticketId, e);
            return Optional.empty();
        }
    }

    // ── Private Mapping Methods ──────────────────────────────────────────────

    private Optional<AIInsightResponse> fetchTicketInsights(Long ticketId) {
        try {
            Optional<WorkflowExecutionEntity> executionOpt = workflowExecutionRepository.findByTicketId(ticketId);
            if (executionOpt.isEmpty()) {
                return Optional.empty();
            }

            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(executionOpt.get().getId());
            if (checkpoints.isEmpty()) {
                return Optional.empty();
            }

            WorkflowCheckpointEntity latestCheckpoint = checkpoints.get(0);
            Map<String, Object> attributes = latestCheckpoint.getAttributesSnapshot();

            Object analysisObj = attributes.get(ANALYSIS_RESULT_KEY);
            if (!(analysisObj instanceof Map<?, ?>)) {
                return Optional.empty();
            }
            Map<?, ?> analysisMap = (Map<?, ?>) analysisObj;

            String model = aiExecutionRecordRepository.findTopByTicketIdOrderByExecutedAtDesc(ticketId)
                    .map(AiExecutionRecordEntity::getModelId)
                    .filter(Objects::nonNull)
                    .orElse(DEFAULT_MODEL_ID);
                    
            if (DEFAULT_MODEL_ID.equals(model)) {
                Object kcObj = attributes.get(KNOWLEDGE_CONTEXT_KEY);
                if (kcObj instanceof Map<?, ?> kc && kc.get(MODEL_KEY) != null) {
                    model = kc.get(MODEL_KEY).toString();
                }
            }

            Double confidence = 0.0;
            Object aiDecisionObj = attributes.get(AI_DECISION_KEY);
            if (aiDecisionObj instanceof Map<?, ?> aiDecision && aiDecision.get(CONFIDENCE_KEY) instanceof Number num) {
                confidence = num.doubleValue();
            }

            List<String> keywordsList = analysisMap.get("keywords") instanceof List<?> list
                    ? list.stream().map(Objects::toString).toList()
                    : Collections.emptyList();

            return Optional.of(AIInsightResponse.builder()
                    .intent(Objects.toString(analysisMap.get(INTENT_KEY), null))
                    .sentiment(Objects.toString(analysisMap.get(SENTIMENT_KEY), null))
                    .urgency(Objects.toString(analysisMap.get(URGENCY_KEY), null))
                    .confidenceScore(confidence)
                    .analysisProvider(model)
                    .keywords(keywordsList)
                    .suggestedCategory(Objects.toString(analysisMap.get("suggestedCategory"), null))
                    .build());
        } catch (Exception e) {
            log.error("Failed to fetch ticket insights for ticketId={}", ticketId, e);
            return Optional.empty();
        }
    }

    private KnowledgeInsightDTO mapKnowledgeInsight(Map<String, Object> attributes) {
        Object kcObj = attributes.get(KNOWLEDGE_CONTEXT_KEY);
        if (!(kcObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> kc = (Map<?, ?>) kcObj;

        boolean knowledgeFound = Boolean.TRUE.equals(kc.get("knowledgeFound"));
        String model = Objects.toString(kc.get(MODEL_KEY), null);
        Integer retrievedDocumentCount = kc.get("retrievedDocumentCount") instanceof Number n ? n.intValue() : null;
        
        List<String> matchedArticleTitles = Collections.emptyList();
        Object rawTitles = kc.get(MATCHED_ARTICLE_TITLES_KEY);
        
        // Safely cast
        if (rawTitles instanceof List<?> titleList) {
            matchedArticleTitles = titleList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        
        return KnowledgeInsightDTO.builder()
                .knowledgeSummary(Objects.toString(kc.get(KNOWLEDGE_SUMMARY_KEY), null))
                .confidence(knowledgeFound ? 1.0 : 0.0)
                .sources(Collections.emptyList())
                .knowledgeFound(knowledgeFound)
                .model(model)
                .retrievedDocumentCount(retrievedDocumentCount)
                .matchedArticleTitles(matchedArticleTitles)
                .build();
    }

    private RoutingInsightDTO mapRoutingInsight(Map<String, Object> attributes) {
        Object rdObj = attributes.get(ROUTING_DECISION_KEY);
        if (!(rdObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> rd = (Map<?, ?>) rdObj;

        return RoutingInsightDTO.builder()
                .assignedTeam(Objects.toString(rd.get(ASSIGN_TO_TEAM_KEY), null))
                .priority(rd.get(PRIORITY_KEY) != null ? rd.get(PRIORITY_KEY).toString() : null)
                .slaHours(rd.get(SLA_HOURS_KEY) instanceof Number n ? n.intValue() : null)
                .build();
    }

    private AiDecisionDTO mapAiDecision(Map<String, Object> attributes) {
        Object adObj = attributes.get(AI_DECISION_KEY);
        if (!(adObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> ad = (Map<?, ?>) adObj;

        return AiDecisionDTO.builder()
                .aiSummary(Objects.toString(ad.get(AI_SUMMARY_KEY), null))
                .suggestedReply(Objects.toString(ad.get(SUGGESTED_REPLY_KEY), null))
                .confidence(ad.get(CONFIDENCE_KEY) instanceof Number n ? n.doubleValue() : null)
                .decisionReason(Objects.toString(ad.get("decisionReason"), null))
                .build();
    }

    private WorkflowMetadataDTO mapWorkflowMetadata(WorkflowExecutionEntity execution) {
        Long durationMs = null;
        if (execution.getCreatedAt() != null && execution.getCompletedAt() != null) {
            durationMs = Duration.between(execution.getCreatedAt(), execution.getCompletedAt()).toMillis();
        }

        return WorkflowMetadataDTO.builder()
                .workflowExecutionId(execution.getId())
                .workflowState(execution.getState() != null ? execution.getState().name() : null)
                .workflowDurationMs(durationMs)
                .build();
    }

    private PipelineProgressDTO mapPipelineProgress(Map<String, Object> attributes) {
        return PipelineProgressDTO.builder()
                .analysisCompleted(attributes.containsKey(ANALYSIS_RESULT_KEY))
                .knowledgeCompleted(attributes.containsKey(KNOWLEDGE_CONTEXT_KEY))
                .routingCompleted(attributes.containsKey(ROUTING_DECISION_KEY))
                .decisionCompleted(attributes.containsKey(AI_DECISION_KEY))
                .build();
    }
}