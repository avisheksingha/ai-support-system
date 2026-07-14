# AI Support System - Sample Customer Tickets

These requests are intended for testing the complete AI Support pipeline:

- Ticket Service
- AI Analysis
- RAG
- Routing
- Customer Workspace
- Agent Workspace

---

## Ticket 1 - Login Issue

```json
{
  "subject": "Unable to login after password reset",
  "message": "I reset my password yesterday, but I'm still unable to sign in. It keeps saying invalid credentials even though I'm sure the password is correct. I've already cleared my browser cache twice."
}
```

Expected:

- Intent: LOGIN
- Priority: MEDIUM
- Knowledge: Login Troubleshooting
- Department: Technical Support

---

## Ticket 2 - Payment Failed

```json
{
  "subject": "Payment deducted but order not created",
  "message": "My credit card was charged successfully, but the subscription was never activated. Can you check what happened? I'm worried because the money has already been deducted."
}
```

Expected:

- Intent: PAYMENT
- Priority: HIGH
- Knowledge: Payment Issues
- Department: Billing

---

## Ticket 3 - Refund Request

```json
{
  "subject": "Need refund for cancelled order",
  "message": "I accidentally purchased the wrong plan this morning and cancelled it immediately. Could you please process my refund? My order number is ORD-45892."
}
```

Expected:

- Intent: REFUND
- Priority: MEDIUM
- Knowledge: Refund Policy
- Department: Billing

---

## Ticket 4 - Security Concern

```json
{
  "subject": "Someone logged into my account",
  "message": "I noticed login notifications from another city that I've never visited. I think someone has accessed my account without permission. Please help me secure it."
}
```

Expected:

- Intent: SECURITY
- Priority: CRITICAL
- Knowledge: Account Security
- Department: Security

---

## Ticket 5 - Service Outage

```json
{
  "subject": "Dashboard is not loading",
  "message": "None of our team members can access the dashboard since this morning. Is there an outage happening? We've tried multiple browsers."
}
```

Expected:

- Intent: OUTAGE
- Priority: HIGH
- Knowledge: Service Outage Information
- Department: Platform Operations

---

## Ticket 6 - Application Crash

```json
{
  "subject": "Mobile application crashes instantly",
  "message": "The Android app crashes every time I open it after today's update. Restarting my phone didn't help."
}
```

Expected:

- Intent: APPLICATION
- Priority: HIGH
- Knowledge: App Crash Troubleshooting
- Department: Technical Support

---

## Ticket 7 - API Rate Limit

```json
{
  "subject": "Receiving HTTP 429 responses",
  "message": "Our integration suddenly started returning Too Many Requests responses even though nothing changed in our application. Is there any API limit that we're hitting?"
}
```

Expected:

- Intent: API
- Priority: MEDIUM
- Knowledge: API Rate Limiting
- Department: Developer Support

---

## Ticket 8 - Subscription Upgrade

```json
{
  "subject": "Upgrade subscription immediately",
  "message": "We're outgrowing our current plan and need to upgrade today. Will we be charged only for the remaining period or the full amount again?"
}
```

Expected:

- Intent: SUBSCRIPTION
- Priority: LOW
- Knowledge: Subscription Upgrades and Downgrades
- Department: Sales / Billing

---

## Ticket 9 - GDPR Data Export

```json
{
  "subject": "Need all my personal data",
  "message": "I'd like to download all information your company stores about my account before closing it. Could you guide me through the process?"
}
```

Expected:

- Intent: GDPR
- Priority: MEDIUM
- Knowledge: Data Export and GDPR Compliance
- Department: Privacy & Compliance

---

## Ticket 10 - Escalation Request

```json
{
  "subject": "Support ticket has been waiting too long",
  "message": "My previous support request has been open for several days without any meaningful update. I'd like to escalate this issue because it's affecting our business."
}
```

Expected:

- Intent: ESCALATION
- Priority: HIGH
- Knowledge: Ticket Escalation Process
- Department: Customer Success

---
