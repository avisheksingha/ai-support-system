# Observability Platform

Version: 1.0
Status: Current
Last Updated: 2026-07-13

Because the platform is fundamentally event-driven and AI-centric, standard REST monitoring is insufficient. The orchestration service provides a comprehensive observability platform that surfaces execution metrics and trends.

## 1. Dashboard APIs

The system exposes two primary APIs for observability:

- **`GET /api/v1/operations/overview`**: Returns aggregated analytics across logical domains (Runtime, AI, Governance, Tools, Health, SystemInfo). Supports trend data over time. Filters: `?from=`, `?to=`, `?workflowType=`, `?outcome=`, `?model=`, `?provider=`.
- **`GET /api/v1/workflows/search`**: An explorer API that allows deep drilling into specific executions using `ticketId`, `correlationId`, or `workflowId`. Returns paginated execution details.

## 2. Core DTOs

Metrics are structured into distinct areas in the `OperationsOverviewDTO`:

- `RuntimeMetrics`: Workflow throughput, success/failure counts.
- `AiMetrics`: Token usage (prompt, completion, total), model latencies.
- `GovernanceMetrics`: Blocked requests, policy violations.
- `ProviderMetrics`: Tool invocations, success rates, failures per provider (e.g., github-mcp).
- `HealthMetrics`: Circuit breaker states, Kafka/DB connectivity, outbox queue delays.

## 3. Workflow Explorer & Waterfall

The frontend Operations Center features a **Workflow Explorer**. Execution latency bottlenecks are instantly identifiable through a **Waterfall Chart** built directly from backend checkpoint durations (e.g., Context Assembly -> LLM Generation -> Tools -> Governance).

## 4. Data Ownership (Flow)

Data flows from transactional execution into analytics as follows:

```text
WorkflowExecutionEntity / AiExecutionRecordEntity
   ↓
MetricsQueryService (Read-Only Projections)
   ↓
Operations Dashboard / Workflow Explorer API
   ↓
Frontend Recharts & Timeline
```

## 5. Metrics & Future Integration

The runtime continues to track real-time operational health (Micrometer) and distributed tracing (OpenTelemetry).
While the current dashboard is powered by SQL/JPQL projections on the orchestration database, future V2 architectures are primed for exporting these exact metrics to Prometheus and Grafana for external enterprise monitoring.
