package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolMetadata;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;

public class RealFilesystemMcpClient implements McpClient {

    @Override
    @CircuitBreaker(name = "filesystem-mcp")
    @Retry(name = "filesystem-mcp")
    @TimeLimiter(name = "filesystem-mcp")
    @Timed(value = "mcp.discovery.latency", description = "Time taken to discover MCP tools")
    public CompletableFuture<List<McpToolMetadata>> discoverTools() {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Real Filesystem MCP Client not yet implemented"));
    }

    @Override
    @CircuitBreaker(name = "filesystem-mcp")
    @Retry(name = "filesystem-mcp")
    @TimeLimiter(name = "filesystem-mcp")
    @Timed(value = "mcp.invocation.latency", description = "Time taken to execute an MCP tool")
    public CompletableFuture<Map<String, Object>> executeTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Real Filesystem MCP Client not yet implemented"));
    }
}

