package com.aisupport.orchestration.infrastructure.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.application.admin.dto.AdminDashboardResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SystemHealthClient {

    private final RestClient restClient;

    public SystemHealthClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public AdminDashboardResponse.SystemHealthDTO getSystemHealth(String serviceName) {
        String status = "DOWN";
        String version = "N/A";
        String buildVersion = "N/A";
        String uptime = "N/A";

        try {
            // Fetch health
            Map<String, Object> healthResponse = restClient.get()
                    .uri("http://" + serviceName + "/actuator/health")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (healthResponse != null && healthResponse.containsKey("status")) {
                status = (String) healthResponse.get("status");
            }
        } catch (Exception e) {
            log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
        }

        if (!"DOWN".equals(status)) {
            try {
                // Fetch info
                Map<String, Object> infoResponse = restClient.get()
                        .uri("http://" + serviceName + "/actuator/info")
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

                if (infoResponse != null && infoResponse.containsKey("app")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> appInfo = (Map<String, Object>) infoResponse.get("app");
                    if (appInfo.containsKey("version")) {
                        version = (String) appInfo.get("version");
                    }
                    if (appInfo.containsKey("description")) {
                        buildVersion = (String) appInfo.get("description");
                    }
                }
            } catch (Exception e) {
                log.warn("Info check failed for {}: {}", serviceName, e.getMessage());
            }
        }

        return AdminDashboardResponse.SystemHealthDTO.builder()
                .serviceName(serviceName)
                .status(status)
                .version(version)
                .buildVersion(buildVersion)
                .uptime(uptime)
                .build();
    }
}
