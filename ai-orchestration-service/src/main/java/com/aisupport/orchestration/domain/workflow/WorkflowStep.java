package com.aisupport.orchestration.domain.workflow;

public interface WorkflowStep {
    String getName();
    
    default boolean supports(WorkflowContext context) {
        return true;
    }
    
    default void before(WorkflowContext context) {
        // Default implementation
    }
    
    void execute(WorkflowContext context);
    
    default void after(WorkflowContext context) {
        // Default implementation
    }
    
    default void rollback(WorkflowContext context) {
        // Default implementation
    }
}
