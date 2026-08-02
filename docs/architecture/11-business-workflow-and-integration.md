# 11. Business Workflow and Integration Reference

This document describes the currently implemented ticket workflow and the API surface exposed through the API Gateway. It is a reference for the React dashboard and external API consumers; OpenAPI remains the authoritative request/response contract for each service.

## 1. Implemented Ticket Lifecycle

The ticket status enum currently supports:

`NEW` → `ANALYZING` → `ANALYZED` → `ASSIGNED` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`

Agents and administrators can update status, assignment, and priority. The API does not currently expose separate endpoints for auto-close, reopen, AI-draft acceptance, or workflow retry.

## 2. Processing Flow

1. An authenticated customer creates a ticket through `ticket-service`.
2. `ticket-service` persists the ticket and publishes `TicketCreatedEvent` through its transactional outbox.
3. `ai-orchestration-service` consumes the event, obtains analysis, routing, and RAG context through internal service clients, and records workflow state and audit information.
4. On completion, the orchestration service publishes `TicketOrchestratedEvent`; `ticket-service` applies the resulting analysis, route, and knowledge context.

The gateway routes all external `/api/v1/**` requests. Internal `/api/internal/**` capability endpoints are not public gateway routes.

## 3. API Mapping

All routes below require a valid gateway JWT unless explicitly documented as public authentication endpoints. Role annotations on the implementing controller determine authorization.

### Authentication (`/api/v1/auth`)

- `POST /register`, `POST /login`, and `POST /refresh` are public.
- `POST /logout` and `GET /me` require authentication.
- `/admin/users` endpoints require the `ADMIN` role.

### Ticket Management (`/api/v1/tickets`)

- Customers use `POST /`, `GET /my`, `GET /my/{ticketNumber}`, and the equivalent `/my/{ticketNumber}/messages` endpoints.
- Agents and administrators use `GET /`, `GET /{ticketNumber}`, `GET /id/{id}`, status/assignment/priority PATCH endpoints, ticket-message endpoints, and `GET /summary/agent`.
- Ticket deletion is not implemented.

### Orchestration and Context (`/api/v1/orchestration`)

- `GET /tickets/{ticketId}/timeline`, `/insights`, and `/workspace`
- `GET /workflows/search` and `GET /workflows/{workflowId}/timeline`
- `GET /operations/overview`
- `GET /dashboard/admin`, `GET /dashboard/agent`, `GET /dashboard/customer`, and `GET /dashboard/customer/tickets/{ticketNumber}`

### Knowledge Base (`/api/v1/orchestration/knowledge-base`)

- `POST /search`
- `POST /articles`, plus `GET`, `PUT`, and `DELETE /articles/{id}`
- `POST /articles/sync-embeddings`, `GET /stats`, and `POST /articles/bulk-publish`

### Governance (`/api/v1/orchestration/governance`)

- `GET /overview`
- `GET /approval-queue`
- `GET /blocked-requests`
- `GET /audit-logs`
- `GET /active-guardrails`

## 4. Dashboard Integration

The dashboard is a separate Vite application. It calls the gateway at `/api/v1` and uses the orchestration endpoints for role dashboards, timelines, observability, governance, and knowledge-base workflows. The dashboard's API-layer modules are the frontend integration boundary; components should not call backend services directly.

## 5. Real-time Delivery

The gateway forwards `/ws/**` to `ticket-service`. The dashboard currently refreshes much of its server state with TanStack Query polling. WebSocket event payloads and notification-center behavior are not a documented public contract yet and should not be assumed by API consumers.
