package com.aisupport.orchestration.application.workflow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

@Component
public class StepExecutor {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeStep(WorkflowStep step, WorkflowContext context) {
        step.before(context);
        try {
            step.execute(context);
        } catch (Exception e) {
            step.rollback(context);
            throw e;
        } finally {
            step.after(context);
        }
    }
}
