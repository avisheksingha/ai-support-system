package com.aisupport.routing.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.routing.dto.RoutingResponse;
import com.aisupport.routing.service.RoutingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/v1/routing", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routing", description = "Endpoints for ticket routing and rule evaluation")
public class RoutingController {

    private final RoutingService routingService;

    @Operation(
        summary = "Get routing reasoning for a ticket",
        description = "Temporary internal endpoint. Retrieves the routing rule execution history and reasoning for a specific ticket. Will later be consumed by orchestration-service."
    )
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<RoutingResponse> getRoutingForTicket(
            @Parameter(description = "The ID of the ticket") @PathVariable Long ticketId) {
        log.info("REST request to get routing result for ticketId: {}", ticketId);
        RoutingResponse result = routingService.getRoutingForTicket(ticketId);
        return ResponseEntity.ok(result);
    }
}
