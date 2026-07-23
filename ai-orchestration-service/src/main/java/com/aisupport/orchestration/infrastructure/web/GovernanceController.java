package com.aisupport.orchestration.infrastructure.web;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.governance.GovernanceService;
import com.aisupport.orchestration.application.governance.dto.ActiveGuardrailDTO;
import com.aisupport.orchestration.application.governance.dto.ApprovalRequestDTO;
import com.aisupport.orchestration.application.governance.dto.AuditLogDTO;
import com.aisupport.orchestration.application.governance.dto.BlockedRequestDTO;
import com.aisupport.orchestration.application.governance.dto.GovernanceOverviewDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/governance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Governance Dashboard", description = "Endpoints for AI Governance & Trust Center")
public class GovernanceController {

    private final GovernanceService governanceService;

    @GetMapping("/overview")
    @Operation(summary = "Get governance overview metrics")
    public CompletableFuture<ResponseEntity<GovernanceOverviewDTO>> getOverview() {
        log.info("Fetching governance overview");
        return governanceService.getOverview().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/approval-queue")
    @Operation(summary = "Get pending human approvals")
    public CompletableFuture<ResponseEntity<List<ApprovalRequestDTO>>> getApprovalQueue() {
        log.info("Fetching governance approval queue");
        return governanceService.getApprovalQueue().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/blocked-requests")
    @Operation(summary = "Get guardrail blocked requests")
    public CompletableFuture<ResponseEntity<List<BlockedRequestDTO>>> getBlockedRequests() {
        log.info("Fetching governance blocked requests");
        return governanceService.getBlockedRequests().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get governance audit logs")
    public CompletableFuture<ResponseEntity<List<AuditLogDTO>>> getAuditLogs() {
        log.info("Fetching governance audit logs");
        return governanceService.getAuditLogs().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/active-guardrails")
    @Operation(summary = "Get active guardrails")
    public CompletableFuture<ResponseEntity<List<ActiveGuardrailDTO>>> getActiveGuardrails() {
        log.info("Fetching active guardrails");
        return governanceService.getActiveGuardrails().thenApply(ResponseEntity::ok);
    }
}
