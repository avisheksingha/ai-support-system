package com.aisupport.orchestration.application.agent;

import com.aisupport.orchestration.domain.model.Result;

public interface Agent {
    Result<AgentSession> execute(AgentRequest request);
}
