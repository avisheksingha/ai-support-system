package com.aisupport.routing.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.routing.dto.RoutingRuleDTO;
import com.aisupport.routing.dto.RoutingRuleRequestDTO;
import com.aisupport.routing.dto.RoutingRuleStatsDTO;
import com.aisupport.routing.service.RoutingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/v1/routing/rules", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routing Rules Management", description = "Admin APIs for managing ticket routing and assignment rules")
public class RoutingRuleAdminController {

    private static final String DEFAULT_ADMIN_USER = "admin";

    private final RoutingService routingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Search and list routing rules", description = "List routing rules with pagination, search, active filter, and team filter")
    public ResponseEntity<Page<RoutingRuleDTO>> getRoutingRules(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String team,
            @PageableDefault(page = 0, size = 10, sort = "priority", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to list routing rules with search={}, active={}, team={}", search, active, team);
        return ResponseEntity.ok(routingService.searchRules(search, active, team, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get routing rule by ID")
    public ResponseEntity<RoutingRuleDTO> getRoutingRuleById(@PathVariable Long id) {
        log.info("REST request to get routing rule by ID: {}", id);
        return ResponseEntity.ok(routingService.getRuleDTOById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new routing rule")
    public ResponseEntity<RoutingRuleDTO> createRoutingRule(
            @Valid @RequestBody RoutingRuleRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : DEFAULT_ADMIN_USER;
        log.info("REST request to create routing rule by: {}", username);
        RoutingRuleDTO created = routingService.createRule(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing routing rule")
    public ResponseEntity<RoutingRuleDTO> updateRoutingRule(
            @PathVariable Long id,
            @Valid @RequestBody RoutingRuleRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : DEFAULT_ADMIN_USER;
        log.info("REST request to update routing rule ID {} by: {}", id, username);
        RoutingRuleDTO updated = routingService.updateRule(id, request, username);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle active status of a routing rule")
    public ResponseEntity<RoutingRuleDTO> toggleRoutingRuleActive(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : DEFAULT_ADMIN_USER;
        log.info("REST request to toggle active status for rule ID {} by: {}", id, username);
        RoutingRuleDTO toggled = routingService.toggleRuleActive(id, username);
        return ResponseEntity.ok(toggled);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a routing rule")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        log.info("REST request to delete routing rule ID: {}", id);
        routingService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get routing rules aggregate statistics")
    public ResponseEntity<RoutingRuleStatsDTO> getRoutingRuleStats() {
        log.info("REST request to get routing rule stats");
        return ResponseEntity.ok(routingService.getRuleStats());
    }
}
