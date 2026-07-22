package com.aisupport.common.util;

import java.util.List;
import java.util.regex.Pattern;

public class SupportRequestDictionary {

    private SupportRequestDictionary() {
        // Utility class, hide constructor
    }

    // Regex patterns for greetings (case-insensitive)
    public static final Pattern GREETING_PATTERN = Pattern.compile(
            "^(hi|hello|hey|good morning|good afternoon|good evening|greetings|howdy|what's up)\\s*[.!?,]*$", 
            Pattern.CASE_INSENSITIVE
    );

    // Regex patterns for non-support messages like "thanks" or emojis
    public static final Pattern NON_SUPPORT_PATTERN = Pattern.compile(
            "^(thanks|thank you|thx|tysm|ty|awesome|great|cool|ok|okay|bye|goodbye|👍|💯|👏)\\s*[.!?,]*$", 
            Pattern.CASE_INSENSITIVE
    );

    // Regex patterns for spam or noise (repetitive characters, only punctuation)
    public static final Pattern NOISE_PATTERN = Pattern.compile(
            "^([\\W_]+|(.+?)\\2{3,})$", // Matches only punctuation OR same sequence repeated >= 4 times
            Pattern.CASE_INSENSITIVE
    );

    // Common words indicating a support request (used if length is borderline)
    public static final List<String> SUPPORT_KEYWORDS = List.of(
            "issue", "error", "fail", "broken", "help", "need", "not working", "bug", "support", 
            "login", "password", "account", "payment", "charge", "refund", "api", "500", "404", "crash"
    );

    public static boolean isGreeting(String text) {
        return GREETING_PATTERN.matcher(text.trim()).matches();
    }

    public static boolean isNonSupportMessage(String text) {
        return NON_SUPPORT_PATTERN.matcher(text.trim()).matches();
    }

    public static boolean isNoise(String text) {
        if (text.trim().isEmpty()) return false;
        
        // Check for very low character entropy (e.g., repeating the same 1-3 characters)
        long uniqueChars = text.chars().distinct().count();
        if (uniqueChars <= 3 && text.length() > 5) {
            return true;
        }

        return NOISE_PATTERN.matcher(text.trim()).matches();
    }

    public static boolean containsSupportKeywords(String text) {
        String lowerText = text.toLowerCase();
        for (String keyword : SUPPORT_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
