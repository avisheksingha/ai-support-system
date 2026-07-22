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
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.EMPTY)
                    .reason("The support request is empty.")
                    .title("Please provide details")
                    .userMessage("Please provide details about your issue.")
                    .canProceed(false)
                    .isSoftValidation(false)
                    .build();
        }

        boolean subjectIsGreeting = safeSubject.isEmpty() || SupportRequestDictionary.isGreeting(safeSubject);
        boolean contentIsGreeting = safeContent.isEmpty() || SupportRequestDictionary.isGreeting(safeContent);

        if (SupportRequestDictionary.isGreeting(combined) || (subjectIsGreeting && contentIsGreeting)) {
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.GREETING)
                    .reason("The input is just a greeting.")
                    .title("Please describe your issue")
                    .userMessage("Hello! Please describe the issue you'd like help with.")
                    .canProceed(false)
                    .isSoftValidation(false)
                    .build();
        }

        boolean subjectIsNonSupport = safeSubject.isEmpty() || SupportRequestDictionary.isNonSupportMessage(safeSubject);
        boolean contentIsNonSupport = safeContent.isEmpty() || SupportRequestDictionary.isNonSupportMessage(safeContent);

        if (SupportRequestDictionary.isNonSupportMessage(combined) || (subjectIsNonSupport && contentIsNonSupport)) {
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.NON_SUPPORT_MESSAGE)
                    .reason("The input appears to be a conversational or non-support message.")
                    .title("Technical support needed")
                    .userMessage("Thank you! However, this form is intended for technical support requests. Please describe your issue.")
                    .canProceed(false)
                    .isSoftValidation(false)
                    .build();
        }

        if (SupportRequestDictionary.isNoise(combined)) {
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.SPAM_OR_NOISE)
                    .reason("The input appears to be spam or noise.")
                    .title("Please provide details")
                    .userMessage("Please enter a meaningful support request.")
                    .canProceed(false)
                    .isSoftValidation(false)
                    .build();
        }

        if (combined.length() < 10) {
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.TOO_SHORT)
                    .reason("The input is too short to be a valid request.")
                    .title("More detail recommended")
                    .userMessage("Your request is very short. Please provide a more descriptive subject and description.")
                    .canProceed(false)
                    .isSoftValidation(true)
                    .build();
        }

        if (combined.length() < 20 && !SupportRequestDictionary.containsSupportKeywords(combined)) {
            return ValidationResult.builder()
                    .outcome(ValidationOutcome.NEEDS_MORE_INFORMATION)
                    .reason("The input lacks detail and doesn't contain support keywords.")
                    .title("More information needed")
                    .userMessage("Could you please provide a few more details about what's happening?")
                    .canProceed(false)
                    .isSoftValidation(true)
                    .build();
        }

        return ValidationResult.builder()
                .outcome(ValidationOutcome.VALID_SUPPORT_REQUEST)
                .reason("The input passed all validation checks.")
                .title("Valid")
                .userMessage(null)
                .canProceed(true)
                .isSoftValidation(false)
                .build();
    }
}
