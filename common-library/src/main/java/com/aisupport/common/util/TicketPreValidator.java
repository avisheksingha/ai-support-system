package com.aisupport.common.util;

import com.aisupport.common.dto.ValidationResult;
import com.aisupport.common.enums.ValidationOutcome;

public class TicketPreValidator {

    private TicketPreValidator() {
        // Utility class, hide constructor
    }

    public static ValidationResult validate(String subject, String content) {
        String safeSubject = subject != null ? subject.trim() : "";
        String safeContent = content != null ? content.trim() : "";
        String combined = (safeSubject + " " + safeContent).trim();

        if (combined.isEmpty()) {
            return buildResult(ValidationOutcome.EMPTY, "The support request is empty.", 
                    "Please provide details", "Please provide details about your issue.", false, false);
        }

        ValidationResult greetingResult = checkGreeting(safeSubject, safeContent, combined);
        if (greetingResult != null) return greetingResult;

        ValidationResult nonSupportResult = checkNonSupport(safeSubject, safeContent, combined);
        if (nonSupportResult != null) return nonSupportResult;

        if (SupportRequestDictionary.isNoise(combined)) {
            return buildResult(ValidationOutcome.SPAM_OR_NOISE, "The input appears to be spam or noise.", 
                    "Please provide details", "Please enter a meaningful support request.", false, false);
        }

        ValidationResult lengthResult = checkLengthAndKeywords(combined);
        if (lengthResult != null) return lengthResult;

        return buildResult(ValidationOutcome.VALID_SUPPORT_REQUEST, "The input passed all validation checks.", 
                "Valid", null, true, false);
    }

    private static ValidationResult checkGreeting(String safeSubject, String safeContent, String combined) {
        boolean subjectIsGreeting = safeSubject.isEmpty() || SupportRequestDictionary.isGreeting(safeSubject);
        boolean contentIsGreeting = safeContent.isEmpty() || SupportRequestDictionary.isGreeting(safeContent);

        if (SupportRequestDictionary.isGreeting(combined) || (subjectIsGreeting && contentIsGreeting)) {
            return buildResult(ValidationOutcome.GREETING, "The input is just a greeting.", 
                    "Please describe your issue", "Hello! Please describe the issue you'd like help with.", false, false);
        }
        return null;
    }

    private static ValidationResult checkNonSupport(String safeSubject, String safeContent, String combined) {
        boolean subjectIsNonSupport = safeSubject.isEmpty() || SupportRequestDictionary.isNonSupportMessage(safeSubject);
        boolean contentIsNonSupport = safeContent.isEmpty() || SupportRequestDictionary.isNonSupportMessage(safeContent);

        if (SupportRequestDictionary.isNonSupportMessage(combined) || (subjectIsNonSupport && contentIsNonSupport)) {
            return buildResult(ValidationOutcome.NON_SUPPORT_MESSAGE, "The input appears to be a conversational or non-support message.", 
                    "Technical support needed", "Thank you! However, this form is intended for technical support requests. Please describe your issue.", false, false);
        }
        return null;
    }

    private static ValidationResult checkLengthAndKeywords(String combined) {
        if (combined.length() < 10) {
            return buildResult(ValidationOutcome.TOO_SHORT, "The input is too short to be a valid request.", 
                    "More detail recommended", "Your request is very short. Please provide a more descriptive subject and description.", false, true);
        }

        if (combined.length() < 40 && !SupportRequestDictionary.containsSupportKeywords(combined)) {
            return buildResult(ValidationOutcome.NEEDS_MORE_INFORMATION, "The input lacks detail and doesn't contain support keywords.", 
                    "More information needed", "Could you please provide a few more details about what's happening?", false, true);
        }
        return null;
    }

    private static ValidationResult buildResult(ValidationOutcome outcome, String reason, String title, 
            String userMessage, boolean canProceed, boolean isSoft) {
        return ValidationResult.builder()
                .outcome(outcome)
                .reason(reason)
                .title(title)
                .userMessage(userMessage)
                .canProceed(canProceed)
                .isSoftValidation(isSoft)
                .build();
    }
}
