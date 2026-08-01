package com.aisupport.orchestration.application.workflow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.workflow.WorkflowDefinition;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowTriggerRegistryImpl implements WorkflowTriggerRegistry {

    private final List<WorkflowDefinition> definitions;
    private final Map<String, String> triggerToWorkflowIdMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (WorkflowDefinition def : definitions) {
            String trigger = def.getSupportedTrigger();
            if (trigger != null) {
                registerTrigger(trigger, def.getId());
            }
        }
    }

    @Override
    public void registerTrigger(String triggerEventName, String workflowId) {
        triggerToWorkflowIdMap.put(triggerEventName, workflowId);
    }

    @Override
    public String getWorkflowIdForTrigger(String triggerEventName) {
        return triggerToWorkflowIdMap.get(triggerEventName);
    }
}
