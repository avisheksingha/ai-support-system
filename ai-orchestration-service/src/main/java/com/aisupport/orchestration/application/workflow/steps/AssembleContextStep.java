package com.aisupport.orchestration.application.workflow.steps;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.context.ContextProvider;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssembleContextStep implements WorkflowStep {

    private final List<ContextProvider> providers;

    @Override
    public String getName() {
        return "AssembleContextStep";
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Assembling context for workflow: {}", context.getWorkflowId());
        for (ContextProvider provider : providers) {
            if (provider.supports(context.getWorkflowId())) {
                log.debug("Populating context using provider: {}", provider.getClass().getSimpleName());
                provider.populate(context);
            }
        }
    }
}
