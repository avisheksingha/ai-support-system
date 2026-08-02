# Dashboard Agent

**Role:** GenAI Operations Console (Frontend)

**Port:** 5173 (Vite Dev Server)

**Responsibility:** Provides the user interface for administrators, agents, and customers. It uses a Feature-Driven Architecture and interacts with backend services strictly through the API Gateway (`api-gateway` on port 8080).

## Quick Commands

### Install Dependencies
```bash
cd ai-support-dashboard && npm install
```

### Run Service
```bash
cd ai-support-dashboard && npm run dev
```

## Key Files

- **Features Directory:** `src/features/` (Contains feature-specific api, components, hooks, and pages)
- **Shared Directory:** `src/shared/types/`
- **UI Components:** `src/components/ui/` (Shadcn UI components)
- **API Keys:** `src/features/workspace/api/workspaceKeys.ts`
- **Routing:** `src/App.tsx` or similar routing definitions.

## Key Responsibilities & Architecture

- **Feature-Driven Architecture:** Code is organized by feature (`auth/`, `workspace/`) rather than by type (`components/`, `hooks/`).
- **Backend API Boundary:** Direct ad-hoc HTTP calls are forbidden. Uses feature API layers (e.g., `workspaceApi.ts`) targeting `/api/v1/orchestration/**` for workflow data and `/api/v1/tickets/**` for core ticket data.
- **Isolated Domain Models:** UI models strictly map to backend boundaries (e.g., `TicketModel`, `AnalysisModel`, `KnowledgeModel`, `TimelineEvent`) instead of flattened objects.
- **State Management:** Uses `@tanstack/react-query` for automatic caching, refetching, and optimistic UI updates.
- **Design Aesthetic:** Adheres strictly to the Atlassian Jira-like theme using TailwindCSS and Shadcn UI.

## Common Terminology (Business vs Technical)

- **AI Insights:** Refers to `AI Analysis`.
- **Knowledge:** Refers to `RAG Response`.
- **Assignment:** Refers to `Routing Decision`.
- **Activity:** Refers to `Timeline`.

## Important Rules

- Do not introduce state management libraries like Redux. Stick to React Query.
- Do not make API calls directly from React components. Use the API layer and custom hooks.
- Handle loading and empty states gracefully, especially since AI processing is asynchronous. Use Skeleton loaders where appropriate.

## Environment Variables

- `VITE_API_URL`: Should point to the API Gateway (`http://localhost:8080`).

## Related Services

- Communicates strictly via `api-gateway` (port 8080).
- Primarily interacts with `ai-orchestration-service` (for timeline/insights/knowledge/governance) and `ticket-service` (for core updates).
