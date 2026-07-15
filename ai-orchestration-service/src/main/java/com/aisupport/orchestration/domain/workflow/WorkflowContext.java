package com.aisupport.orchestration.domain.workflow;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowContext {
    private String workflowId;
    private Integer workflowVersion;
    private String executionId;
    private String correlationId;
    private Long ticketId;
    private String conversationId;

    @Builder.Default
    private Instant startTime = Instant.now();

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @Builder.Default
    private Map<Class<?>, Object> resources = new HashMap<>();

    private WorkflowExecutionResult result;

    public void putAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public <T> void putResource(Class<T> type, T resource) {
        this.resources.put(type, resource);
    }

    public <T> T getResource(Class<T> type) {
        return type.cast(this.resources.get(type));
    }

    public long getExecutionDuration() {
        return Duration.between(startTime, Instant.now()).toMillis();
    }
}
