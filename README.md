# Benchmark URL shortener architecture

This repository contains a URL shortener built with Spring Boot 4 and Java 21, following Hexagonal Architecture. It benchmarks four architectural evolutions side-by-side in isolated containerized environments.

## Architectural evolutions and profiles

Spring Profiles toggle the active version from a single compiled binary or Docker image:

| Version | Spring Profile | Port | URL repository | Analytics adapter | External services |
|---|---|---|---|---|---|
| `v1-postgres` | `v1` | `8081` | `PostgresUrlRepositoryImpl` | `NoopAnalyticsAdapter` | PostgreSQL |
| `v2-redis` | `v2` | `8082` | `CachedUrlRepositoryImpl` (Redis + DB) | `NoopAnalyticsAdapter` | PostgreSQL + Redis |
| `v3-sync-analytics` | `v3` | `8083` | `CachedUrlRepositoryImpl` (Redis + DB) | `SyncAnalyticsAdapter` | PostgreSQL + Redis |
| `v4-kafka-async` | `v4` | `8084` | `CachedUrlRepositoryImpl` (Redis + DB) | `KafkaAnalyticsAdapter` | PostgreSQL + Redis + Kafka |

## Package layout

The project organizes files into four layers:

- **`application/`**: Driving adapters. Contains REST controllers and HTTP request/response DTOs
- **`domain/`**: Business logic core. Defines domain models, command/query DTOs, and use case interfaces (input ports)
- **`infrastructure/`**: Driven adapters. Implements database persistence, Spring Data repositories, cache, messaging adapters (Kafka), and mappers
- **`shared/`**: Cross-cutting concerns. Defines configurations, exception handling, and response wrappers (`ApiResult`)

## How to build and run

### Prerequisites

- Java 21 (JDK 21)
- Docker and Docker Compose
- [k6](https://k6.io/) for load testing

### Build the application

```bash
./gradlew build -x test
```

### Build the Docker image

```bash
docker build -t url-shortener-app:latest -f docker/Dockerfile .
```

## Project documentation

For architectural details and performance measurements, see:

- [Architectural evolution and versions](docs/versions.md): Describes what changes in each milestone version and how class mappings evolve from JPA to decorated cache and Kafka pub-sub
- [Performance benchmark results](docs/benchmark.md): Displays the performance comparison metrics (RPS, latency) measured on WSL2 with analysis of read and write bottlenecks

## Running benchmarks

The repository includes automated benchmark orchestrators. Each orchestrator spins up isolated stacks sequentially, waits for health probes, triggers k6 load tests, compiles metrics, and generates reports.

### On Linux or WSL2 (recommended)

Install `jq` (`sudo apt install jq`), then run:

```bash
chmod +x benchmark/run-benchmarks.sh
./benchmark/run-benchmarks.sh
```

### On Windows (PowerShell)

Open PowerShell and run:

```powershell
powershell -ExecutionPolicy Bypass -File benchmark/run-benchmarks.ps1
```

## Testing a specific version manually

To spin up a single architectural stack and explore the APIs:

1. Start the stack (version 4 in this example):
   ```bash
   docker compose -f docker/docker-compose.v4.yml up -d
   ```
2. Shorten a URL:
   ```bash
   curl -X POST http://localhost:8084/api/urls \
     -H "Content-Type: application/json" \
     -d '{"originalUrl": "https://github.com", "customCode": "myrepo"}'
   ```
3. Trigger redirection:
   ```bash
   curl -i http://localhost:8084/myrepo
   ```
4. Tear down the stack when done:
   ```bash
   docker compose -f docker/docker-compose.v4.yml down -v
   ```
