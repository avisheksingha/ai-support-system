# Discovery Service

Eureka Server for Service Discovery and Registration in the AI Support System.

## Overview
This service acts as the central registry where all microservices in the platform register themselves. It enables dynamic service-to-service communication without hardcoded URLs.

Registered services include:
- `api-gateway`
- `ticket-service`
- `ai-analysis-service`
- `rule-engine-service`
- `routing-service`

## Configuration
| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8761 | Standard Eureka Port |
| Dashboard | Enabled | Access UI at http://localhost:8761 |

## Accessing the Dashboard
Once the service is running, open [http://localhost:8761](http://localhost:8761) to view registered instances and their health status.

## Running Locally
```bash
mvn spring-boot:run
```
