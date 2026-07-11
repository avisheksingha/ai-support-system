package com.aisupport.orchestration.application.agent;

import java.util.List;
import java.util.Map;

import com.aisupport.orchestration.domain.context.ConversationContext;
import com.aisupport.orchestration.domain.context.KnowledgeContext;
import com.aisupport.orchestration.domain.model.ModelProfile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentRequest {
    private final String promptVersion;
    private final String systemPrompt;
    private final String userPrompt;
    private final ConversationContext conversation;
    private final KnowledgeContext knowledge;
    
    private final ModelProfile modelProfile;
    private final Map<String, Object> parameters;
    
    private final List<String> allowedCapabilities;
    private final Map<String, Object> metadata;
}
