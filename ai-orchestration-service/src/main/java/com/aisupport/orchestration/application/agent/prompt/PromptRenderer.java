package com.aisupport.orchestration.application.agent.prompt;

import java.util.Map;

public interface PromptRenderer {
    String render(String templateContent, Map<String, Object> variables);
}
