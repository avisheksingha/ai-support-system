package com.aisupport.orchestration.domain.workflow;

import com.aisupport.common.event.AiDecision;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketContextSnapshot {
    @Builder.Default
    private Integer schemaVersion = 1;
    
    private Long ticketId;
    private String workflowExecutionId;
    private AnalysisResult analysisResult;
    private KnowledgeContext knowledgeContext;
    private RoutingDecision routingDecision;
    private PromptMetadata prompt;
    private AiDecision aiDecision;
}
