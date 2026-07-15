package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIActionRequest {
    private String actionType; // e.g., GENERATE_DRAFT, RE_ANALYZE
    private String instructions;
}
