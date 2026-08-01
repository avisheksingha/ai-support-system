package com.aisupport.orchestration.domain.workflow;

public final class WorkflowStepConstants {
    private WorkflowStepConstants() {}
    public static final String ASSEMBLE_CONTEXT = "AssembleContextStep";
    public static final String ANALYZE_TICKET = "AnalyzeTicketStep";
    public static final String KNOWLEDGE_SEARCH = "KnowledgeSearchStep";
    public static final String ROUTE_TICKET = "RouteTicketStep";
    public static final String FINAL_AI_DECISION = "FinalAiDecisionStep";

    // Infrastructure & Sub-Steps
    public static final String EVENT_PUBLICATION = "Event Publication";
    public static final String PERSISTENCE = "Persistence";
    public static final String FAILURE = "FAILURE";
    public static final String COMPLETED = "COMPLETED";
    public static final String PROMPT_BUILDER = "Prompt Builder";
    public static final String GUARDRAILS = "Guardrails";
}
