package com.aisupport.orchestration.application.agent.evaluation;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AiRegressionScenario {
    private String scenarioName;
    private Long ticketId;
    private String subject;
    private List<Message> history;
    private List<Article> knowledgeArticles;
    private String expectedPromptSnippet;
    private String mockLlmResponse;
    private Map<String, Object> expectedExtracts;
    private boolean expectPolicyBlock;

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    public static class Article {
        private String title;
        private String content;
    }
}
