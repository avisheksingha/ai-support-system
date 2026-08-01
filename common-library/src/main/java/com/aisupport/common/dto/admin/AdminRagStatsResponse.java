package com.aisupport.common.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRagStatsResponse {
    private long totalArticles;
    private long vectorizedDocuments;
    private String mostAccessedArticle;
    private long totalRagRequests;
    private long successfulRagRequests;
}
