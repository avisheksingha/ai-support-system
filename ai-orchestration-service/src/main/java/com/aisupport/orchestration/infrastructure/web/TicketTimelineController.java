package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.timeline.TimelineService;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
import com.aisupport.orchestration.application.timeline.dto.AIInsightResponse;
import com.aisupport.orchestration.application.timeline.dto.AIActionRequest;
import com.aisupport.orchestration.application.timeline.dto.AIActionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ticket Timeline", description = "Endpoints for fetching ticket-specific orchestration timelines")
public class TicketTimelineController {

    private final TimelineService timelineService;

    @GetMapping("/{ticketId}/timeline")
    @Operation(summary = "Get ticket timeline", description = "Retrieves the orchestration timeline for a specific ticket")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket timeline")
    public ResponseEntity<TimelinePageResponse> getTicketTimeline(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Fetching timeline for ticket: {}", ticketId);
        TimelinePageResponse response = timelineService.getTimelineForTicket(ticketId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{ticketId}/insights")
    @Operation(summary = "Get AI Insights", description = "Retrieves the latest AI analysis and insights for a specific ticket")
    public ResponseEntity<AIInsightResponse> getTicketInsights(@PathVariable Long ticketId) {
        log.info("Fetching AI insights for ticket: {}", ticketId);
        
        // Mock implementation for V1 frontend integration testing. 
        // In reality, this would query the DB for the latest AnalysisResult associated with this ticket.
        AIInsightResponse mockResponse = AIInsightResponse.builder()
                .ticketId(ticketId)
                .intent("BILLING_ISSUE")
                .sentiment("FRUSTRATED")
                .urgency("HIGH")
                .confidenceScore(0.92)
                .keywords(List.of("subscription", "upgrade", "charge", "error"))
                .suggestedCategory("Billing & Subscriptions")
                .analyzedAt(java.time.Instant.now().toString())
                .build();
                
        return ResponseEntity.ok(mockResponse);
    }
    
    @PostMapping("/{ticketId}/actions")
    @Operation(summary = "Trigger AI Action", description = "Manually trigger an AI action (e.g. generate draft, re-analyze)")
    public ResponseEntity<AIActionResponse> triggerAction(
            @PathVariable Long ticketId,
            @RequestBody AIActionRequest request) {
            
        log.info("Triggering AI action {} for ticket: {}", request.getActionType(), ticketId);
        
        // Mock implementation. In reality, this would publish a command to Kafka for the workflow orchestrator.
        AIActionResponse response = AIActionResponse.builder()
                .status("ACCEPTED")
                .message("Action " + request.getActionType() + " queued for execution")
                .workflowExecutionId(java.util.UUID.randomUUID().toString())
                .build();
                
        return ResponseEntity.accepted().body(response);
    }
}
