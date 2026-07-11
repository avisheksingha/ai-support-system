package com.aisupport.orchestration.infrastructure.mcp.github;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolMetadata;
import com.aisupport.orchestration.infrastructure.mcp.exception.McpException;
import com.aisupport.orchestration.infrastructure.mcp.exception.McpTransientException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;

public class MockGithubMcpClient implements McpClient {

    private static final String TYPE_OBJECT = "object";
    private static final String TYPE_STRING = "string";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_STATE = "state";


    private final Random random = new Random();

    @Override
    @CircuitBreaker(name = "github-mcp")
    @Retry(name = "github-mcp")
    @TimeLimiter(name = "github-mcp")
    @Timed(value = "mcp.discovery.latency", description = "Time taken to discover MCP tools")
    public CompletableFuture<List<McpToolMetadata>> discoverTools() {
        return CompletableFuture.supplyAsync(() -> {
            simulateNetworkConditions();
            
            return List.of(
                McpToolMetadata.builder()
                    .name("github.searchRepository")
                    .description("Searches a GitHub repository for code or text")
                    .inputSchema(Map.of(
                        "type", TYPE_OBJECT,
                        KEY_PROPERTIES, Map.of(
                            "query", Map.of("type", TYPE_STRING)
                        )
                    ))
                    .build(),
                McpToolMetadata.builder()
                    .name("github.readFile")
                    .description("Reads a specific file from a GitHub repository")
                    .inputSchema(Map.of(
                        "type", TYPE_OBJECT,
                        KEY_PROPERTIES, Map.of(
                            "path", Map.of("type", TYPE_STRING)
                        )
                    ))
                    .build(),
                McpToolMetadata.builder()
                    .name("github.searchIssues")
                    .description("Searches issues in the GitHub repository")
                    .inputSchema(Map.of(
                        "type", TYPE_OBJECT,
                        KEY_PROPERTIES, Map.of(
                            KEY_STATE, Map.of("type", TYPE_STRING)
                        )
                    ))
                    .build(),
                McpToolMetadata.builder()
                    .name("github.readPullRequest")
                    .description("Reads details of a specific Pull Request")
                    .inputSchema(Map.of(
                        "type", TYPE_OBJECT,
                        KEY_PROPERTIES, Map.of(
                            "prNumber", Map.of("type", "number")
                        )
                    ))
                    .build()
            );
        });
    }

    @Override
    @CircuitBreaker(name = "github-mcp")
    @Retry(name = "github-mcp")
    @TimeLimiter(name = "github-mcp")
    @Timed(value = "mcp.invocation.latency", description = "Time taken to execute an MCP tool")
    public CompletableFuture<Map<String, Object>> executeTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            simulateNetworkConditions();
            
            return switch (toolName) {
                case "github.searchRepository" -> Map.of(
                    "repository", "avisheksingha/ai-support-system",
                    "path", "src/main/java/App.java",
                    "branch", "main",
                    "snippet", "public class App { ... }",
                    "score", 0.95
                );
                case "github.readFile" -> {
                    String path = (String) arguments.get("path");
                    if (path == null) throw new McpException("Missing required parameter: path");
                    yield Map.of(
                        "path", path,
                        "content", "// Mock content for " + path,
                        "sizeBytes", 1024
                    );
                }
                case "github.searchIssues" -> Map.of(
                    "title", "Add MCP Integration",
                    "labels", List.of("enhancement", "phase-5"),
                    KEY_STATE, "open",
                    "assignee", "avisheksingha",
                    "url", "https://github.com/avisheksingha/ai-support-system/issues/42"
                );
                case "github.readPullRequest" -> Map.of(
                    "number", arguments.getOrDefault("prNumber", 1),
                    "title", "Refactor ToolRegistry",
                    KEY_STATE, "merged",
                    "merged_by", "avisheksingha",
                    "url", "https://github.com/avisheksingha/ai-support-system/pull/1"
                );
                default -> throw new McpException("Unknown tool: " + toolName);
            };
        });
    }
    
    private void simulateNetworkConditions() {
        try {
            TimeUnit.MILLISECONDS.sleep(50L + random.nextInt(150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int r = random.nextInt(100);
        if (r < 5) {
            try {
                TimeUnit.SECONDS.sleep(2);
                throw new McpTransientException("Simulated connection timeout to GitHub MCP");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else if (r < 10) {
            throw new McpTransientException("HTTP 503: GitHub API rate limit or unavailability");
        }
    }
}
