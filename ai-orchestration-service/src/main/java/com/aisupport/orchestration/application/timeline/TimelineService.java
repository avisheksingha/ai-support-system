package com.aisupport.orchestration.application.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.timeline.dto.AIInsightResponse;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowCheckpointRepository workflowCheckpointRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;
    private final TimelineMapper timelineMapper;
    private final AnalysisClient analysisClient;

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
        try {
            Result<Object> analysisResult = analysisClient.getAnalysis(ticketId);
            if (!analysisResult.isSuccess() || analysisResult.getData() == null) {
                return Optional.empty();
            }

            
            Object rawData = analysisResult.getData();
            if (!(rawData instanceof Map<?, ?>)) {
                return Optional.empty();
            }
            Map<?, ?> data = (Map<?, ?>) rawData;

            String model = (String) data.get("analysisProvider"); // Fallback from analysis service
            Optional<AiExecutionRecordEntity> aiRecord = aiExecutionRecordRepository.findTopByTicketIdOrderByExecutedAtDesc(ticketId);
            if (aiRecord.isPresent() && aiRecord.get().getModelId() != null) {
                model = aiRecord.get().getModelId();
            }

            Double confidence = null;
            if (data.get("confidenceScore") instanceof Number number) {
                confidence = number.doubleValue();
            }

            
            List<String> keywords = null;
            if (data.get("keywords") instanceof List<?> rawList) {
                keywords = rawList.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }

            return Optional.of(AIInsightResponse.builder()
                    .ticketId(ticketId)
                    .intent((String) data.get("intent"))
                    .sentiment((String) data.get("sentiment"))
                    .urgency((String) data.get("urgency"))
                    .confidenceScore(confidence)
                    .keywords(keywords)
                    .suggestedCategory((String) data.get("suggestedCategory"))
                    .analyzedAt((String) data.get("analyzedAt"))
                    .analysisProvider(model)
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
