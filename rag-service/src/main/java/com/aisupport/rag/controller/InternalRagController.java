package com.aisupport.rag.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.rag.service.RagService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class InternalRagController {

    private final RagService ragService;

    @PostMapping("/search")
    public ResponseEntity<RagSearchResponse> searchKnowledge(@RequestBody RagSearchRequest request) {
        log.info("Internal REST request for RAG search on ticketId: {}", request.getTicketId());
        
        String responseText = ragService.generateResponseSync(
                request.getTicketId(), 
                request.getQuery());
                
        return ResponseEntity.ok(new RagSearchResponse(responseText));
    }

    @Data
    public static class RagSearchRequest {
        private Long ticketId;
        private String query;
    }
    
    @Data
    public static class RagSearchResponse {
        private final String response;
    }
}
