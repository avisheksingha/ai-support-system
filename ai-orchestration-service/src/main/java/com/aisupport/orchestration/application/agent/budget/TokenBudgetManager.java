package com.aisupport.orchestration.application.agent.budget;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.AgentRequest;
import com.aisupport.orchestration.domain.model.ModelProfile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenBudgetManager {

    public AgentRequest applyBudget(AgentRequest request) {
        ModelProfile profile = request.getModelProfile();
        if (profile == null) {
            return request;
        }

        int maxTokens = profile.getMaxContextTokens();
        
        // Pseudo-logic for token estimation: 4 chars per token
        int estimatedTokens = 0;
        if (request.getSystemPrompt() != null) {
            estimatedTokens += request.getSystemPrompt().length() / 4;
        }
        
        if (estimatedTokens > maxTokens) {
            log.warn("Prompt exceeds token limit of {}. Truncating context...", maxTokens);
            // Truncation logic goes here
        }
        
        return request;
    }
}
