package com.aisupport.routing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.routing.dto.RoutingRequest;
import com.aisupport.routing.dto.RoutingResponse;
import com.aisupport.routing.service.RoutingOrchestrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/routing")
@CrossOrigin(origins = "http://localhost:8085")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routing", description = "Ticket routing orchestration endpoints using WebClient")
public class RoutingController {
    
    private final RoutingOrchestrationService routingService;
    
    @PostMapping("/route")
    @Operation(summary = "Route a ticket through the complete workflow")
    public ResponseEntity<RoutingResponse> routeTicket(
            @Valid @RequestBody RoutingRequest request) {
        log.info("Received routing request for ticket ID: {}", request.getTicketId());
        RoutingResponse response = routingService.routeTicket(request);
        return ResponseEntity.ok(response);
    }
}