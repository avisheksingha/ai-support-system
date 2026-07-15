package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.timeline.TimelineService;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;

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
}
