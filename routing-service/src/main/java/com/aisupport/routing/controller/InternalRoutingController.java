package com.aisupport.routing.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/routing", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class InternalRoutingController {

    private final RoutingService routingService;

    @PostMapping("/route")
    public ResponseEntity<TicketRoutedEvent> routeTicket(@RequestBody TicketAnalyzedEvent event) {
        log.info("Internal REST request to route ticketId: {}", event.getTicketId());
        
        TicketRoutedEvent response = routingService.routeSync(event);
                
        return ResponseEntity.ok(response);
    }
}
