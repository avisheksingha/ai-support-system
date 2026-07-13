package com.aisupport.orchestration.application.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
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
}
