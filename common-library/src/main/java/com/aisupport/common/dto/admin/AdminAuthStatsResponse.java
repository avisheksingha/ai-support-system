package com.aisupport.common.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthStatsResponse {
    private long totalCustomers;
    private long totalAgents;
    private long totalAdmins;
}
