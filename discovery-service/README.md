# Discovery Service

Eureka Server for Service Discovery and Registration in the AI Support System.

## Overview
This service acts as the registry where other microservices (Ticket Service, AI Analysis Service) register themselves. Clients can use this service to locate other services without hardcoding hostnames and ports.

## Configuration
| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8761 | Standard Eureka Port |
| Dashboard | Enabled | Access UI at http://localhost:8761 |

## Accessing the Dashboard
Once the service is running, open [http://localhost:8761](http://localhost:8761) to view registered instances.

## Running Locally
```bash
mvn spring-boot:run
```
