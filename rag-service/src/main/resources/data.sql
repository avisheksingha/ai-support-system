-- =============================================
-- SEED DATA: Knowledge Base Articles
-- =============================================
-- This runs on every startup (spring.sql.init.mode=always).
-- DELETE + re-insert ensures a clean, consistent knowledge base
-- for development. The DataLoaderRunner will re-embed these into
-- PGVector on each restart, so this approach is safe for dev use.

DELETE FROM knowledge_articles;

INSERT INTO knowledge_articles (title, content) VALUES
('Refund Policy',
 'Refunds are processed within 5-7 business days after approval. To request a refund, open a ticket with your order ID. Partial refunds are available for partially used services.'),

('Login Troubleshooting',
 'If you cannot login, try resetting your password via the Forgot Password link. Clear your browser cache and cookies. If using SSO, ensure your corporate credentials are active. Contact support if the issue persists after 3 attempts.'),

('Payment Issues',
 'If payment was deducted but order failed, a refund is auto-initiated within 24 hours. Check your bank statement after 3-5 days. For recurring billing issues, verify your card expiry date and billing address match.'),

('Account Security',
 'Enable two-factor authentication (2FA) from Settings > Security. If you suspect unauthorized access, immediately change your password and review recent login activity. We never ask for passwords via email.'),

('Service Outage Information',
 'Check our status page at status.example.com for real-time updates during outages. Planned maintenance is announced 48 hours in advance via email. If a specific feature is down, try clearing cache or using an incognito window.');