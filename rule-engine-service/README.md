# Rule Engine Service

The Rule Engine Service is a microservice that manages and executes business rules for ticket routing and automation in the AI Support System.

## Features

- **Rule Management**: CRUD operations for routing rules.
- **Flexible Conditions**: Evaluate tickets based on sentiment, urgency, intent, or ticket properties.
- **Action Execution**: Define actions like assigning to specific teams (TECHNICAL, BILLING, SUPPORT) or updating priority.
- **History Tracking**: Maintains a record of rule executions for auditing.
- **Dynamic Rules**: Rules can be toggled active/inactive without service restart.

## Core Concepts

- **Rule**: A combination of conditions and an action.
- **Condition**: Criteria like `SENTIMENT == NEGATIVE` or `URGENCY == CRITICAL`.
- **Action**: The outcome, such as `ASSIGN_TEAM` or `SET_PRIORITY`.

## API Endpoints

### Rule Management
- `POST /api/v1/rules`: Create a new routing rule.
- `GET /api/v1/rules`: List all rules (with `activeOnly` filter).
- `GET /api/v1/rules/{id}`: Get rule by ID.
- `PUT /api/v1/rules/{id}`: Update an existing rule.
- `DELETE /api/v1/rules/{id}`: Delete a rule.
- `PATCH /api/v1/rules/{id}/toggle`: Activate/deactivate a rule.

### Evaluation
- `POST /api/v1/rules/evaluate`: Evaluate rules for a specific ticket and return the matching action.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8084 | Port where service runs |
| Database | PostgreSQL | `rule_db` |
| Service Discovery | Enabled | Registers with Eureka |

## Running Locally

```bash
mvn spring-boot:run
```
