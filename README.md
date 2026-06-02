# Spring Boot Microservice eCommerce App

A microservices-based eCommerce backend built with Spring Boot, featuring reactive programming, containerization, and a composite API pattern.

## Architecture

```
┌─────────────────────────────────┐
│      product-composite-service  │  <- API Gateway (port 8080)
└────────────┬────────────────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
product  recommendation  review
service    service      service
    │                    │
  MongoDB              MySQL
```

## Services

| Service | Description | Database |
|---|---|---|
| `product-service` | Manages product catalog | MongoDB |
| `recommendation-service` | Product recommendations | MongoDB |
| `review-service` | Product reviews | MySQL |
| `product-composite-service` | Aggregates all services | — |

## Shared Modules

- **`api/`** — Shared API interfaces and exception types
- **`util/`** — Shared HTTP utilities and error handling

## Tech Stack

- **Java 8** / **Spring Boot 2.6.3**
- **Spring WebFlux** — Reactive REST APIs
- **Spring Data MongoDB** — Product & recommendation persistence
- **Spring Data JPA / MySQL** — Review persistence
- **MapStruct** — Entity/DTO mapping
- **Docker & Docker Compose** — Containerization
- **Testcontainers** — Integration testing

## Getting Started

### Prerequisites

- Java 8+
- Docker Desktop
- Gradle

### Run with Docker Compose

```bash
docker-compose up --build
```

This starts all 4 microservices along with MongoDB and MySQL.

### Access the API

```
http://localhost:8080
```

### Build Locally

```bash
./gradlew build
```

## Project Structure

```
├── api/                           # Shared API interfaces & exceptions
├── util/                          # Shared utilities
├── microservices/
│   ├── product-service/           # Core product service
│   ├── recommendation-service/    # Recommendation service
│   ├── review-service/            # Review service
│   └── product-composite-service/ # Composite/aggregator service
├── docker-compose.yml
└── settings.gradle
```

## Frontend

> Frontend integration coming soon. This section will be updated as frontend features are added.

## Documentation

- [Distributed Systems Notes](distributed.md)
- [Persistence Layer](persistence.md)
- [Event-Driven Architecture](event-driven.md)
- [Service Discovery](service-disovery.md)
- [Kubernetes Deployment](kubernetes.md)
- [DevOps Notes](devops.md)
- [OpenAPI](openApi.md)
