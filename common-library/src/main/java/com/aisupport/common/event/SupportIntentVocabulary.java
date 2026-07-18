package com.aisupport.common.event;

import java.util.Arrays;
import java.util.List;

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
    
    public static boolean isValid(String intent) {
        if (intent == null) return false;
        return ALLOWED_INTENTS.contains(intent.toUpperCase().replace(" ", "_"));
    }
    
    public static String normalize(String intent) {
        if (intent == null) return GENERAL;
        String normalized = intent.toUpperCase().replace(" ", "_");
        return ALLOWED_INTENTS.contains(normalized) ? normalized : GENERAL;
    }
}
