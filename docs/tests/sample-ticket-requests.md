# AI Support System - Sample Customer Tickets

These sample requests are designed to validate the complete AI-powered customer support pipeline.

## Features Covered

- Ticket Service
- AI Analysis
- Intent Detection
- Sentiment Analysis
- Urgency Detection
- RAG Knowledge Retrieval
- AI Decision Making
- Smart Routing
- Customer Workspace
- Agent Workspace

---

## Ticket 1 - Password Reset Issue

```json
{
  "subject": "Unable to sign in after resetting my password",
  "message": "I changed my password yesterday using the password reset link, but every login attempt still says invalid credentials. I've cleared my browser cache and tried another browser, but the problem remains."
}
```

Expected

- Intent: LOGIN
- Sentiment: NEGATIVE
- Urgency: MEDIUM
- Knowledge: Password Reset / Login Troubleshooting
- Department: Technical Support

---

## Ticket 2 - Suspicious Login Activity

```json
{
  "subject": "Unexpected login notification",
  "message": "I received a login alert from another country even though I haven't travelled. I immediately changed my password, but I'd like to know if my account has been compromised."
}
```

Expected

- Intent: SECURITY
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Suspicious Login / Account Security
- Department: Security Operations

---

## Ticket 3 - Failed Subscription Renewal

```json
{
  "subject": "Subscription renewal failed",
  "message": "Our Enterprise subscription expired today because the automatic payment was declined. The credit card is valid and we'd like to restore access as soon as possible."
}
```

Expected

- Intent: PAYMENT
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Subscription Renewal / Payment Methods
- Department: Billing

---

## Ticket 4 - Refund Eligibility

```json
{
  "subject": "Requesting a refund",
  "message": "I accidentally upgraded to the Enterprise Plan this morning. Since we haven't started using the new features yet, I'd like to request a refund if we're still eligible."
}
```

Expected

- Intent: REFUND
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: Refund Policy
- Department: Billing

---

## Ticket 5 - API Rate Limiting

```json
{
  "subject": "Receiving HTTP 429 errors",
  "message": "Our integration has started returning HTTP 429 Too Many Requests responses during business hours. Could you explain the API rate limits and the recommended retry strategy?"
}
```

Expected

- Intent: API
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: API Rate Limiting
- Department: Developer Support

---

## Ticket 6 - OAuth Configuration

```json
{
  "subject": "OAuth callback URL not working",
  "message": "We've configured OAuth for our application, but after authentication the provider redirects users to an invalid callback URL. We'd appreciate guidance on the correct configuration."
}
```

Expected

- Intent: API
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: OAuth Configuration
- Department: Developer Support

---

## Ticket 7 - Webhook Delivery Failure

```json
{
  "subject": "Webhook events are not arriving",
  "message": "Our webhook endpoint hasn't received any payment events since yesterday. The endpoint returns HTTP 200 when tested manually, but the production events never arrive."
}
```

Expected

- Intent: WEBHOOK
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Webhook Delivery / Retry Policy
- Department: Developer Support

---

## Ticket 8 - Invite New Team Members

```json
{
  "subject": "Unable to invite additional users",
  "message": "I'm the workspace administrator, but every invitation fails when I try to add new team members. We still have available licenses."
}
```

Expected

- Intent: USER_MANAGEMENT
- Sentiment: NEGATIVE
- Urgency: LOW
- Knowledge: User Invitations / Organization Management
- Department: Customer Success

---

## Ticket 9 - GDPR Data Export

```json
{
  "subject": "Requesting a copy of my personal data",
  "message": "Before closing my account I'd like to download all personal information associated with my profile in accordance with GDPR requirements."
}
```

Expected

- Intent: GDPR
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: Data Export / GDPR
- Department: Privacy & Compliance

---

## Ticket 10 - Dashboard Performance

```json
{
  "subject": "Dashboard is extremely slow",
  "message": "Our team can still access the dashboard, but every page takes more than thirty seconds to load. This started after yesterday's maintenance window."
}
```

Expected

- Intent: PERFORMANCE
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Dashboard Performance Troubleshooting
- Department: Platform Operations

---

## Ticket 11 - File Upload Failure

```json
{
  "subject": "Cannot upload project attachments",
  "message": "Every attempt to upload a PDF larger than 15 MB fails with an unexpected error. Smaller files upload successfully."
}
```

Expected

- Intent: FILE_UPLOAD
- Sentiment: NEGATIVE
- Urgency: MEDIUM
- Knowledge: File Upload Limits / Upload Troubleshooting
- Department: Technical Support

---

## Ticket 12 - Ticket Escalation

```json
{
  "subject": "Existing support case needs escalation",
  "message": "Our support ticket has remained in progress for five business days without an update. This issue is affecting our production environment and we'd like to escalate it."
}
```

Expected

