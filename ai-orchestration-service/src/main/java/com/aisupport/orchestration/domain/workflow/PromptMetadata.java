package com.aisupport.orchestration.domain.workflow;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptMetadata {
    private String templateName;
    private String promptVersion;
    private String promptHash;
    private String modelId;
}
