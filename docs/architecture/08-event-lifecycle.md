# Event Lifecycle

Version: 1.1
Status: Current
Last Updated: 2026-08-02

The **AI Orchestration Service** uses Apache Kafka for event-driven workflow triggering and completion publishing. It also exposes public REST endpoints through the API Gateway for dashboards, operations, governance, knowledge-base management, and workflow timelines.

## Topic Topology

All topic names are defined in `common-library` via `KafkaTopics.java`.

- **`ticket-created`**: Emitted by `ticket-service`. Triggers the `AnalyzeWorkflowDefinition` in the orchestrator.
- **`ticket-analyzed`**: Emitted by `ai-analysis-service`. Consumed by `routing-service` and `rag-service` for downstream processing.
- **`ticket-routed`**: Emitted by `routing-service`.
- **`ticket-rag-response`**: Emitted by `rag-service`.
- **`ticket-orchestrated`**: Emitted by `ai-orchestration-service` upon successful workflow completion. Consumed by `ticket-service` to apply the final AI analysis, routing decision, and knowledge context.
- **`ticket-updated`**: Emitted by `ticket-service` when a ticket is modified.

## The Outbox Pattern

Services do not write to Kafka directly during a JPA transaction. Instead, they write to an `outbox_events` table in the same transaction as the business entity. A background publisher polls the outbox and publishes the messages to Kafka, ensuring *at-least-once* delivery and preventing dual-write inconsistencies.