- Intent: ESCALATION
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Ticket Escalation Policy
- Department: Customer Success

---

## Ticket 13 - Multi-Factor Authentication

```json
{
  "subject": "Authenticator app verification code is always rejected",
  "message": "I recently enabled two-factor authentication, but every login attempt says the verification code is invalid. I've synchronized my phone time but it still doesn't work."
}
```

Expected

- Intent: AUTHENTICATION
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Multi-Factor Authentication (2FA)
- Department: Security Operations

---

## Ticket 14 - Single Sign-On Configuration

```json
{
  "subject": "Unable to configure SAML Single Sign-On",
  "message": "We're trying to configure SAML SSO for our Enterprise organization, but users are redirected back to the login page after authenticating with our identity provider."
}
```

Expected

- Intent: SSO
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Single Sign-On (SSO)
- Department: Identity & Access Management

---

## Ticket 15 - Account Lockout

```json
{
  "subject": "My account has been locked after failed login attempts",
  "message": "I accidentally entered the wrong password several times and now my account is locked. How can I regain access immediately?"
}
```
Expected

- Intent: ACCOUNT_LOCKOUT
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Account Lockout
- Department: Technical Support

---

## Ticket 16 - Profile Management

```json
{
  "subject": "Unable to update my email address",
  "message": "Whenever I change my email address in my profile, the changes aren't saved. I also never receive the verification email."
}
```

Expected

- Intent: PROFILE_MANAGEMENT
- Sentiment: NEGATIVE
- Urgency: MEDIUM
- Knowledge: Profile Management
- Department: Customer Success

---

## Ticket 17 - Missing Invoice

```json
{
  "subject": "Invoice for last month is missing",
  "message": "I need last month's invoice for accounting purposes, but it isn't available in the Billing Dashboard."
}
```

Expected

- Intent: BILLING
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: Downloading Invoices and Receipts
- Department: Billing

---

## Ticket 18 - Duplicate Charge

```json
{
  "subject": "Charged twice for the same subscription",
  "message": "My credit card statement shows two identical charges for the same subscription renewal. Could someone investigate?"
}
```

Expected

- Intent: BILLING
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: Duplicate Charges
- Department: Billing

---

## Ticket 19 - Incorrect Proration

```json
{
  "subject": "Upgrade charge seems incorrect",
  "message": "We upgraded our subscription yesterday, but the prorated amount on the invoice doesn't look correct."
}
```

Expected

- Intent: BILLING
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: Subscription Billing and Proration
- Department: Billing

---

## Ticket 20 - Browser Compatibility

```json
{
  "subject": "Dashboard doesn't load properly in Safari",
  "message": "The application works correctly in Chrome but several pages fail to load in Safari after the latest update."
}


Expected

- Intent: BROWSER_COMPATIBILITY
- Sentiment: NEGATIVE
- Urgency: LOW
- Knowledge: Browser Compatibility
- Department: Technical Support

```
## Ticket 21 - Email Notifications

```json
{
  "subject": "Not receiving email notifications",
  "message": "Our users stopped receiving notification emails for ticket updates, although notification settings are enabled."
}
```

Expected

- Intent: EMAIL_NOTIFICATION
- Sentiment: NEGATIVE
- Urgency: MEDIUM
- Knowledge: Email Notifications
- Department: Technical Support

---

## Ticket 22 - Organization Settings

```json
{
  "subject": "Unable to update organization settings",
  "message": "Changes to our organization name and billing contact never get saved, even though I'm an administrator."
}
```

Expected

- Intent: ORGANIZATION_MANAGEMENT
- Sentiment: NEGATIVE
- Urgency: MEDIUM
- Knowledge: Organization Settings
- Department: Customer Success

---

## Ticket 23 - API Authentication

```json
{
  "subject": "API requests return Unauthorized",
  "message": "Every API request now returns HTTP 401 Unauthorized after rotating our API credentials."
}
```

Expected

- Intent: API_AUTHENTICATION
- Sentiment: NEGATIVE
- Urgency: HIGH
- Knowledge: API Authentication
- Department: Developer Support

## Ticket 24 - Audit Logs

```json
{
  "subject": "Need audit logs for compliance review",
  "message": "Our compliance team needs audit logs covering all administrator actions from the past ninety days."
```

Expected

- Intent: AUDIT_LOGS
- Sentiment: NEUTRAL
- Urgency: MEDIUM
- Knowledge: Audit Logs
- Department: Compliance

## Ticket 25 - Data Retention Policy

```json
{
  "subject": "How long is customer data retained?",
  "message": "We're reviewing our compliance policies and need to understand how long customer information is retained after an account is closed."
}
```

Expected

- Intent: DATA_RETENTION
- Sentiment: NEUTRAL
- Urgency: LOW
- Knowledge: Data Retention Policy
- Department: Privacy & Compliance
