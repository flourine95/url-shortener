# Benchmark URL shortener architecture

This guide explains the architecture, package layout, and instructions for building, running, and benchmarking the URL shortener.

The project follows Hexagonal Architecture with a clean layered structure using **Spring Boot 4** and **Java 21**.

You can benchmark four architectural evolutions of the URL shortener side-by-side in isolated containerized environments.

## Architectural evolutions and profiles

Spring Profiles toggle the active URL shortener versions dynamically from a single compiled binary or Docker image:

| Version | Spring Profile | Port | URL repository | Analytics adapter | External Services |
|---|---|---|---|---|---|
| **`v1.0.0-postgres`** | `v1` | `8081` | `PostgresUrlRepositoryImpl` | `NoopAnalyticsAdapter` | PostgreSQL |
| **`v1.1.0-redis`** | `v2` | `8082` | `CachedUrlRepositoryImpl` (Redis + DB) | `NoopAnalyticsAdapter` | PostgreSQL + Redis |
| **`v1.2.0-sync-analytics`** | `v3` | `8083` | `CachedUrlRepositoryImpl` (Redis + DB) | `SyncAnalyticsAdapter` | PostgreSQL + Redis |
| **`v2.0.0-kafka-async`** | `v4` | `8084` | `CachedUrlRepositoryImpl` (Redis + DB) | `KafkaAnalyticsAdapter` | PostgreSQL + Redis + Kafka |

## Package layout

The project organizes files into four clean layers:

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

---

## 📚 Project Documentation & Benchmarks
To help navigate the architectural details and actual performance measurements, refer to the following documents:
- 🗺️ **[Architectural Evolution & Versions (docs/versions.md)](docs/versions.md)**: Describes what changes in each milestone version and how class mappings evolve from clean JPA to decorated cache and Kafka pub-sub.
- 🏎️ **[Performance Benchmark Results (docs/benchmark.md)](docs/benchmark.md)**: Displays the final performance comparison metrics (RPS, Latency) measured on WSL2 with detailed analysis of read and write bottlenecks.

---

## 🏎️ Running Benchmarks
We provide fully automated benchmark orchestrators that will sequentially spin up each isolated stack, wait for health probes, trigger `k6` load tests, compile metrics, and generate reports.

### On Linux / WSL2 (Recommended)
Make sure `jq` is installed (`sudo apt install jq`), then run:
```bash
chmod +x benchmark/run-benchmarks.sh
./benchmark/run-benchmarks.sh
```

### On Windows (PowerShell)
Open PowerShell and run:
```powershell
powershell -ExecutionPolicy Bypass -File benchmark/run-benchmarks.ps1
```

---

## 🧪 Testing a Specific Version Manually
If you want to spin up a single architectural stack to query and explore the APIs manually:

1. **Start the stack** (e.g. Version 4):
   ```bash
   docker compose -f docker/docker-compose.v4.yml up -d
   ```
2. **Shorten a URL**:
   ```bash
   curl -X POST http://localhost:8084/api/urls \
     -H "Content-Type: application/json" \
     -d '{"originalUrl": "https://github.com", "customCode": "myrepo"}'
   ```
3. **Trigger Redirection**:
   ```bash
   curl -i http://localhost:8084/myrepo
   ```
4. **Tear down the stack** when done:
   ```bash
   docker compose -f docker/docker-compose.v4.yml down -v
   ```
