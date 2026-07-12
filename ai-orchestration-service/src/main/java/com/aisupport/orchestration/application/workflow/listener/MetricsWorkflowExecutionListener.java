package com.aisupport.orchestration.application.workflow.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Component
@Order(20)
@RequiredArgsConstructor
public class MetricsWorkflowExecutionListener implements WorkflowExecutionListener {

    private static final String TAG_WORKFLOW_ID = "workflowId";
    private static final String TAG_RESULT = "result";
    private static final String UNKNOWN_WORKFLOW_ID = "unknown";

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer.Sample> workflowTimers = new ConcurrentHashMap<>();
    private final Map<String, Timer.Sample> stepTimers = new ConcurrentHashMap<>();

    @Override
    public void beforeWorkflow(WorkflowContext context) {
        workflowTimers.put(context.getExecutionId(), Timer.start(meterRegistry));
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        String workflowId = context.getWorkflowId() != null ? context.getWorkflowId() : UNKNOWN_WORKFLOW_ID;
        Timer.Sample sample = workflowTimers.remove(context.getExecutionId());
        if (sample != null) {
            sample.stop(meterRegistry.timer("workflow.duration", 
                TAG_WORKFLOW_ID, workflowId, 
                TAG_RESULT, "success"));
        }
        meterRegistry.counter("workflow.success", TAG_WORKFLOW_ID, workflowId).increment();
    }

    @Override
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        String key = context.getExecutionId() + "-" + step.getName();
        stepTimers.put(key, Timer.start(meterRegistry));
    }

    @Override
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        String workflowId = context.getWorkflowId() != null ? context.getWorkflowId() : UNKNOWN_WORKFLOW_ID;
        String key = context.getExecutionId() + "-" + step.getName();
        Timer.Sample sample = stepTimers.remove(key);
        if (sample != null) {
            sample.stop(meterRegistry.timer("step.duration", 
                TAG_WORKFLOW_ID, workflowId, 
                "step", step.getName(), 
                TAG_RESULT, "success"));
        }
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable error) {
        String workflowId = context.getWorkflowId() != null ? context.getWorkflowId() : UNKNOWN_WORKFLOW_ID;
        Timer.Sample sample = workflowTimers.remove(context.getExecutionId());
        if (sample != null) {
            sample.stop(meterRegistry.timer("workflow.duration", 
                TAG_WORKFLOW_ID, workflowId, 
                TAG_RESULT, "failure"));
        }
        meterRegistry.counter("workflow.failure", TAG_WORKFLOW_ID, workflowId).increment();
    }
}
