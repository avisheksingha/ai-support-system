package com.aisupport.rag.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagSearchResponse {
    private String answer;
    private boolean knowledgeFound;
    private String model;
}
