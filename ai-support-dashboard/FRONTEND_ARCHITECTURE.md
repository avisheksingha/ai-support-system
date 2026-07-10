# Frontend Architecture: AI Support Dashboard

This document outlines the architectural decisions and design patterns used in the AI Support Dashboard, which serves as the "GenAI Operations Console" for the backend microservices.

## 1. Feature-Driven Architecture

The project abandons the traditional flat `components/`, `hooks/`, and `pages/` structure in favor of a **Feature-Driven Architecture**.

```text
src/
  features/
    auth/
      api/
      components/
      hooks/
      pages/
    workspace/
      api/
      components/
      hooks/
      pages/
  shared/
    types/
  components/
    ui/ (Shadcn)
```

**Why?**
As the application scales (e.g., adding an Operations Center or Administration feature), the codebase remains localized. Developers working on the Ticket Workspace don't need to sift through global API or Hook directories; everything related to the workspace is co-located.

## 2. Abstraction of Orchestration (`workspaceApi.ts`)

Instead of allowing React components to directly call the Ticket Service, Analysis Service, RAG Service, and Routing Service, we've introduced an explicit API layer: `workspaceApi.ts`.

**Why?**
Currently, the backend does not have a unified Orchestration Service. The frontend `workspaceApi` temporarily acts as this orchestrator, fetching data from multiple microservices concurrently. When the backend `ai-orchestration-service` is eventually built, *only* `workspaceApi.ts` will need to change. The React components (`useWorkspace`, `AiInsightsPanel`, etc.) will remain completely untouched. This is a highly scalable enterprise pattern.

## 3. Isolated Domain Models

Rather than flattening all AI metadata into a single bloated `TicketResponse` object, we mapped the frontend models strictly to the backend microservice boundaries:

- `TicketModel`: Core lifecycle data (Ticket Service).
- `AnalysisModel`: Sentiment, Intent, Urgency (Analysis Service).
- `KnowledgeModel`: RAG responses, similarity scores, source docs (RAG Service).
- `RoutingModel`: Suggested department and confidence (Routing Service).
- `TimelineEvent`: Asynchronous lifecycle events.

**Why?**
It forces the UI to respect the eventual consistency and asynchronous nature of the Kafka event-driven backend. It allows us to render elegant "Empty States" (e.g., *Waiting for AI Analysis...*) for individual panels while the rest of the ticket is fully usable.

## 4. Business Terminology vs Technical Reality

We made a conscious UX decision to decouple backend technical terms from the user-facing UI:

- `AI Analysis` -> **AI Insights**
- `RAG Response` -> **Knowledge**
- `Routing Decision` -> **Assignment**
- `Timeline` -> **Activity**

This elevates the dashboard from a developer utility to an Enterprise Product.

## 5. React Query for State Management

We utilize `@tanstack/react-query` to handle all server state, leveraging a centralized `workspaceKeys` factory.
This provides automatic caching, background refetching, and simplified optimistic updates (e.g., updating Priority or assigning a ticket instantly updates the local cache and fires a Toast notification).

---

## Manual Demo Script

For technical interviews or GitHub recordings, follow this sequence to demonstrate the end-to-end Kafka event flow:

1. **Login**: Authenticate as a Support Agent (demonstrates the `auth-service` JWT flow).
2. **Open Workspace**: Navigate to the Ticket Workspace.
3. **Trigger Event**: Use Postman/Bruno to `POST /api/v1/tickets` through the API Gateway.
4. **Observe Asynchronous Flow**:
   - The Ticket List updates (via React Query refetch or Websockets).
   - Click the new ticket. Notice the Skeleton loaders or Empty States ("Waiting for AI...").
   - As Kafka processes the events (`ticket-created` -> `ticket-analyzed` -> `ticket-rag-response` -> `ticket-routed`), the UI panels progressively populate with semantic confidence bars, knowledge previews, and rule-based routing explanations.
5. **Activity Feed**: Point out the color-coded Activity Feed mapping exactly to the backend microservice handoffs.
6. **Action**: Change the ticket status to `RESOLVED` and watch the Toast notification pop up, demonstrating the immediate React Query cache invalidation and UI update.
