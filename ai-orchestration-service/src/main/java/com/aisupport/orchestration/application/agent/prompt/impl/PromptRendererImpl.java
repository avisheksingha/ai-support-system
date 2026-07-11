package com.aisupport.orchestration.application.agent.prompt.impl;

import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.agent.prompt.PromptRenderer;

@Component
public class PromptRendererImpl implements PromptRenderer {
    @Override
    public String render(String templateContent, Map<String, Object> variables) {
        PromptTemplate template = new PromptTemplate(templateContent);
        return template.render(variables);
    }
}
