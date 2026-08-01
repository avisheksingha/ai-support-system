package com.aisupport.orchestration.infrastructure.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import com.aisupport.orchestration.domain.tool.ToolDefinition;

class McpToolProviderTest {

    @Test
    void testDiscoverTools_MapsCorrectly() {
        // Arrange
        McpClient mockClient = mock(McpClient.class);
        McpToolProvider provider = new McpToolProvider(mockClient);
        
        McpToolMetadata metadata = McpToolMetadata.builder()
                .name("test_tool")
                .description("A test tool")
                .build();
                
        when(mockClient.discoverTools()).thenReturn(CompletableFuture.completedFuture(List.of(metadata)));

        // Act
        List<ToolDefinition> tools = provider.discoverTools();

        // Assert
        assertEquals(1, tools.size());
        assertEquals("test_tool", tools.get(0).getDescriptor().getName());
        assertEquals("A test tool", tools.get(0).getDescriptor().getDescription());
        assertEquals("1.0.0-MCP", tools.get(0).getDescriptor().getVersion());
    }
}





