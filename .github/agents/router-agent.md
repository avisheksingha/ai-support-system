# Routing Service Agent

**Role:** Rule-Based Ticket Router (Domain Capability)

**Port:** 8084

**Responsibility:** Provides deterministic ticket routing as a domain capability. Evaluates active routing rules in priority order, records rule execution history. Exposes an internal REST endpoint consumed by the `ai-orchestration-service` via Tool Calling.

## Quick Commands

### Build
```bash
mvn -pl routing-service clean install
```

### Run Service
```bash
cd routing-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl routing-service test
mvn -pl routing-service -Dtest=RoutingServiceTest,RuleEvaluationServiceTest test
```

## Key Files

- **Internal Controller:** `src/main/java/com/aisupport/routing/controller/InternalRoutingController.java`
- **Consumer (legacy):** `src/main/java/com/aisupport/routing/consumer/TicketAnalyzedConsumer.java`
- **Routing Service:** `src/main/java/com/aisupport/routing/service/RoutingService.java`
- **Rule Evaluation:** `src/main/java/com/aisupport/routing/service/RuleEvaluationService.java`
- **Rule Entity:** `src/main/java/com/aisupport/routing/entity/RoutingRule.java`
- **History Entity:** `src/main/java/com/aisupport/routing/entity/RuleExecutionHistory.java`
- **Rule Repo:** `src/main/java/com/aisupport/routing/repository/RoutingRuleRepository.java`
- **History Repo:** `src/main/java/com/aisupport/routing/repository/RuleExecutionHistoryRepository.java`

## Key Responsibilities & Flow

1. Orchestrator calls `POST /api/internal/routing/route` with analysis results.
2. Load active rules ordered by priority.
3. Match by intent/sentiment/urgency/keywords.
4. Persist execution history per evaluated rule.
5. Build routed result (team, priority, SLA fallback values when no match).
6. Return routing decision to orchestrator.

## Current API Endpoints

### Internal Endpoints (Orchestrator Only)

- `POST /api/internal/routing/route` — Invoked by `ai-orchestration-service` as an AI Tool.

## Database Snapshot

### routing_rules
- `id` (Long, PK)
- `rule_name`, `description`, `rule_version`
- `priority`, `active`
- `intent_pattern`, `sentiment_pattern`, `urgency_pattern`, `keyword_patterns` (TEXT[])
- `assign_to_team`, `priority_override`, `sla_hours`
- `created_at`, `updated_at`, `created_by`, `updated_by`

### rule_execution_history
- `id` (Long, PK)
- `rule_id`, `ticket_id`, `matched`, `execution_time_ns`, `executed_at`

## Common Tasks

### Verify Rule Rows
```sql
SELECT id, rule_name, priority, active, assign_to_team, priority_override, sla_hours
FROM routing_rules
ORDER BY priority ASC;
```

### Verify Rule Execution History
```sql
SELECT ticket_id, rule_id, matched, execution_time_ns, executed_at
FROM rule_execution_history
ORDER BY executed_at DESC
LIMIT 50;
```

## Important Rules

- Primary invocation is via orchestrator REST Tool Calling, not direct Kafka consumption.
- Keep routing logic data-driven via DB rules.
- Preserve priority ordering for deterministic evaluation.
- Preserve correlation-id header/MDC propagation in all flows.

## Environment Variables

None specified.

## Related Services

- Invoked by `ai-orchestration-service` via internal REST API (Tool Calling).
- Routing decisions are included in the `ticket-orchestrated` event published by the orchestrator.

## Debugging Tips

1. No route decision: inspect active rules and exact pattern values.
2. Unexpected fallback routing: check intent/sentiment/urgency normalization from upstream analysis.
3. Orchestrator not receiving routing: verify internal endpoint availability and service discovery.
