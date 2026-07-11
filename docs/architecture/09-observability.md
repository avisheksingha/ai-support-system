# Observability

Version: 1.0
Status: Current
Last Updated: 2026-07-11

Because the platform is fundamentally event-driven and AI-centric, standard REST monitoring is insufficient.

## 1. Metrics (Micrometer)
The runtime uses Micrometer to expose metrics to Prometheus. 
Key metrics tracked:
- Active workflow count
- Guardrail blocks
- Policy denials
- Tool failures

## 2. Distributed Tracing (OpenTelemetry)
Tracing spans across microservices via W3C Trace Context headers. A single execution trace might look like:
```text
Ticket Created (Ticket Service) -> Workflow Engine (Orchestrator) -> Agent Loop -> Tool Execution -> Ticket Analyzed (Orchestrator)
```

## 3. Audit Logging
See [Governance & Audit](07-governance-and-audit.md) for details on the `AiExecutionRecordEntity`.
