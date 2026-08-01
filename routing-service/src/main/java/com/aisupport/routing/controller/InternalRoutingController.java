package com.aisupport.routing.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.dto.response.RoutingResponse;
import com.aisupport.routing.service.RoutingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/routing", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Routing", description = "Internal endpoints for orchestration service")
public class InternalRoutingController {

    private final RoutingService routingService;

    @PostMapping("/route")
    public ResponseEntity<TicketRoutedEvent> routeTicket(@RequestBody TicketAnalyzedEvent event) {
        log.info("Internal REST request to route ticketId: {}", event.getTicketId());
        
        TicketRoutedEvent response = routingService.routeSync(event);
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<RoutingResponse> getRoutingByTicketId(@PathVariable Long ticketId) {
        log.info("Internal REST request to fetch routing for ticketId: {}", ticketId);
        RoutingResponse result = routingService.getRoutingForTicket(ticketId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
