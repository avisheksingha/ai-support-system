package com.aisupport.common.event;

import java.util.List;

/**
 * Defines the allowed business categories for support ticket classification.
 * These categories align with Knowledge Article categories in the RAG service
 * and are used to constrain LLM output for consistent downstream routing
 * and retrieval.
 */
public class SupportCategoryVocabulary {

    public static final String ACCOUNT_MANAGEMENT = "Account Management";
    public static final String AUTHENTICATION_AND_SSO = "Authentication & SSO";
    public static final String BILLING_AND_PAYMENTS = "Billing & Payments";
    public static final String SUBSCRIPTION_AND_PLANS = "Subscription & Plans";
    public static final String API_AND_INTEGRATIONS = "API & Integrations";
    public static final String PLATFORM_PERFORMANCE = "Platform Performance";
    public static final String DATA_PRIVACY_AND_COMPLIANCE = "Data Privacy & Compliance";
    public static final String FILE_AND_STORAGE = "File & Storage";
    public static final String SECURITY_AND_FRAUD = "Security & Fraud";
    public static final String SERVICE_OUTAGE = "Service Outage";
    public static final String GENERAL_INQUIRY = "General Inquiry";

    private static final List<String> ALLOWED_CATEGORIES = List.of(
        ACCOUNT_MANAGEMENT,
        AUTHENTICATION_AND_SSO,
        BILLING_AND_PAYMENTS,
        SUBSCRIPTION_AND_PLANS,
        API_AND_INTEGRATIONS,
        PLATFORM_PERFORMANCE,
        DATA_PRIVACY_AND_COMPLIANCE,
        FILE_AND_STORAGE,
        SECURITY_AND_FRAUD,
        SERVICE_OUTAGE,
        GENERAL_INQUIRY
    );

    private SupportCategoryVocabulary() {
        /* This utility class should not be instantiated */
    }

    public static List<String> getAllowedCategories() {
        return ALLOWED_CATEGORIES;
    }

    /**
     * Normalizes a raw category string returned by the LLM into
     * the closest allowed category. Falls back to GENERAL_INQUIRY
     * if no match is found.
     */
    public static String normalize(String category) {
        if (category == null || category.isBlank()) {
            return GENERAL_INQUIRY;
        }

        String trimmed = category.trim();

        // 1. Exact match (case-insensitive)
        for (String allowed : ALLOWED_CATEGORIES) {
            if (allowed.equalsIgnoreCase(trimmed)) {
                return allowed;
            }
        }

        // 2. Substring / keyword match
        String lower = trimmed.toLowerCase();

        if (lower.contains("account") && !lower.contains("billing")) {
            return ACCOUNT_MANAGEMENT;
        }
        if (lower.contains("auth") || lower.contains("sso") || lower.contains("login") || lower.contains("oauth")) {
            return AUTHENTICATION_AND_SSO;
        }
        if (lower.contains("bill") || lower.contains("payment") || lower.contains("invoice") || lower.contains("charge")) {
            return BILLING_AND_PAYMENTS;
        }
        if (lower.contains("subscri") || lower.contains("plan") || lower.contains("upgrade") || lower.contains("downgrade")) {
            return SUBSCRIPTION_AND_PLANS;
        }
        if (lower.contains("api") || lower.contains("integrat") || lower.contains("webhook") || lower.contains("endpoint")) {
            return API_AND_INTEGRATIONS;
        }
        if (lower.contains("perform") || lower.contains("latency") || lower.contains("slow") || lower.contains("timeout")) {
            return PLATFORM_PERFORMANCE;
        }
        if (lower.contains("privacy") || lower.contains("gdpr") || lower.contains("ccpa") || lower.contains("compliance") || lower.contains("data export") || lower.contains("data deletion")) {
            return DATA_PRIVACY_AND_COMPLIANCE;
        }
        if (lower.contains("file") || lower.contains("upload") || lower.contains("storage") || lower.contains("attachment")) {
            return FILE_AND_STORAGE;
        }
        if (lower.contains("security") || lower.contains("fraud") || lower.contains("hack") || lower.contains("compromis") || lower.contains("phish")) {
            return SECURITY_AND_FRAUD;
        }
        if (lower.contains("outage") || lower.contains("downtime") || lower.contains("unavailable") || lower.contains("system down")) {
            return SERVICE_OUTAGE;
        }

        // 3. Fallback
        return GENERAL_INQUIRY;
    }
}
