package com.aisupport.analysis.dto.response;

import java.util.List;

public record WritingImprovementLLMResponse(
        String improvedSubject,
        String improvedContent,
        List<String> changes,
        boolean improved,
        String model,
        String qualityAssessment,
        List<String> checklist
) {
}
