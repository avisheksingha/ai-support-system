package com.aisupport.orchestration.infrastructure.mcp.postgres;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolMetadata;
import com.aisupport.orchestration.infrastructure.mcp.exception.McpException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;

public class MockPostgresMcpClient implements McpClient {

    private final PostgresMcpProperties properties;
    private static final Pattern WRITE_OPERATIONS = Pattern.compile("(?i).*\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|CREATE|GRANT|REVOKE)\\b.*");

    public MockPostgresMcpClient(PostgresMcpProperties properties) {
        this.properties = properties;
    }

    @Override
    @CircuitBreaker(name = "postgres-mcp")
    @Retry(name = "postgres-mcp")
    @TimeLimiter(name = "postgres-mcp")
    @Timed(value = "mcp.discovery.latency", description = "Time taken to discover MCP tools")
    public CompletableFuture<List<McpToolMetadata>> discoverTools() {
        return CompletableFuture.supplyAsync(() -> List.of(
            McpToolMetadata.builder()
                .name("postgres.listTables")
                .description("Lists all allowed tables in the specified connection")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "connectionName", Map.of("type", "string")
                    ),
                    "required", List.of("connectionName")
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.describeTable")
                .description("Describes the schema (columns, types) of a specific table")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "connectionName", Map.of("type", "string"),
                        "tableName", Map.of("type", "string")
                    ),
                    "required", List.of("connectionName", "tableName")
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.queryTickets")
                .description("Queries support tickets in ticket-db safely")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "connectionName", Map.of("type", "string"),
                        "status", Map.of("type", "string")
                    ),
                    "required", List.of("connectionName")
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.queryWorkflowExecutions")
                .description("Queries workflow executions in orchestration-db safely")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "connectionName", Map.of("type", "string"),
                        "status", Map.of("type", "string")
                    ),
                    "required", List.of("connectionName")
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.executeReadOnlyQuery")
                .description("Executes a read-only SQL query against the specified connection with strict validation")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "connectionName", Map.of("type", "string"),
                        "query", Map.of("type", "string")
                    ),
                    "required", List.of("connectionName", "query")
                ))
                .build()
        ));
    }

    @Override
    @CircuitBreaker(name = "postgres-mcp")
    @Retry(name = "postgres-mcp")
    @TimeLimiter(name = "postgres-mcp")
    @Timed(value = "mcp.invocation.latency", description = "Time taken to execute an MCP tool")
    public CompletableFuture<Map<String, Object>> executeTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            String connectionName = (String) arguments.get("connectionName");
            if (connectionName == null || connectionName.isBlank()) {
                throw new McpException("Missing required parameter: connectionName");
            }

            PostgresConnectionConfig config = properties.getConnections().get(connectionName);
            if (config == null) {
                throw new McpException("Unknown connection name: " + connectionName);
            }

            long startTime = System.currentTimeMillis();
            Map<String, Object> result;

            switch (toolName) {
                case "postgres.listTables":
                    result = mockListTables(connectionName, config);
                    break;
                case "postgres.describeTable":
                    String tableName = (String) arguments.get("tableName");
                    validateTableAllowed(tableName, config);
                    result = mockDescribeTable(connectionName, tableName);
                    break;
                case "postgres.queryTickets":
                    result = mockQueryTickets(connectionName, config, (String) arguments.get("status"));
                    break;
                case "postgres.queryWorkflowExecutions":
                    result = mockQueryWorkflows(connectionName, config, (String) arguments.get("status"));
                    break;
                case "postgres.executeReadOnlyQuery":
                    String query = (String) arguments.get("query");
                    validateQuery(query, config);
                    result = mockExecuteQuery(connectionName, query);
                    break;
                default:
                    throw new McpException("Unknown tool: " + toolName);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            
            // Enrich result with metadata
            return Map.of(
                "connectionName", connectionName,
                "executionTimeMs", executionTime,
                "schema", config.getAllowedSchemas() != null && !config.getAllowedSchemas().isEmpty() ? config.getAllowedSchemas().get(0) : "public",
                "data", result.get("data"),
                "rowCount", result.get("rowCount"),
                "truncated", result.getOrDefault("truncated", false)
            );
        });
    }

    private void validateTableAllowed(String tableName, PostgresConnectionConfig config) {
        if (tableName == null || tableName.isBlank()) throw new McpException("Table name is required");
        
        List<String> allowedTables = config.getAllowedTables();
        if (allowedTables != null && !allowedTables.isEmpty() && !allowedTables.contains(tableName)) {
            throw new McpException("Permission denied: Table '" + tableName + "' is not in the allowed-tables list");
        }
    }

    private void validateQuery(String query, PostgresConnectionConfig config) {
        if (query == null || query.isBlank()) {
            throw new McpException("Query cannot be empty");
        }

        // 1. Single Statement Validation
        if (query.contains(";")) {
            throw new McpException("Validation failed: Multiple statements (;) are strictly prohibited.");
        }

        // 2. Read-Only Validation
        if (WRITE_OPERATIONS.matcher(query).matches()) {
            throw new McpException("Validation failed: Write operations (INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE, CREATE) are strictly prohibited.");
        }

        // 3. Allowed Tables Check (Simple mock parsing, for real implementation a SQL parser is needed)
        List<String> allowedTables = config.getAllowedTables();
        if (allowedTables != null && !allowedTables.isEmpty()) {
            boolean usesAllowedTable = allowedTables.stream().anyMatch(t -> query.contains(t));
            if (!usesAllowedTable) {
                // Warning rather than block here in mock, but for real, we enforce it tightly.
            }
        }
    }

    private Map<String, Object> mockListTables(String connectionName, PostgresConnectionConfig config) {
        if ("ticket-db".equals(connectionName)) {
            return Map.of("data", List.of("tickets", "agents", "customers"), "rowCount", 3);
        } else if ("orchestration-db".equals(connectionName)) {
            return Map.of("data", List.of("workflow_execution", "workflow_checkpoint", "tool_execution", "ai_execution_record"), "rowCount", 4);
        }
        return Map.of("data", List.of(), "rowCount", 0);
    }

    private Map<String, Object> mockDescribeTable(String connectionName, String tableName) {
        if ("tickets".equals(tableName)) {
            return Map.of(
                "data", List.of(
                    Map.of("column", "id", "type", "uuid"),
                    Map.of("column", "status", "type", "varchar"),
                    Map.of("column", "priority", "type", "varchar"),
                    Map.of("column", "created_at", "type", "timestamp")
                ),
                "rowCount", 4
            );
        }
        return Map.of("data", List.of(), "rowCount", 0);
    }

    private Map<String, Object> mockQueryTickets(String connectionName, PostgresConnectionConfig config, String status) {
        validateTableAllowed("tickets", config);
        List<Map<String, Object>> tickets = List.of(
            Map.of("id", "101", "status", "OPEN", "priority", "HIGH"),
            Map.of("id", "102", "status", "CLOSED", "priority", "LOW")
        );
        return Map.of("data", tickets, "rowCount", 2, "truncated", false);
    }

    private Map<String, Object> mockQueryWorkflows(String connectionName, PostgresConnectionConfig config, String status) {
        validateTableAllowed("workflow_execution", config);
        List<Map<String, Object>> workflows = List.of(
            Map.of("id", "wf-1", "status", "WAITING_APPROVAL", "workflow_type", "refund"),
            Map.of("id", "wf-2", "status", "COMPLETED", "workflow_type", "password_reset")
        );
        return Map.of("data", workflows, "rowCount", 2, "truncated", false);
    }

    private Map<String, Object> mockExecuteQuery(String connectionName, String query) {
        // Enforce mock limit of 100
        List<Map<String, Object>> results = List.of(
            Map.of("mock_column", "mock_value_1"),
            Map.of("mock_column", "mock_value_2")
        );
        return Map.of("data", results, "rowCount", 2, "truncated", false); // Default false, mock truncated if rowCount > 100
    }
}
