package com.aisupport.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aisupport.common.dto.ValidationResult;
import com.aisupport.common.enums.ValidationOutcome;

class TicketPreValidatorTest {

    @Test
    void testGreeting() {
        ValidationResult res1 = TicketPreValidator.validate("Hello", "");
        assertEquals(ValidationOutcome.GREETING, res1.getOutcome());
        assertFalse(res1.isCanProceed());

        ValidationResult res2 = TicketPreValidator.validate("Good morning", "");
        assertEquals(ValidationOutcome.GREETING, res2.getOutcome());
    }

    @Test
    void testNonSupportMessage() {
        ValidationResult res1 = TicketPreValidator.validate("Thanks", "");
        assertEquals(ValidationOutcome.NON_SUPPORT_MESSAGE, res1.getOutcome());
        
        ValidationResult res2 = TicketPreValidator.validate("👍", "");
        assertEquals(ValidationOutcome.NON_SUPPORT_MESSAGE, res2.getOutcome());
    }

    @Test
    void testTooShort() {
        ValidationResult res = TicketPreValidator.validate("test", "");
        assertEquals(ValidationOutcome.TOO_SHORT, res.getOutcome());
    }

    @Test
    void testSpamOrNoise() {
        ValidationResult res1 = TicketPreValidator.validate("asdasdasd", "");
        assertEquals(ValidationOutcome.SPAM_OR_NOISE, res1.getOutcome());

        ValidationResult res2 = TicketPreValidator.validate("???", "");
        assertEquals(ValidationOutcome.SPAM_OR_NOISE, res2.getOutcome());
    }

    @Test
    void testValidSupportRequest() {
        ValidationResult res1 = TicketPreValidator.validate("My password reset is not working.", "");
        assertEquals(ValidationOutcome.VALID_SUPPORT_REQUEST, res1.getOutcome());
        assertTrue(res1.isCanProceed());

        ValidationResult res2 = TicketPreValidator.validate("API returns HTTP 500.", "When calling the users endpoint, I get a 500 error.");
        assertEquals(ValidationOutcome.VALID_SUPPORT_REQUEST, res2.getOutcome());
        assertTrue(res2.isCanProceed());
    }

    @Test
    void testNeedsMoreInformation() {
        ValidationResult res = TicketPreValidator.validate("I have a problem", "");
        assertEquals(ValidationOutcome.NEEDS_MORE_INFORMATION, res.getOutcome());
    }

    @Test
    void testEmpty() {
        ValidationResult res = TicketPreValidator.validate(" ", "\t");
        assertEquals(ValidationOutcome.EMPTY, res.getOutcome());
    }
}
