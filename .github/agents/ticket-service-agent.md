# Ticket Service Agent

**Role:** Ticket Lifecycle Management

**Port:** 8082

**Responsibility:** Exposes ticket APIs, enforces ticket state transitions, persists ticket data, publishes `TicketCreatedEvent` via outbox, and consumes routing/RAG result events.

## Quick Commands

### Build
```bash
mvn -pl ticket-service clean install
```

### Run Service
```bash
cd ticket-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl ticket-service test
mvn -pl ticket-service -Dtest=TicketControllerTest,TicketServiceBehaviorTest,GlobalExceptionHandlerTest,OutboxEventPublisherTest test
```

### Swagger
```text
http://localhost:8082/swagger-ui.html
```

## Key Files

- **Controller:** `src/main/java/com/aisupport/ticket/controller/TicketController.java`
- **Service:** `src/main/java/com/aisupport/ticket/service/TicketService.java`
- **Entity:** `src/main/java/com/aisupport/ticket/entity/Ticket.java`
- **Repository:** `src/main/java/com/aisupport/ticket/repository/TicketRepository.java`
- **Outbox Entity:** `src/main/java/com/aisupport/ticket/outbox/OutboxEvent.java`
- **Outbox Service:** `src/main/java/com/aisupport/ticket/outbox/OutboxEventService.java`
- **Outbox Publisher:** `src/main/java/com/aisupport/ticket/outbox/OutboxEventPublisher.java`
- **Consumers:** `src/main/java/com/aisupport/ticket/consumer/TicketRoutedConsumer.java`, `TicketRagResponseConsumer.java`

## Current API Endpoints

- `POST /api/v1/tickets`
- `GET /api/v1/tickets/{ticketNumber}`
- `GET /api/v1/tickets/id/{id}`
- `GET /api/v1/tickets?status=<STATUS>`
- `PATCH /api/v1/tickets/{ticketNumber}/status?status=<STATUS>&slaHours=<optional>`
- `PATCH /api/v1/tickets/{ticketNumber}/assign?assignedTo=<name>&slaHours=<optional>`
- `PATCH /api/v1/tickets/{ticketNumber}/priority?priority=<PRIORITY>&slaHours=<optional>`

## State Machine Notes

Core statuses include:
- `NEW`, `ANALYZING`, `ANALYZED`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

Transition checks are enforced in `Ticket.transitionTo(...)`.

## Common Tasks

### Create Ticket
```bash
curl -X POST "http://localhost:8082/api/v1/tickets" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Payment Issue",
    "message": "Card charged twice",
    "customerEmail": "customer@example.com"
  }'
```

### Update Ticket Status
```bash
curl -X PATCH "http://localhost:8082/api/v1/tickets/TICK-001/status?status=IN_PROGRESS"
```

### Assign Ticket
```bash
curl -X PATCH "http://localhost:8082/api/v1/tickets/TICK-001/assign?assignedTo=agent-1"
```

### Update Priority
```bash
curl -X PATCH "http://localhost:8082/api/v1/tickets/TICK-001/priority?priority=HIGH"
```

## Database Snapshot (from entity)

### tickets
- `id` (Long, PK)
- `version` (optimistic lock)
- `ticket_number`, `customer_email`, `customer_name`
- `subject`, `message`
- `status`, `priority`, `assigned_to`
- `intent`, `sentiment`, `urgency`, `sla_hours`
- `rag_response`, `rag_generated_at`
- `created_at`, `updated_at`

### outbox_events
- `id` (String UUID)
- `aggregate_type`, `aggregate_id`, `event_type`, `payload`
- `correlation_id`
- `status` (`PENDING`, `SENT`, `FAILED`, `DEAD`)
- `retry_count`, `created_at`, `processed_at`

## Important Rules

- Use outbox for event publication.
- Keep state transitions valid through domain transition checks.
- Keep consumers focused on event handling + service delegation.
- Preserve correlation id in Kafka and MDC flows.

## Related Services

- Produces for `ai-analysis-service` via `ticket-created`
- Consumes from `routing-service` via `ticket-routed`
- Consumes from `rag-service` via `ticket-rag-response`

## Debugging Tips

1. Outbox stuck: inspect `outbox_events` status/retry columns.
2. Bad status transition: verify allowed transitions in `Ticket` entity.
3. Missing updates from events: verify consumer group offsets and deserialization logs.
