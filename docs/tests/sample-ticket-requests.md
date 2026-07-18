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
