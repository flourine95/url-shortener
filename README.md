# High-throughput URL shortener platform

[![Java Version](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This project implements a high-throughput URL shortener in Java 21 and Spring Boot 4. It compares the performance of database caching and messaging pipelines across four architectural iterations.

## Key features

- **Hexagonal Architecture**: Decouples business logic from external database adapters, cache libraries, and transport protocols.
- **Cache-Aside Pattern**: Integrates Redis read-through caching to shield PostgreSQL from repetitive lookup traffic.
- **Event-Driven Decoupling**: Offloads click analytics logging to Apache Kafka, protecting redirect latencies from disk write speeds.
- **k6 Load-Testing Suite**: Validates performance metrics using baseline, load, and endurance test profiles.

## Performance snapshot

The following table summarizes the throughput and P95 latency metrics recorded across the four architectural profiles:

| Milestone | Spring Profile | Port | Database Write | Throughput | P95 Latency | Notes |
|---|---|---|---|---:|---:|---|
| `v1.0.0-postgres` | `v1` | `8081` | None | 1566.45 RPS | 43.59 ms | PostgreSQL baseline |
| `v1.1.0-redis` | `v2` | `8082` | None | 2628.61 RPS | 12.94 ms | Redis cache-aside |
| `v1.2.0-sync-analytics` | `v3` | `8083` | Synchronous | 602.37 RPS | 119.46 ms | Write bottleneck |
| `v2.0.0-kafka-async` | `v4` | `8084` | Asynchronous | 1147.17 RPS | 62.41 ms | Decoupled analytics |

Read [docs/benchmark.md](./docs/benchmark.md) for full methodologies, load testing profiles, and memory stability charts.

## Quick Start (Run Latest Version)

The easiest way to run the entire stack (including the Spring Boot application, PostgreSQL, Redis, and Kafka) is using Docker Compose. The application container will automatically compile the Java project on its first run:

1. **Start all services**:
   ```bash
   docker compose -f docker/docker-compose.v4.yml up -d
   ```
2. **Verify the API**:
   * **Linux / macOS (Bash/Zsh)**:
     ```bash
     curl -X POST http://localhost:8084/api/urls \
       -H "Content-Type: application/json" \
       -d '{"originalUrl": "https://example.com"}'
     ```
   * **Windows (PowerShell)**:
     ```powershell
     Invoke-RestMethod -Uri http://localhost:8084/api/urls -Method Post -ContentType "application/json" -Body '{"originalUrl": "https://example.com"}'
     ```
   * **Redirect lookup** (replace `abc123` with your generated short code):
     ```bash
     curl -I http://localhost:8084/abc123
     ```

## Prerequisites

- **Docker**: Docker Desktop (or engine) with Docker Compose installed
- **Java** (Optional, only for local IDE runs): JDK 21 or higher
- **k6** (Optional, only for running benchmarks): installed on your system

## Documentation index

- [System architecture](./docs/architecture.md): Hexagonal layers, database schemas, and request data flows.
- [REST API reference](./docs/api.md): Endpoint payloads, JSON structures, and status code specifications.
- [Performance benchmarks](./docs/benchmark.md): Baseline metrics, 500 VUs load tests, and 1h endurance run resource profiles.
- [Architecture progression](./docs/versions.md): Architectural evolution (v1.0.0 to v2.0.0) and detailed technical trade-offs.

## Running benchmarks locally

Ensure you have Docker and k6 installed on your system.

### Option 1: Run the baseline comparison benchmark

This option runs a quick baseline test for 30s across all four versions to verify relative performance trends:

```bash
# Run inside Ubuntu (WSL2)
./benchmark/scripts/run-benchmarks.sh
```

### Option 2: Run load and endurance tests on the Kafka profile

This option opens a terminal prompt manager to execute advanced tests against the `v4-kafka-async` profile:

```bash
# Run inside Windows PowerShell or Ubuntu terminal
python3 benchmark/scripts/run_v4_advanced.py
```

The script displays an interactive menu to choose between:

1. **Load Test**: 500 VUs running for 10m.
2. **Endurance Test**: 200 VUs running for 1h (generates CPU and memory logging CSV files).
3. **Custom Run**: Customizable VU count, durations, and resource logging setups.
