package com.aisupport.rule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.rule.dto.RuleEvaluationRequest;
import com.aisupport.rule.dto.RuleEvaluationResponse;
import com.aisupport.rule.service.RuleEvaluationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/rules/evaluate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rule Evaluation", description = "Rule evaluation endpoints")
public class RuleEvaluationController {
    
    private final RuleEvaluationService evaluationService;
    
    @PostMapping
    @Operation(summary = "Evaluate rules for a ticket")
    public ResponseEntity<RuleEvaluationResponse> evaluateRules(
            @Valid @RequestBody RuleEvaluationRequest request) {
        log.info("Received rule evaluation request for ticket ID: {}", request.getTicketId());
        RuleEvaluationResponse response = evaluationService.evaluateRules(request);
        return ResponseEntity.ok(response);
    }
}