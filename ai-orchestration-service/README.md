# AI Orchestration Service

## Purpose

The AI Orchestration Service is the central workflow runtime for the AI Support System platform. It acts as an intelligent coordinator that consumes business events and composes various domain capabilities (Analysis, Routing, RAG) to execute automated AI workflows.

## Responsibilities

- **Workflow Orchestration**: Manages the end-to-end AI execution lifecycle (e.g., Ticket Triage Workflow).
- **Capability Composition**: Orchestrates domain capability services via synchronous tool calling.
- **Context Assembly**: Gathers necessary state and context before invoking AI reasoning.
- **State Machine Management**: Transitions workflows between states based on AI decisions.
- **Policy Enforcement**: Applies business guardrails during execution.

## Key Components

- **Workflow Runtime**: Event-driven engine that tracks and resumes workflow state.
- **AI Agent Abstraction**: Spring AI-based implementations (like `SpringAiAgent`) for reasoning.
- **Tool Registry**: Dynamic registry mapping workflow actions to downstream REST calls.
- **Context Providers**: Interceptors that fetch context from systems (like the RAG Service).

## External Dependencies

- **Spring AI**: For reasoning and tool execution logic.
- **PostgreSQL**: For workflow state persistence.
- **Kafka**: For event-driven triggering and completion publishing.

## Events Consumed

- `TicketCreatedEvent`: Triggers the primary AI triage workflow.

## Events Produced

- `TicketOrchestratedEvent`: Emitted when a workflow successfully finishes processing a ticket. The `ticket-service` consumes this event to apply the final AI analysis, routing decision, and knowledge context.

## Internal Service Clients

The Orchestrator utilizes synchronous REST calls (often abstracted as AI Tools) to consume the capabilities of other microservices:

- **AI Analysis Service**: Called to perform sentiment, urgency, and intent extraction.
- **Routing Service**: Called to evaluate routing rules based on the gathered context.
- **RAG Service**: Called to fetch contextual knowledge for the agent.

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8086 | Port where service runs |
| Database | PostgreSQL | `orchestration_db` |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
