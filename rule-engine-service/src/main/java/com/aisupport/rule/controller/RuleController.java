package com.aisupport.rule.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.aisupport.rule.dto.RuleRequest;
import com.aisupport.rule.dto.RuleResponse;
import com.aisupport.rule.service.RuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/rules")
@CrossOrigin(origins = "http://localhost:8084")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rules", description = "Routing rule management endpoints")
public class RuleController {
    
    private final RuleService ruleService;
    
    @PostMapping
    @Operation(summary = "Create a new routing rule")
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleRequest request) {
        log.info("Received request to create rule: {}", request.getRuleName());
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get rule by ID")
    public ResponseEntity<RuleResponse> getRule(@PathVariable Long id) {
        RuleResponse response = ruleService.getRuleById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/name/{ruleName}")
    @Operation(summary = "Get rule by name")
    public ResponseEntity<RuleResponse> getRuleByName(@PathVariable String ruleName) {
        RuleResponse response = ruleService.getRuleByName(ruleName);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all rules")
    public ResponseEntity<List<RuleResponse>> getAllRules(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<RuleResponse> responses = activeOnly 
                ? ruleService.getActiveRules() 
                : ruleService.getAllRules();
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a rule")
    public ResponseEntity<RuleResponse> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody RuleRequest request) {
        RuleResponse response = ruleService.updateRule(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rule")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle rule active status")
    public ResponseEntity<RuleResponse> toggleRuleStatus(@PathVariable Long id) {
        RuleResponse response = ruleService.toggleRuleStatus(id);
        return ResponseEntity.ok(response);
    }
}
