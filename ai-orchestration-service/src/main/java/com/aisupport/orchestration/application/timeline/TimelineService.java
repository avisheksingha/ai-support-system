package com.aisupport.orchestration.application.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.orchestration.application.timeline.dto.AIInsightResponse;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
import com.aisupport.orchestration.application.timeline.dto.WorkspaceAggregationResponse;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;
import com.aisupport.orchestration.infrastructure.client.RagClient;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimelineService {
	
	private static final String DEFAULT_MODEL_ID = "ai-analysis-service";

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowCheckpointRepository workflowCheckpointRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;
    private final TimelineMapper timelineMapper;
    private final AnalysisClient analysisClient;
    private final RoutingClient routingClient;
    private final RagClient ragClient;

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
    public Optional<AIInsightResponse> getTicketInsights(Long ticketId) {
        return fetchTicketInsights(ticketId);
    }

    private Optional<AIInsightResponse> fetchTicketInsights(Long ticketId) {
        try {
            Result<AnalysisResult> analysisResult = analysisClient.getAnalysis(ticketId);
            if (!analysisResult.isSuccess() || analysisResult.getData() == null) {
                return Optional.empty();
            }

            AnalysisResult data = analysisResult.getData();

            String model = aiExecutionRecordRepository.findTopByTicketIdOrderByExecutedAtDesc(ticketId)
                    .map(AiExecutionRecordEntity::getModelId)
                    .filter(Objects::nonNull)
                    .orElse(DEFAULT_MODEL_ID);

            return Optional.of(AIInsightResponse.builder()
                    .ticketId(ticketId)
                    .intent(data.intent())
                    .sentiment(data.sentiment())
                    .urgency(data.urgency())
                    .analysisProvider(model)
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceAggregationResponse> getWorkspaceData(Long ticketId) {
        WorkspaceAggregationResponse response = new WorkspaceAggregationResponse();

        // Fetch Analysis
        Optional<AIInsightResponse> insights = fetchTicketInsights(ticketId);
        insights.ifPresent(response::setAnalysis);

        Result<KnowledgeContext> ragResponse = ragClient.getRagResponse(ticketId);
        if (ragResponse.isSuccess()) {
            response.setKnowledge(ragResponse.getData());
        }

        Result<RoutingDecision> routingResponse = routingClient.getRouting(ticketId);
        if (routingResponse.isSuccess()) {
            response.setRouting(routingResponse.getData());
        }

        if (response.getAnalysis() == null && response.getKnowledge() == null && response.getRouting() == null) {
            return Optional.empty();
        }

        return Optional.of(response);
    }
}
