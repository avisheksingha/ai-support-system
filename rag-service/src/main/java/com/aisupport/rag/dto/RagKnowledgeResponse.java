package com.aisupport.rag.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagKnowledgeResponse {
    private Long ticketId;
    private String query;
    private String generatedReply;
    private Double similarityScore;
    private List<String> sourceDocuments;
    private String modelUsed;
    private Instant generatedAt;
}
