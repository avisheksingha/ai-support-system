package com.aisupport.orchestration.infrastructure.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.common.dto.admin.AdminAuthStatsResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                      @Value("${api.services.auth.url:http://AUTH-SERVICE}") String authServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(authServiceUrl)
                .build();
    }

    public AdminAuthStatsResponse getAdminStats() {
        return restClient.get()
                .uri("/api/internal/auth/stats/admin")
                .retrieve()
                .body(AdminAuthStatsResponse.class);
    }
}
