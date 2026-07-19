package com.aisupport.common.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportIntentVocabulary {
    
    public static final String ACCOUNT_ACCESS = "ACCOUNT_ACCESS";
    public static final String SECURITY = "SECURITY";
    public static final String BILLING = "BILLING";
    public static final String SUBSCRIPTION = "SUBSCRIPTION";
    public static final String API = "API";
    public static final String WEBHOOK = "WEBHOOK";
    public static final String PERFORMANCE = "PERFORMANCE";
    public static final String FILE_UPLOAD = "FILE_UPLOAD";
    public static final String DATA_PRIVACY = "DATA_PRIVACY";
    public static final String ESCALATION = "ESCALATION";
    public static final String OUTAGE = "OUTAGE";
    public static final String GENERAL = "GENERAL";

    public static final List<String> ALLOWED_INTENTS = Arrays.asList(
        ACCOUNT_ACCESS, SECURITY, BILLING, SUBSCRIPTION, API, WEBHOOK, 
        PERFORMANCE, FILE_UPLOAD, DATA_PRIVACY, ESCALATION, OUTAGE, GENERAL
    );
    
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    
    static {
        // ACCOUNT_ACCESS
        mapTo(ACCOUNT_ACCESS, "LOGIN_ISSUE", "PASSWORD_RESET", "INVALID_CREDENTIALS", 
            "SIGN_IN_PROBLEM", "AUTHENTICATION_FAILURE", "LOCKOUT", "SSO_ISSUE", "LOGIN", "SIGN_IN", "ACCESS");
        
        // SECURITY
        mapTo(SECURITY, "HACKED", "COMPROMISED", "VULNERABILITY", "DATA_BREACH", "PHISHING", "MALWARE");
        
        // BILLING
        mapTo(BILLING, "INVOICE_ISSUE", "PAYMENT_FAILED", "REFUND_REQUEST", "CHARGE_DISPUTE", "PAYMENT", "INVOICE", "BILL");
        
        // SUBSCRIPTION
        mapTo(SUBSCRIPTION, "CANCEL_SUBSCRIPTION", "UPGRADE_PLAN", "DOWNGRADE_PLAN", "RENEWAL", "PLAN_CHANGE", "CANCEL");
        
        // API
        mapTo(API, "API_ERROR", "RATE_LIMIT", "API_KEY", "ENDPOINT_FAILURE", "DEVELOPER_API", "REST", "GRAPHQL");
        
        // WEBHOOK
        mapTo(WEBHOOK, "WEBHOOK_FAILED", "PAYLOAD_ERROR", "CALLBACK_ISSUE", "WEBHOOKS", "EVENT_DELIVERY");
        
        // PERFORMANCE
        mapTo(PERFORMANCE, "SLOW_SYSTEM", "HIGH_LATENCY", "TIMEOUTS", "SYSTEM_LAG", "SLOW", "LATENCY");
        
        // FILE_UPLOAD
        mapTo(FILE_UPLOAD, "UPLOAD_FAILED", "FILE_TOO_LARGE", "ATTACHMENT_ISSUE", "INVALID_FORMAT", "UPLOAD");
        
        // DATA_PRIVACY
        mapTo(DATA_PRIVACY, "GDPR_REQUEST", "ACCOUNT_DELETION", "DATA_EXPORT", "PRIVACY_CONCERN", "CCPA", "GDPR");
        
        // ESCALATION
        mapTo(ESCALATION, "SPEAK_TO_MANAGER", "URGENT_COMPLAINT", "UNRESOLVED_ISSUE", "LEGAL_THREAT", "COMPLAINT", "MANAGER");
        
        // OUTAGE
        mapTo(OUTAGE, "SYSTEM_DOWN", "SERVICE_UNAVAILABLE", "CRASH", "500_ERROR", "DOWNTIME", "OUTAGE");
    }
    
    private SupportIntentVocabulary() {
        /* This utility class should not be instantiated */
    }
    
    private static void mapTo(String targetIntent, String... variants) {
        for (String variant : variants) {
            SYNONYMS.put(variant, targetIntent);
        }
    }

    public static boolean isValid(String intent) {
        if (intent == null) return false;
        return ALLOWED_INTENTS.contains(intent.toUpperCase().replace(" ", "_"));
    }
    
    public static String normalize(String intent) {
        if (intent == null) return GENERAL;
        String normalized = intent.toUpperCase().replace(" ", "_");
        
        // 1. Exact vocabulary match
        if (ALLOWED_INTENTS.contains(normalized)) {
            return normalized;
        }
        
        // 2. Synonym exact match
        if (SYNONYMS.containsKey(normalized)) {
            return SYNONYMS.get(normalized);
        }
        
        // 2b. Synonym partial match (safeguard)
        for (Map.Entry<String, String> entry : SYNONYMS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 3. GENERAL fallback
        return GENERAL;
    }
}
