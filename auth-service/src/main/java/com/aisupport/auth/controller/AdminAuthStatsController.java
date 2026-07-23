package com.aisupport.auth.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.auth.repository.UserRepository;
import com.aisupport.common.dto.admin.AdminAuthStatsResponse;
import com.aisupport.common.enums.UserRole;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/internal/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Admin Stats", description = "Internal endpoints for orchestration service")
public class AdminAuthStatsController {

    private final UserRepository userRepository;

    @GetMapping("/stats/admin")
    public ResponseEntity<AdminAuthStatsResponse> getAdminStats() {
        log.info("Fetching admin auth stats");
        
        List<Object[]> roleCounts = userRepository.countUsersByRole();
        
        long customers = 0;
        long agents = 0;
        long admins = 0;

        for (Object[] result : roleCounts) {
            UserRole role;
            if (result[0] instanceof UserRole ur) {
                role = ur;
            } else if (result[0] instanceof String s) {
                role = UserRole.valueOf(s);
            } else {
                continue; // Should not happen
            }

            long count = ((Number) result[1]).longValue();
            
            if (role == UserRole.CUSTOMER) customers = count;
            else if (role == UserRole.AGENT) agents = count;
            else if (role == UserRole.ADMIN) admins = count;
        }

        AdminAuthStatsResponse response = AdminAuthStatsResponse.builder()
                .totalCustomers(customers)
                .totalAgents(agents)
                .totalAdmins(admins)
                .build();

        return ResponseEntity.ok(response);
    }
}
