# Benchmark URL shortener architecture

A URL shortener built with Spring Boot 4 and Java 21, following Hexagonal Architecture. This repo benchmarks four architectural evolutions side-by-side in isolated containerized environments.

## Architectural evolutions and profiles

Spring Profiles toggle the active version from a single compiled binary or Docker image:

| Version | Spring Profile | Port | URL repository | Analytics adapter | External services |
|---|---|---|---|---|---|
| `v1-postgres` | `v1` | `8081` | `PostgresUrlRepositoryImpl` | `NoopAnalyticsAdapter` | PostgreSQL |
| `v2-redis` | `v2` | `8082` | `CachedUrlRepositoryImpl` (Redis + DB) | `NoopAnalyticsAdapter` | PostgreSQL + Redis |
| `v3-sync-analytics` | `v3` | `8083` | `CachedUrlRepositoryImpl` (Redis + DB) | `SyncAnalyticsAdapter` | PostgreSQL + Redis |
| `v4-kafka-async` | `v4` | `8084` | `CachedUrlRepositoryImpl` (Redis + DB) | `KafkaAnalyticsAdapter` | PostgreSQL + Redis + Kafka |

## Package layout

Files are split into four layers:

- **`application/`**: REST controllers and HTTP request/response DTOs
- **`domain/`**: Domain models, command/query DTOs, and use case interfaces (input ports)
- **`infrastructure/`**: Database persistence, Spring Data repositories, cache, messaging adapters (Kafka), and mappers
- **`shared/`**: Configurations, exception handling, and response wrappers (`ApiResult`)

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

## Documentation

- [docs/versions.md](docs/versions.md): architectural evolution across four tagged milestones
- [docs/benchmark.md](docs/benchmark.md): load testing methodology and performance comparison

## Running benchmarks

### On Linux or WSL2 (recommended)

Install `jq` (`sudo apt install jq`), then run:

```bash
chmod +x benchmark/run-benchmarks.sh
./benchmark/run-benchmarks.sh
```

### On Windows (PowerShell)

```powershell
powershell -ExecutionPolicy Bypass -File benchmark/run-benchmarks.ps1
```

See [docs/benchmark.md](docs/benchmark.md) for methodology, manual test steps, and full results.
