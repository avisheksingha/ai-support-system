package com.aisupport.orchestration.application.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentSession {
    private final String sessionId;
    private final AgentRequest initialRequest;
    
    @Builder.Default
    private final List<AgentResponse.ToolCallRequest> toolInvocations = new ArrayList<>();
    
    @Builder.Default
    private final List<AgentResponse> intermediateResponses = new ArrayList<>();
    
    private AgentResponse finalResponse;
    private AgentResponse.UsageData totalUsage;
    
    private String policyId;
    private String policyVersion;
    private String guardrailId;
    private String guardrailVersion;
    private String failureReason;
    
    private final Instant startedAt;
    private Instant completedAt;
    
    public void addToolInvocation(AgentResponse.ToolCallRequest toolCall) {
        this.toolInvocations.add(toolCall);
    }
    
    public void addIntermediateResponse(AgentResponse response) {
        this.intermediateResponses.add(response);
    }
    
    public void complete(AgentResponse finalResponse) {
        this.finalResponse = finalResponse;
        this.completedAt = Instant.now();
        this.totalUsage = finalResponse.getUsage();
    }
}
