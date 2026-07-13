package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.timeline.TimelineService;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/workflows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workflow Explorer", description = "Endpoints for exploring workflow executions")
public class WorkflowExplorerController {

    private final TimelineService timelineService;

    @GetMapping("/search")
    @Operation(summary = "Search workflow executions", description = "Search and drill down into specific workflow executions and their timelines")
    public ResponseEntity<TimelinePageResponse> searchWorkflows(
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Searching workflows - ticketId: {}, correlationId: {}", ticketId, correlationId);
        
        // For V1 we just route ticketId to TimelineService, which provides the timeline
        // In a real V2, this would query workflowExecutionRepo first, handle correlationId etc.
        if (ticketId != null) {
            return ResponseEntity.ok(timelineService.getTimelineForTicket(ticketId, page, size));
        }
        
        return ResponseEntity.ok(TimelinePageResponse.builder().build());
    }
}
