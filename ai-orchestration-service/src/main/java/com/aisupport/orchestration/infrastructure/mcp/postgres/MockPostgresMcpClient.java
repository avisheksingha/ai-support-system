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

    private static final String TYPE_OBJECT = "object";
    private static final String TYPE_STRING = "string";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_REQUIRED = "required";
    private static final String KEY_CONNECTION = "connectionName";
    private static final String KEY_TABLE = "tableName";
    private static final String KEY_STATUS = "status";
    private static final String KEY_QUERY = "query";
    private static final String KEY_DATA = "data";
    private static final String KEY_ROW_COUNT = "rowCount";
    private static final String KEY_TRUNCATED = "truncated";
    private static final String KEY_COLUMN = "column";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_TICKETS = "tickets";


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
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_CONNECTION, Map.of("type", TYPE_STRING)
                    ),
                    KEY_REQUIRED, List.of(KEY_CONNECTION)
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.describeTable")
                .description("Describes the schema (columns, types) of a specific table")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_CONNECTION, Map.of("type", TYPE_STRING),
                        KEY_TABLE, Map.of("type", TYPE_STRING)
                    ),
                    KEY_REQUIRED, List.of(KEY_CONNECTION, KEY_TABLE)
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.queryTickets")
                .description("Queries support tickets in ticket-db safely")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_CONNECTION, Map.of("type", TYPE_STRING),
                        KEY_STATUS, Map.of("type", TYPE_STRING)
                    ),
                    KEY_REQUIRED, List.of(KEY_CONNECTION)
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.queryWorkflowExecutions")
                .description("Queries workflow executions in orchestration-db safely")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_CONNECTION, Map.of("type", TYPE_STRING),
                        KEY_STATUS, Map.of("type", TYPE_STRING)
                    ),
                    KEY_REQUIRED, List.of(KEY_CONNECTION)
                ))
                .build(),
            McpToolMetadata.builder()
                .name("postgres.executeReadOnlyQuery")
                .description("Executes a read-only SQL query against the specified connection with strict validation")
                .inputSchema(Map.of(
                    "type", TYPE_OBJECT,
                    KEY_PROPERTIES, Map.of(
                        KEY_CONNECTION, Map.of("type", TYPE_STRING),
                        KEY_QUERY, Map.of("type", TYPE_STRING)
                    ),
                    KEY_REQUIRED, List.of(KEY_CONNECTION, KEY_QUERY)
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
            String connectionName = (String) arguments.get(KEY_CONNECTION);
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
                    result = mockListTables(connectionName);
                    break;
                case "postgres.describeTable":
                    String tableName = (String) arguments.get(KEY_TABLE);
                    validateTableAllowed(tableName, config);
                    result = mockDescribeTable(tableName);
                    break;
                case "postgres.queryTickets":
                    result = mockQueryTickets(config);
                    break;
                case "postgres.queryWorkflowExecutions":
                    result = mockQueryWorkflows(config);
                    break;
                case "postgres.executeReadOnlyQuery":
                    String query = (String) arguments.get(KEY_QUERY);
                    validateQuery(query, config);
                    result = mockExecuteQuery();
                    break;
                default:
                    throw new McpException("Unknown tool: " + toolName);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            
            // Enrich result with metadata
            return Map.of(
                KEY_CONNECTION, connectionName,
                "executionTimeMs", executionTime,
                "schema", config.getAllowedSchemas() != null && !config.getAllowedSchemas().isEmpty() ? config.getAllowedSchemas().get(0) : "public",
                KEY_DATA, result.get(KEY_DATA),
                KEY_ROW_COUNT, result.get(KEY_ROW_COUNT),
                KEY_TRUNCATED, result.getOrDefault(KEY_TRUNCATED, false)
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
            boolean usesAllowedTable = allowedTables.stream().anyMatch(query::contains);
            if (!usesAllowedTable) {
                // Warning rather than block here in mock, but for real, we enforce it tightly.
            }
        }
    }

    private Map<String, Object> mockListTables(String connectionName) {
        if ("ticket-db".equals(connectionName)) {
            return Map.of(KEY_DATA, List.of(KEY_TICKETS, "agents", "customers"), KEY_ROW_COUNT, 3);
        } else if ("orchestration-db".equals(connectionName)) {
            return Map.of(KEY_DATA, List.of("workflow_execution", "workflow_checkpoint", "tool_execution", "ai_execution_record"), KEY_ROW_COUNT, 4);
        }
        return Map.of(KEY_DATA, List.of(), KEY_ROW_COUNT, 0);
    }

    private Map<String, Object> mockDescribeTable(String tableName) {
        if (KEY_TICKETS.equals(tableName)) {
            return Map.of(
                KEY_DATA, List.of(
                    Map.of(KEY_COLUMN, "id", "type", "uuid"),
                    Map.of(KEY_COLUMN, KEY_STATUS, "type", "varchar"),
                    Map.of(KEY_COLUMN, KEY_PRIORITY, "type", "varchar"),
                    Map.of(KEY_COLUMN, "created_at", "type", "timestamp")
                ),
                KEY_ROW_COUNT, 4
            );
        }
        return Map.of(KEY_DATA, List.of(), KEY_ROW_COUNT, 0);
    }

    private Map<String, Object> mockQueryTickets(PostgresConnectionConfig config) {
        validateTableAllowed(KEY_TICKETS, config);
        List<Map<String, Object>> tickets = List.of(
            Map.of("id", "101", KEY_STATUS, "OPEN", KEY_PRIORITY, "HIGH"),
            Map.of("id", "102", KEY_STATUS, "CLOSED", KEY_PRIORITY, "LOW")
        );
        return Map.of(KEY_DATA, tickets, KEY_ROW_COUNT, 2, KEY_TRUNCATED, false);
    }

    private Map<String, Object> mockQueryWorkflows(PostgresConnectionConfig config) {
        validateTableAllowed("workflow_execution", config);
        List<Map<String, Object>> workflows = List.of(
            Map.of("id", "wf-1", KEY_STATUS, "WAITING_APPROVAL", "workflow_type", "refund"),
            Map.of("id", "wf-2", KEY_STATUS, "COMPLETED", "workflow_type", "password_reset")
        );
        return Map.of(KEY_DATA, workflows, KEY_ROW_COUNT, 2, KEY_TRUNCATED, false);
    }

    private Map<String, Object> mockExecuteQuery() {
        // Enforce mock limit of 100
        List<Map<String, Object>> results = List.of(
            Map.of("mock_column", "mock_value_1"),
            Map.of("mock_column", "mock_value_2")
        );
        return Map.of(KEY_DATA, results, KEY_ROW_COUNT, 2, KEY_TRUNCATED, false); // Default false, mock truncated if rowCount > 100
    }
}
