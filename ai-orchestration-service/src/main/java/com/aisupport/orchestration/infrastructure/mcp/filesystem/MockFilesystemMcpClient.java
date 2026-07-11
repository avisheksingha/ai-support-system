package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolMetadata;
import com.aisupport.orchestration.infrastructure.mcp.exception.McpException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;

public class MockFilesystemMcpClient implements McpClient {

    private static final String TYPE_OBJECT = "object";
    private static final String TYPE_STRING = "string";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_PATH = "path";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_EXTENSION = "extension";


    private final FilesystemMcpProperties properties;

    public MockFilesystemMcpClient(FilesystemMcpProperties properties) {
        this.properties = properties;
    }

    @Override
    @CircuitBreaker(name = "filesystem-mcp")
    @Retry(name = "filesystem-mcp")
    @TimeLimiter(name = "filesystem-mcp")
    @Timed(value = "mcp.discovery.latency", description = "Time taken to discover MCP tools")
    public CompletableFuture<List<McpToolMetadata>> discoverTools() {
        return CompletableFuture.supplyAsync(() -> List.of(
            McpToolMetadata.builder()
                .name("filesystem.readMarkdown")
                .description("Reads a markdown documentation file from the workspace")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_PATH, Map.of("type", TYPE_STRING)
                    )
                ))
                .build(),
            McpToolMetadata.builder()
                .name("filesystem.readConfiguration")
                .description("Reads configuration files (yml/properties)")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_PATH, Map.of("type", TYPE_STRING)
                    )
                ))
                .build(),
            McpToolMetadata.builder()
                .name("filesystem.readLogs")
                .description("Reads application log files safely")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_PATH, Map.of("type", TYPE_STRING)
                    )
                ))
                .build(),
            McpToolMetadata.builder()
                .name("filesystem.readPrompt")
                .description("Reads AI prompt templates from the workspace")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_PATH, Map.of("type", TYPE_STRING)
                    )
                ))
                .build()
        ));
    }

    @Override
    @CircuitBreaker(name = "filesystem-mcp")
    @Retry(name = "filesystem-mcp")
    @TimeLimiter(name = "filesystem-mcp")
    @Timed(value = "mcp.invocation.latency", description = "Time taken to execute an MCP tool")
    public CompletableFuture<Map<String, Object>> executeTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            String requestedPath = (String) arguments.get(KEY_PATH);
            if (requestedPath == null || requestedPath.isBlank()) {
                throw new McpException("Missing required parameter: path");
            }

            validatePath(requestedPath);

            return switch (toolName) {
                case "filesystem.readMarkdown" -> Map.of(
                    KEY_PATH, requestedPath,
                    KEY_CONTENT, "# Mock Markdown Content\n\nThis is a simulation of " + requestedPath,
                    KEY_METADATA, Map.of("size", 2048, KEY_EXTENSION, "md")
                );
                case "filesystem.readConfiguration" -> Map.of(
                    KEY_PATH, requestedPath,
                    KEY_CONTENT, "mock.property=value\nmock.enabled=true",
                    KEY_METADATA, Map.of("size", 512, KEY_EXTENSION, KEY_PROPERTIES)
                );
                case "filesystem.readLogs" -> Map.of(
                    KEY_PATH, requestedPath,
                    KEY_CONTENT, "[INFO] 2026-07-11 10:00:00 - Mock log entry from " + requestedPath,
                    KEY_METADATA, Map.of("size", 4096, KEY_EXTENSION, "log")
                );
                case "filesystem.readPrompt" -> Map.of(
                    KEY_PATH, requestedPath,
                    KEY_CONTENT, "You are an AI assistant. Context: {context}",
                    KEY_METADATA, Map.of("size", 1024, KEY_EXTENSION, "txt")
                );
                default -> throw new McpException("Unknown tool: " + toolName);
            };
        });
    }
    
    private void validatePath(String requestedPath) {
        String workspaceRoot = properties.getWorkspaceRoot();
        if (workspaceRoot == null || workspaceRoot.isBlank()) {
            throw new McpException("Filesystem MCP misconfigured: workspaceRoot is null");
        }

        List<String> allowedPaths = properties.getAllowedPaths();
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            throw new McpException("Filesystem MCP misconfigured: no allowed paths defined");
        }

        // Normalize requested path
        Path rootPath = Paths.get(workspaceRoot).toAbsolutePath().normalize();
        Path resolvedPath = rootPath.resolve(requestedPath).normalize();

        // 1. Must not escape workspace root (prevents ../ traversal outside workspace)
        if (!resolvedPath.startsWith(rootPath)) {
            throw new McpException("Path traversal violation: Cannot access paths outside workspace root");
        }

        // 2. Must start with one of the allowed paths
        boolean isAllowed = false;
        for (String allowed : allowedPaths) {
            Path allowedPath = rootPath.resolve(allowed).normalize();
            if (resolvedPath.startsWith(allowedPath) || resolvedPath.equals(allowedPath)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new McpException("Permission denied: Path " + requestedPath + " is not within allowed workspace paths");
        }
        
        // Simulating File Not Found
        if (requestedPath.contains("missing")) {
            throw new McpException("File not found: " + requestedPath);
        }
        
        // Simulating unsupported extension (simple mock logic)
        if (requestedPath.endsWith(".exe") || requestedPath.endsWith(".dll")) {
            throw new McpException("Unsupported file extension: " + requestedPath);
        }
    }
}
