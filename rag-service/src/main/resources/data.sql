-- =============================================
-- SEED DATA: Knowledge Base Articles
-- =============================================
-- This runs on every startup (spring.sql.init.mode=always).
-- DELETE + re-insert ensures a clean, consistent knowledge base
-- for development. The DataLoaderRunner will re-embed these into
-- PGVector on each restart, so this approach is safe for dev use.

DELETE FROM knowledge_articles;

INSERT INTO knowledge_articles (title, content, embedded) VALUES
('Refund Policy',
 'Refunds are processed within 5-7 business days after approval. To request a refund, open a ticket with your order ID. Partial refunds are available for partially used services.', false),

('Login Troubleshooting',
 'If you cannot login, try resetting your password via the Forgot Password link. Clear your browser cache and cookies. If using SSO, ensure your corporate credentials are active. Contact support if the issue persists after 3 attempts.', false),

('Payment Issues',
 'If payment was deducted but order failed, a refund is auto-initiated within 24 hours. Check your bank statement after 3-5 days. For recurring billing issues, verify your card expiry date and billing address match.', false),

('Account Security',
 'Enable two-factor authentication (2FA) from Settings > Security. If you suspect unauthorized access, immediately change your password and review recent login activity. We never ask for passwords via email.', false),

('Service Outage Information',
 'Check our status page at status.example.com for real-time updates during outages. Planned maintenance is announced 48 hours in advance via email. If a specific feature is down, try clearing cache or using an incognito window.', false),
 
('App Crash Troubleshooting',
 'If the app crashes repeatedly, update to the latest version. Restart your device and clear app cache. If the issue persists, reinstall the app and contact support with logs.', false),

('API Rate Limiting',
 'Our REST API enforces a rate limit of 1000 requests per minute per API key. Exceeding this limit will result in a 429 Too Many Requests response. Contact your account manager to request a quota increase for enterprise plans.', false),

('Subscription Upgrades and Downgrades',
 'You can upgrade your subscription plan at any time; changes take effect immediately and are prorated. Downgrades take effect at the start of the next billing cycle. We do not offer refunds for unused time on downgraded plans.', false),

('Data Export and GDPR Compliance',
 'To export your account data or request account deletion under GDPR guidelines, navigate to Privacy Settings and select "Export Data" or "Delete Account". Data exports take up to 48 hours to compile and are delivered securely via email.', false),

('SLA and Response Times',
 'Enterprise customers are guaranteed a 2-hour response time for critical issues (Severity 1) and a 12-hour response time for standard inquiries. Standard tier customers receive responses within 24-48 business hours.', false),

('Invoice and Billing Receipts',
 'All past invoices and billing receipts can be downloaded directly from the Billing Dashboard. We also automatically email a PDF copy to the designated billing contact on file within 24 hours of successful payment.', false),

('Supported Web Browsers',
 'Our web application is fully supported on the latest versions of Google Chrome, Mozilla Firefox, Apple Safari, and Microsoft Edge. Internet Explorer is strictly not supported and may cause severe UI degradation.', false),

('Webhook Configuration',
 'Webhooks can be configured in the Developer Settings panel. You must provide a valid HTTPS endpoint. Our system expects a 200 OK response within 3 seconds; otherwise, the webhook delivery will be retried up to 5 times using exponential backoff.', false),

('Adding Team Members',
 'Administrators can invite new team members from the Organization Settings page. You can assign roles such as "Admin", "Agent", or "Viewer". Invitations expire after 7 days if not accepted.', false),
 
('Ticket Escalation Process',
 'If your support ticket has not been resolved within the expected SLA timeframe, you may escalate it by replying to the ticket with the word "ESCALATE". This flags the ticket for immediate review by a senior support engineer.', false);