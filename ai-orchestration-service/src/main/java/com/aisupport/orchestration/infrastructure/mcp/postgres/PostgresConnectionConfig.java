package com.aisupport.orchestration.infrastructure.mcp.postgres;

import java.util.List;

public class PostgresConnectionConfig {
    private String url;
    private String username;
    private String purpose;
    private List<String> allowedSchemas;
    private List<String> allowedTables;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public List<String> getAllowedSchemas() {
        return allowedSchemas;
    }

    public void setAllowedSchemas(List<String> allowedSchemas) {
        this.allowedSchemas = allowedSchemas;
    }

    public List<String> getAllowedTables() {
        return allowedTables;
    }

    public void setAllowedTables(List<String> allowedTables) {
        this.allowedTables = allowedTables;
    }
}
