# AI Support Parent

Parent Maven Project for the AI Support System microservices platform.

## Overview

This project serves as the parent Object Model (POM) for all microservices in the AI Support System. It centralizes:

- **Dependency Management**: Ensures consistent versions across all modules (Spring Boot, Spring Cloud, Spring AI, etc.).
- **Build Configuration**: Defines plugins and profiles used by all services.
- **Project Inheritance**: Simplifies the child `pom.xml` files by providing shared properties and dependencies.

## Key Technologies

- **Java**: 21
- **Spring Boot**: 4.0.3
- **Spring Cloud**: 2025.1.0
- **Spring AI**: 2.0.0-M1

## Building the Entire Project

From this directory (or the root directory), you can build all modules at once:

```bash
mvn clean install
```

## Modular Structure

The parent POM manages the following modules:
- `discovery-service`
- `api-gateway` (orchestrated via root)
- `ticket-service`
- `ai-analysis-service`
- `routing-service`
- `common-library`
