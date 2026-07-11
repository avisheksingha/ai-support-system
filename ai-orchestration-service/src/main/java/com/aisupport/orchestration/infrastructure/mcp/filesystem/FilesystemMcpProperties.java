package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcp.filesystem")
public class FilesystemMcpProperties {

    private boolean enabled;
    private String mode; // "mock" or "real"
    private String workspaceRoot;
    private List<String> allowedPaths;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getWorkspaceRoot() {
        return workspaceRoot;
    }

    public void setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }
}
