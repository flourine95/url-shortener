---
meta.contentType: How-to
---

# Benchmark URL shortener architecture

This guide explains the architecture, package layout, and instructions for building, running, and benchmarking the URL shortener evolutions.

The application uses **Spring Boot 4** and **Java 21**, following **Hexagonal Architecture (Ports and Adapters)**, **Domain-Driven Design (DDD)**, and the **Nitrotech API Layered Architecture** standards.

You can benchmark four architectural evolutions of the URL shortener side-by-side in isolated containerized environments.

## Architectural evolutions and profiles

Spring Profiles toggle the active URL shortener versions dynamically from a single compiled binary or Docker image:

| Version | Spring Profile | Port | active URL Repository | active Analytics | External Services |
|---|---|---|---|---|---|
| **`v1.0.0-postgres`** | `v1` | `8081` | `PostgresUrlRepositoryImpl` | `NoopAnalyticsAdapter` | PostgreSQL |
| **`v1.1.0-redis`** | `v2` | `8082` | `CachedUrlRepositoryImpl` (Redis + DB) | `NoopAnalyticsAdapter` | PostgreSQL + Redis |
| **`v1.2.0-sync-analytics`** | `v3` | `8083` | `CachedUrlRepositoryImpl` (Redis + DB) | `SyncAnalyticsAdapter` | PostgreSQL + Redis |
| **`v2.0.0-kafka-async`** | `v4` | `8084` | `CachedUrlRepositoryImpl` (Redis + DB) | `KafkaAnalyticsAdapter` | PostgreSQL + Redis + Kafka |

## Package layout

The project follows the Nitrotech API standard and organizes files into four layers:

- **`application/`** (Driving Adapters): Contains REST controllers and HTTP request and response DTOs.
- **`domain/`** (Business Logic Core): Defines pure domain models, command and query DTOs, and use case interfaces (input ports).
- **`infrastructure/`** (Driven Adapters): Implements database persistence entities, Spring Data repositories, cache implementations, messaging adapters (Kafka), and mappers.
- **`shared/`** (Cross-Cutting Concerns): Defines global configurations, exceptions handling, and response wrappers (`ApiResult`).

## How to build and run

### Prerequisites

- Java 21 (JDK 21)
- Docker and Docker Compose
- [k6](https://k6.io/) (for load testing)

### Build the application

```bash
./gradlew build -x test
```

### Build Docker image

```bash
docker build -t url-shortener-app:latest -f docker/Dockerfile .
```
