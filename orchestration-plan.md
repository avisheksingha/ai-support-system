# AI Orchestration Service — Architecture Plan

> **Architectural Principle:** The AI Orchestration Service is an execution runtime, not a business service. Its responsibility is to execute workflows, coordinate AI capabilities, and manage workflow lifecycle. Domain intelligence remains within specialized services (AI Analysis, RAG, Routing), while orchestration composes those capabilities into end-to-end AI experiences.

This document proposes the architectural design for introducing the `ai-orchestration-service` into the existing `ai-support-system` microservices ecosystem. It is intended for review to ensure alignment with enterprise design patterns before implementation.

## 1. Current Architecture Context

Currently, the platform relies on **Event-Driven Choreography** over Apache Kafka:
- `ticket-service` (8082) publishes `ticket-created`.
- `ai-analysis-service` (8083) consumes `ticket-created`, performs LLM analysis, and publishes `ticket-analyzed`.
- `routing-service` (8084) and `rag-service` (8085) both independently consume `ticket-analyzed` to route the ticket and generate a response.

**The Problem:** Choreography lacks a central "brain." It is difficult to implement complex, multi-step conditional AI logic (e.g., "If confidence is low, search knowledge base again before routing") without creating a tangled web of Kafka topics.

## 2. The New Orchestration Architecture

We will shift from *Choreography* to an **Agentic Orchestration Model**. The new `ai-orchestration-service` (Port 8086) will act as the central AI supervisor.

### Goal
To build an AI-native Orchestrator that uses an LLM (via Spring AI) with **Tool Calling** to proactively coordinate the other microservices, maintaining conversation history and context, and providing full observability into its reasoning.

### Event Flow Re-wiring
1. `ticket-service` publishes `ticket-created`.
2. **Only** the `ai-orchestration-service` consumes `ticket-created`.
3. The existing consumers in `ai-analysis`, `routing`, and `rag` services will be disabled or re-purposed.
4. The Orchestrator LLM receives the ticket and decides its plan of action.

## 3. Tool Calling & Inter-Service Communication

Spring AI's `@Tool` (Function Calling) requires synchronous responses so the LLM can process the result and decide the next step. 

Since our services currently lack synchronous trigger endpoints, we will introduce **Internal REST APIs** for the orchestrator to invoke them as Tools:

* **Tool 1: `analyzeTicketTool`**
  * Makes a REST call to `ai-analysis-service:8083/api/internal/analyze`
  * *Returns:* Sentiment, Intent, Urgency, Confidence.
* **Tool 2: `searchKnowledgeBaseTool`**
  * Makes a REST call to `rag-service:8085/api/internal/search`
  * *Returns:* Vector-matched knowledge articles.
* **Tool 3: `routeTicketTool`**
  * Makes a REST call to `routing-service:8084/api/internal/route`
  * *Returns:* Department assignment and SLA.

*Note: These internal endpoints bypass the API Gateway and are used strictly for server-to-server orchestration.*

## 4. Observability & AI Memory (Database)

The Orchestration service will have its own PostgreSQL database schema (`orchestration_db`) to store:

1. **`OrchestrationAuditLog`**: Tracks the LLM's step-by-step reasoning (Thought -> Action -> Observation). This provides the "Explainability" required for enterprise AI Governance.
2. **`ConversationContext`**: Stores the multi-turn conversational history for the Spring AI `ChatClient`, allowing the orchestrator to remember past actions for a specific ticket.

## 5. Final Output

Once the LLM decides the ticket is fully processed (e.g., it has analyzed, fetched RAG, and routed), it will construct a final payload and publish a `ticket-orchestrated` event to Kafka (via the Outbox pattern).

The `ticket-service` (and the UI via WebSockets eventually) will listen to this event to update the ticket status and populate the Agent Dashboard's "AI Copilot" recommendations.

## 6. Implementation Steps

1. **Scaffold Service:** Initialize `ai-orchestration-service` (Spring Boot, Spring AI, Web, Kafka, JPA, PostgreSQL, Eureka Client).
2. **REST Endpoints:** Add synchronous `/api/internal/*` endpoints to the Analysis, RAG, and Routing services.
3. **Tool Functions:** Implement `java.util.function.Function` beans in the Orchestrator that wrap `RestClient` calls to the above endpoints.
4. **Agent Workflow:** Configure a `ChatClient` system prompt that instructs the LLM on how to resolve a ticket using the provided tools.
5. **Event Wiring:** Re-route the Kafka consumers so only the Orchestrator listens to `ticket-created`.
6. **Audit Trail:** Implement the persistence layer for the `OrchestrationAuditLog`.
