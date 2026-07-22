package com.aisupport.common.dto;

import com.aisupport.common.enums.ValidationOutcome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private ValidationOutcome outcome;
    private String reason;
    private String title;
    private String userMessage;
    private boolean canProceed;
    private boolean isSoftValidation;
}
