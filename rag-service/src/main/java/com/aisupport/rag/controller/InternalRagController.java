package com.aisupport.rag.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.rag.dto.internal.RagSearchRequest;
import com.aisupport.rag.dto.internal.RagSearchResponse;
import com.aisupport.rag.service.RagService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal RAG", description = "Internal endpoints for orchestration service")
public class InternalRagController {

    private final RagService ragService;

    @PostMapping("/search")
    public ResponseEntity<RagSearchResponse> searchKnowledge(@Valid @RequestBody RagSearchRequest request) {
        
        int queryLength = request.getQuery() != null ? request.getQuery().length() : 0;
        log.info("Internal REST request for RAG search. ticketId={}, queryLength={}", request.getTicketId(), queryLength);
        
        String responseText = ragService.generateResponseSync(
                request.getTicketId(), 
                request.getQuery());
                
        return ResponseEntity.ok(RagSearchResponse.builder()
                .answer(responseText)
                .build());
    }
}
