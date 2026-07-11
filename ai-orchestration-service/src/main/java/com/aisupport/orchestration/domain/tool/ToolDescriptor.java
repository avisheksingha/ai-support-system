package com.aisupport.orchestration.domain.tool;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolDescriptor {
    private String name;
    private String description;
    private Map<String, Class<?>> parameters;
    private Class<?> returnType;
    private String version;
}
