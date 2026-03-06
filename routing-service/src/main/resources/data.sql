DELETE FROM routing_rules;

INSERT INTO routing_rules
(rule_name, description, priority, active,
 intent_pattern, sentiment_pattern, urgency_pattern, keyword_patterns,
 assign_to_team, priority_override, sla_hours,
 created_at, updated_at)
VALUES

('Critical Urgent Issues',
 'Route critical urgent issues to senior support',
 100, true,
 NULL, NULL, 'CRITICAL', NULL,
 'senior-support', 'CRITICAL', 2,
 now(), now()),

('High Urgency Negative Sentiment',
 'Escalate urgent negative cases',
 90, true,
 NULL, 'NEGATIVE', 'HIGH', NULL,
 'escalation-team', 'HIGH', 4,
 now(), now()),

('Technical High Priority',
 'High urgency technical issues',
 80, true,
 'TECHNICAL', NULL, 'HIGH', NULL,
 'tech-support-senior', 'HIGH', 8,
 now(), now()),

('Billing Issues',
 'All billing related issues',
 70, true,
 'BILLING|PAYMENT_ISSUE|CHECK_REFUND_STATUS', NULL, NULL, NULL,
 'billing-team', NULL, 24,
 now(), now()),

('Account Access Issues',
 'Account login problems',
 60, true,
 'ACCOUNT', NULL, NULL, NULL,
 'account-team', 'HIGH', 12,
 now(), now()),

('Technical Medium Priority',
 'Medium urgency tech issues',
 50, true,
 'TECHNICAL', NULL, 'MEDIUM', NULL,
 'tech-support', NULL, 24,
 now(), now()),

('Feature Requests',
 'Route feature requests',
 40, true,
 'FEATURE_REQUEST', NULL, NULL, NULL,
 'product-team', 'LOW', 72,
 now(), now()),

('Complaints',
 'Customer complaints',
 30, true,
 'COMPLAINT', NULL, NULL, NULL,
 'customer-success', 'MEDIUM', 24,
 now(), now()),

('General Low Priority',
 'General low urgency tickets',
 20, true,
 'GENERAL', NULL, 'LOW', NULL,
 'general-support', 'LOW', 48,
 now(), now()),

('Default Catch-All',
 'Fallback routing',
 10, true,
 NULL, NULL, NULL, NULL,
 'general-support', 'MEDIUM', 24,
 now(), now());