# URL Shortener Benchmarking Architecture

A highly scalable URL Shortener application built using **Spring Boot 4** and **Java 21**, following **Hexagonal Architecture (Ports & Adapters)**, **Domain-Driven Design (DDD)**, and the **Nitrotech API Layered Architecture** standards.

This project is specifically designed to benchmark four different architectural evolutions of a URL shortener side-by-side in isolated containerized environments.

---

## 🚀 Architectural Evolutions & Profiles

All versions are packaged within a **single compiled binary/Docker image** and toggled dynamically using Spring Profiles:

| Version | Spring Profile | Port | active URL Repository | active Analytics | External Services |
|---|---|---|---|---|---|
| **`v1-postgres`** | `v1` | `8081` | `PostgresUrlRepositoryImpl` | `NoopAnalyticsAdapter` | PostgreSQL |
| **`v2-redis`** | `v2` | `8082` | `CachedUrlRepositoryImpl` (Redis + DB) | `NoopAnalyticsAdapter` | PostgreSQL + Redis |
| **`v3-sync-analytics`** | `v3` | `8083` | `CachedUrlRepositoryImpl` (Redis + DB) | `SyncAnalyticsAdapter` | PostgreSQL + Redis |
| **`v4-kafka-async`** | `v4` | `8084` | `CachedUrlRepositoryImpl` (Redis + DB) | `KafkaAnalyticsAdapter` | PostgreSQL + Redis + Kafka |

---

## 📁 Package Layout

Following the **Nitrotech API Standard**, the project is organized into four clean layers:

- **`application/`** (Driving Adapters): REST controllers and HTTP request/response DTOs.
- **`domain/`** (Business Logic Core): Pure domain models, command/query DTOs, and use case interfaces (input ports).
- **`infrastructure/`** (Driven Adapters): Database persistence entities, Spring Data repositories, cache implementations, messaging adapters (Kafka), and mappers.
- **`shared/`** (Cross-Cutting Concerns): Global configuration, exceptions handling, response wrappers (`ApiResult`).

---

## 🛠️ How to Build & Run

### Prerequisites
- Java 21 (JDK 21)
- Docker & Docker Compose
- [k6](https://k6.io/) (for load testing)

### Build the Application
```bash
./gradlew build -x test
```

### Build Docker Image
```bash
docker build -t url-shortener-app:latest -f docker/Dockerfile .
```
