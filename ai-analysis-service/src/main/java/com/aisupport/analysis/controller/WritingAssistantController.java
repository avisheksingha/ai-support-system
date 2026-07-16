package com.aisupport.analysis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.analysis.dto.request.WritingImproveRequest;
import com.aisupport.analysis.dto.response.WritingImproveResponse;
import com.aisupport.analysis.service.WritingAssistantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/analysis/writing")
@Tag(name = "Writing Assistant", description = "Endpoints for AI-powered writing improvements")
public class WritingAssistantController {

    private final WritingAssistantService writingAssistantService;

    public WritingAssistantController(WritingAssistantService writingAssistantService) {
        this.writingAssistantService = writingAssistantService;
    }

    @PostMapping("/improve")
    @Operation(
        summary = "Improve writing quality",
        description = "Provides grammar and clarity improvements using AI while preserving the original intent."
    )
    public ResponseEntity<WritingImproveResponse> improveWriting(@Valid @RequestBody WritingImproveRequest request) {
        WritingImproveResponse response = writingAssistantService.improve(request);
        return ResponseEntity.ok(response);
    }
}
