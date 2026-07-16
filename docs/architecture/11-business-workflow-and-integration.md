# 11. V1 Business Workflow & Integration Baseline

This document defines the finalized business workflow, state machine, REST mapping, and WebSocket mapping for the AI Support System (V1 Architecture Baseline).

## 1. Ticket Lifecycle

The ticket progresses through a strict state machine, augmented by the AI Orchestration Service and Customer actions.

1. **SUBMITTED**: The customer submits a ticket. It is stored in the `ticket-service`.
2. **AI_ANALYSIS**: The AI Orchestrator begins analysis (intent, sentiment, urgency).
3. **AI_RESOLUTION_CANDIDATE**: (Optional) The AI suggests a complete resolution to the Customer based on RAG. If the customer confirms it resolves their issue, the ticket transitions directly to **CLOSED** without agent involvement.
4. **ASSIGNMENT_PENDING**: If the AI cannot resolve the issue or the customer rejects the AI resolution, the ticket awaits routing.
5. **ASSIGNED**: The `routing-service` determines the appropriate queue/agent.
6. **AGENT_WORKING**: An agent actively opens the ticket and begins drafting a response or running diagnostics.
7. **WAITING_FOR_CUSTOMER**: The agent replies and awaits customer feedback or further action.
8. **RESOLVED**: The agent provides the final solution. The issue is considered fixed.
9. **CLOSED**: Either via auto-close rules or manual action, the ticket is archived.
10. **REOPENED**: The customer replies to a `RESOLVED` or `CLOSED` ticket within the allowed window.

## 2. Conversation Message Types

Conversations are bi-directional and asynchronous. We define five distinct message types on the timeline:

- **Customer Message**: Input originating from the end-user.
- **Agent Message**: A verified human reply sent to the customer.
- **AI Draft**: An unverified AI-generated response waiting for agent review.
- **Internal Note**: A private message (`isInternal: true`) hidden from the customer but visible to agents.
- **System Event**: A non-message chronological entry (e.g., "Ticket Assigned to Minu", "Priority Escalated to High").

## 3. AI Suggestion Lifecycle

AI insights and draft responses are treated as first-class entities with human-in-the-loop oversight.

1. **GENERATED**: The AI Orchestrator produces a draft response based on intent and RAG knowledge.
2. **REVIEWED**: The agent reads the draft.
3. **EDITED**: The agent makes manual adjustments to the AI draft for accuracy or tone.
4. **ACCEPTED**: The agent sends the (edited or unedited) AI draft to the customer.
5. **REGENERATED**: The agent rejects the draft and prompts the AI with new context to generate a better one.
6. **ESCALATED**: The AI detects a high-risk situation and flags the ticket for supervisor review.
7. **EXPIRED**: A draft that has become stale due to new customer messages or significant ticket changes.

## 4. Ticket Assignment Strategy and Routing

The `routing-service` acts deterministically based on the output of the `ai-analysis-service`.

- **Tier 1 (Automated / Generalist)**: Routine queries (e.g., password reset). Can be auto-replied or assigned to a round-robin general queue.
- **Tier 2 (Specialist)**: Complex queries identified by intent. Routed to specific department queues.
- **VIP / High Urgency**: Escalated immediately to the priority queue. Triggers real-time alerts.
- **Reassignment**: If an agent cannot resolve the issue, they can manually reassign it back to the pool or escalate it to a specific specialist.

## 5. Auto-close and Reopen Rules

- **Auto-Close**: Tickets in the `RESOLVED` state automatically transition to `CLOSED` after **72 hours** of customer inactivity.
- **Reopen Window**: If a customer replies to a `RESOLVED` ticket within the 72-hour window, it reverts to `AGENT_WORKING`.
- **New Ticket**: If a customer replies to a `CLOSED` ticket (post-72 hours), a new ticket is generated with a relational link to the old ticket.

## 6. Notification & Real-time Event Strategy

We use a hybrid approach to balance reliability and real-time responsiveness.

- **REST for CRUD**: All mutations (creating tickets, updating status, adding messages) are synchronous REST calls.
- **WebSocket / STOMP for Real-time Updates**: The frontend subscribes to STOMP topics. When the backend processes a Kafka event, it broadcasts a lightweight notification payload, invalidating the frontend cache.

### Standard WebSocket Event Names

- `TicketCreated`
- `TicketAssigned`
- `TicketUpdated`
- `MessageAdded`
- `AIAnalysisCompleted`
- `WorkflowUpdated`
- `GovernanceUpdated`
- `NotificationCreated`

*(Note: A dedicated Notification Center is reserved for future architecture expansion).*

## 7. Knowledge Article Lifecycle

To support accurate RAG retrieval, Knowledge Articles must follow a strict lifecycle:

- **Draft** → **Published** → **Deprecated** → **Archived**

## 8. API Mapping (Frontend → Backend)

### Ticket Management (`/api/v1/tickets`)

- `GET /api/v1/tickets?status=...` - List tickets
- `GET /api/v1/tickets/{id}` - Get details
- `PATCH /api/v1/tickets/{id}` - Update status/priority
- `POST /api/v1/tickets/{id}/messages` - Add message

### Orchestration & AI (`/api/v1/orchestration`)

- `GET /api/v1/orchestration/tickets/{id}/insights` - Get AI analysis for ticket
- `POST /api/v1/orchestration/tickets/{id}/actions` - Trigger AI action
- `GET /api/v1/orchestration/workflows` - List active workflows
- `GET /api/v1/orchestration/workflows/{id}` - Get workflow details
- `GET /api/v1/orchestration/workflows/{id}/trace` - Get execution traces
- `POST /api/v1/orchestration/workflows/{id}/retry` - Retry workflow

### Governance (`/api/v1/governance`)

- `GET /api/v1/governance/metrics` - High-level KPIs
- `GET /api/v1/governance/approvals?status=PENDING` - Review queue
- `POST /api/v1/governance/approvals/{id}` - Approve/Reject action
- `GET /api/v1/governance/blocked` - View guardrail blocks
- `GET /api/v1/governance/audit-logs` - View chronological execution timeline
