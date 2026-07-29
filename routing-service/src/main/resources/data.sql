DELETE FROM routing_rules;

INSERT INTO routing_rules
(
    rule_name,
    description,
    rule_version,
    priority,
    active,
    intent_pattern,
    sentiment_pattern,
    urgency_pattern,
    keyword_patterns,
    assign_to_team,
    priority_override,
    sla_hours,
    created_at,
    updated_at
)
VALUES

-- ============================================================================
-- CRITICAL INCIDENTS
-- ============================================================================

(
'Critical Security Incident',
'Compromised accounts, security breaches, phishing, unauthorized access',
1,
100,
true,
'SECURITY',
NULL,
'CRITICAL',
'breach|compromised|unauthorized|phishing|malware|hacked',
'security-team',
'CRITICAL',
1,
now(),
now()
),

(
'Critical Platform Outage',
'Complete platform outage affecting customers',
1,
98,
true,
'OUTAGE',
NULL,
'CRITICAL',
'down|outage|production down|service unavailable',
'platform-operations',
'CRITICAL',
2,
now(),
now()
),

(
'Low Confidence AI Review',
'AI confidence below acceptable threshold requires manual routing review',
1,
95,
true,
NULL,
NULL,
NULL,
'ai_low_confidence',
'manual-review',
NULL,
4,
now(),
now()
),

-- ============================================================================
-- ACCOUNT & AUTHENTICATION
-- ============================================================================

(
'Authentication Issues',
'Login, password reset, MFA, SSO and account lockout',
1,
90,
true,
'ACCOUNT_ACCESS',
NULL,
'HIGH',
'password|login|signin|authentication|lockout|mfa|2fa|otp|reset',
'account-team',
NULL,
8,
now(),
now()
),

(
'Account Management',
'General account management and profile issues',
1,
88,
true,
'ACCOUNT_ACCESS',
NULL,
'MEDIUM|LOW',
'profile|account|email|username',
'account-team',
NULL,
12,
now(),
now()
),

-- ============================================================================
-- DATA PRIVACY
-- ============================================================================

(
'Privacy & GDPR',
'GDPR, privacy requests, data export, account deletion',
1,
85,
true,
'DATA_PRIVACY',
NULL,
NULL,
'gdpr|privacy|personal data|delete account|export data|right to be forgotten',
'compliance-team',
'HIGH',
24,
now(),
now()
),

-- ============================================================================
-- BILLING
-- ============================================================================

(
'Payment Issues',
'Failed payments and payment processing',
1,
82,
true,
'PAYMENT',
NULL,
NULL,
'payment|credit card|declined|failed payment',
'billing-team',
NULL,
12,
now(),
now()
),

(
'Refund Requests',
'Refunds and billing disputes',
1,
80,
true,
'REFUND',
NULL,
NULL,
'refund|chargeback|duplicate charge',
'billing-team',
NULL,
24,
now(),
now()
),

(
'Subscription Management',
'Subscription upgrades, renewals and cancellations',
1,
78,
true,
'SUBSCRIPTION',
NULL,
NULL,
'subscription|plan|upgrade|downgrade|renewal|cancel',
'billing-team',
NULL,
48,
now(),
now()
),

-- ============================================================================
-- API & DEVELOPER
-- ============================================================================

(
'API Support',
'REST API, SDK, authentication and integrations',
1,
75,
true,
'API',
NULL,
NULL,
'api|sdk|oauth|token|integration|endpoint',
'developer-support',
NULL,
12,
now(),
now()
),

(
'Webhook Support',
'Webhook delivery, retries and event notifications',
1,
74,
true,
'WEBHOOK',
NULL,
NULL,
'webhook|callback|event delivery|retry',
'integration-team',
NULL,
12,
now(),
now()
),

-- ============================================================================
-- PERFORMANCE
-- ============================================================================

(
'Performance Issues',
'Slow application, latency and response time',
1,
72,
true,
'PERFORMANCE',
NULL,
'HIGH|MEDIUM',
'slow|latency|performance|timeout|response time',
'platform-operations',
NULL,
12,
now(),
now()
),

(
'File Upload Issues',
'Upload failures and attachment problems',
1,
70,
true,
'FILE_UPLOAD',
NULL,
NULL,
'upload|attachment|pdf|file size',
'technical-support',
NULL,
24,
now(),
now()
),

-- ============================================================================
-- CUSTOMER SUCCESS
-- ============================================================================

(
'Escalation Requests',
'Customer escalation requiring management attention',
1,
65,
true,
'ESCALATION',
'NEGATIVE',
'HIGH',
'escalate|manager|urgent|production impact',
'customer-success',
'HIGH',
4,
now(),
now()
),

(
'Feature Requests',
'Enhancement requests and product feedback',
1,
50,
true,
'FEATURE_REQUEST|FEEDBACK',
NULL,
NULL,
'feature|enhancement|improvement|suggestion',
'product-team',
NULL,
72,
now(),
now()
),

-- ============================================================================
-- GENERAL
-- ============================================================================

(
'General Support',
'General customer inquiries',
1,
20,
true,
'GENERAL',
NULL,
NULL,
NULL,
'general-support',
NULL,
48,
now(),
now()
),

(
'Default Catch-All',
'Fallback rule for unmatched tickets',
1,
10,
true,
NULL,
NULL,
NULL,
NULL,
'general-support',
NULL,
24,
now(),
now()
);