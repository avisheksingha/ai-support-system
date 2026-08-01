package com.aisupport.orchestration.domain.workflow;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkflowExecutionResult {
    private final AnalysisResult analysis;
    private final RoutingDecision routing;
    private final KnowledgeContext knowledge;
}
