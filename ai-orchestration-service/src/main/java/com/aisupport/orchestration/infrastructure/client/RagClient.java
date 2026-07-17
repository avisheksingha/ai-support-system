package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.orchestration.domain.model.Result;

public interface RagClient {
    Result<KnowledgeContext> searchKnowledge(Long ticketId, String query);
    Result<KnowledgeContext> getRagResponse(Long ticketId);
}
