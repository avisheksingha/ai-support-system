# Routing Service Agent

**Role:** Rule-Based Ticket Router

**Port:** 8084

**Responsibility:** Consumes analyzed ticket events, evaluates active routing rules in priority order, records rule execution history, and publishes `TicketRoutedEvent` through outbox.

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

- **Consumer:** `src/main/java/com/aisupport/routing/consumer/TicketAnalyzedConsumer.java`
- **Routing Service:** `src/main/java/com/aisupport/routing/service/RoutingService.java`
- **Rule Evaluation:** `src/main/java/com/aisupport/routing/service/RuleEvaluationService.java`
- **Rule Entity:** `src/main/java/com/aisupport/routing/entity/RoutingRule.java`
- **History Entity:** `src/main/java/com/aisupport/routing/entity/RuleExecutionHistory.java`
- **Rule Repo:** `src/main/java/com/aisupport/routing/repository/RoutingRuleRepository.java`
- **History Repo:** `src/main/java/com/aisupport/routing/repository/RuleExecutionHistoryRepository.java`
- **Outbox:** `src/main/java/com/aisupport/routing/outbox/OutboxEventService.java`, `OutboxEventPublisher.java`

## Runtime Flow

1. Consume `ticket-analyzed` event.
2. Load active rules ordered by priority.
3. Match by intent/sentiment/urgency/keywords.
4. Persist execution history per evaluated rule.
5. Build routed result (team, priority, SLA fallback values when no match).
6. Publish `TicketRoutedEvent` via outbox.

## Current Defaults in Code

If no matching rule:
- `team = general-support`
- `priority = MEDIUM`
- `slaHours = 24`

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

### Verify Outbox Routing Events
```sql
SELECT aggregate_id, event_type, status, retry_count, processed_at
FROM outbox_events
WHERE event_type = 'TicketRoutedEvent'
ORDER BY created_at DESC;
```

## Schema Snapshot (from entities)

### routing_rules
- `id` (Long, PK)
- `rule_name`, `description`
- `priority`, `active`
- `intent_pattern`, `sentiment_pattern`, `urgency_pattern`, `keyword_patterns` (TEXT[])
- `assign_to_team`, `priority_override`, `sla_hours`
- `created_at`, `updated_at`, `created_by`, `updated_by`

### rule_execution_history
- `id` (Long, PK)
- `rule_id`, `ticket_id`, `matched`, `execution_time_ns`, `executed_at`

## Important Rules

- Keep routing logic data-driven via DB rules.
- Preserve priority ordering for deterministic evaluation.
- Keep outbox publication for integration events.
- Preserve correlation-id header/MDC propagation in consumer flow.

## Related Services

- Consumes `ticket-analyzed` from `ai-analysis-service`
- Produces `ticket-routed` for `ticket-service`

## Debugging Tips

1. No route decision: inspect active rules and exact pattern values.
2. Unexpected fallback routing: check intent/sentiment/urgency normalization from upstream analysis.
3. Event not reaching ticket-service: verify outbox publisher status and Kafka topic delivery.
