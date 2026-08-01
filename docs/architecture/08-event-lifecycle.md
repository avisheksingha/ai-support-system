# Event Lifecycle

Version: 1.0
Status: Current
Last Updated: 2026-07-11

The **AI Orchestration Service** is strictly event-driven via Apache Kafka. It has no REST endpoints exposed to the frontend.

## Topic Topology

- **`ticket-created`**: Emitted by `ticket-service`. Triggers the `AnalyzeWorkflowDefinition`.
- **`ticket-analyzed`**: Emitted by the Orchestrator upon successful workflow completion. Consumed by downstream services like `routing-service` or `rag-service`.
- **`ticket-routed`**: Emitted by `routing-service`.

## The Outbox Pattern

Services do not write to Kafka directly during a JPA transaction. Instead, they write to an `outbox_events` table in the same transaction as the business entity. A background publisher polls the outbox and publishes the messages to Kafka, ensuring *at-least-once* delivery and preventing dual-write inconsistencies.
