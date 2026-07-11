package com.aisupport.orchestration.application.workflow.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.WorkflowFactory;
import com.aisupport.orchestration.domain.workflow.WorkflowDefinition;

@Component
public class WorkflowFactoryImpl implements WorkflowFactory {

    private final Map<String, WorkflowDefinition> workflowMap;

    public WorkflowFactoryImpl(List<WorkflowDefinition> definitions) {
        this.workflowMap = definitions.stream()
                .collect(Collectors.toMap(WorkflowDefinition::getId, Function.identity()));
    }

    @Override
    public WorkflowDefinition create(String workflowId) {
        return workflowMap.get(workflowId);
    }
}
