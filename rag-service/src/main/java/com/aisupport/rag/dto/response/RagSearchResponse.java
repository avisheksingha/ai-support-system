package com.aisupport.rag.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagSearchResponse {
    private String answer;
    private boolean knowledgeFound;
    private String model;
    private Integer retrievedDocumentCount;
    private List<String> matchedArticleTitles;
}
