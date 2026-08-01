package com.aisupport.common.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalysisStatsResponse {
    private long totalAnalyses;
    private long highConfidenceAnalyses;
    private long processedToday;
}
