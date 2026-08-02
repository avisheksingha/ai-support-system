# AI Support Dashboard

React and TypeScript dashboard for the AI Support System. It communicates with backend services only through the API Gateway and presents role-specific views for customers, support agents, and administrators.

## Capabilities

- **Customer workspace**: Create tickets, view owned tickets, and exchange messages with support.
- **Agent workspace**: Browse and manage the support queue, update status and priority, assign tickets, and inspect orchestration timelines.
- **Administrator views**: Review user administration, operational workflow metrics, governance data, and knowledge-base management.
- **AI assistance**: Use the writing assistant and view orchestration-provided analysis, routing, and knowledge context where the signed-in role is authorized.

## Prerequisites

- Node.js 20 or later
- The API Gateway running at `http://localhost:8080`
- Backend services required by the selected view. The orchestration features require `ai-orchestration-service` on port `8086`.

## Run Locally

```bash
npm ci
npm run dev
```

Vite prints the local URL; its default is `http://localhost:5173`.

## Environment

`.env.development` contains the default local settings:

```properties
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_REDPANDA_URL=http://localhost:9090
```

`VITE_API_BASE_URL` must include `/api/v1`, because dashboard API clients use paths relative to that prefix. `VITE_REDPANDA_URL` is used for the Kafka console link.

## Useful Commands

```bash
npm run build
npm run lint
npm test
npm run test:e2e
```

For implementation details and API-layer boundaries, see [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md).
