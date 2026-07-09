# URL shortener architectural evolutions

This document details the software development steps and architectural differences across the four tagged milestones.

## v1.0.0-postgres (PostgreSQL redirection)

### Evolution summary
The initial baseline version of the URL shortener queries and records redirection metadata directly in a PostgreSQL database.

### Key components
- **Domain Entity**: `UrlData` record containing `id`, `originalUrl`, `shortCode`, `createdAt`, `updatedAt`.
- **Use Cases**: `CreateUrlUseCase` (generates 6-character random code if custom code is not provided) and `RedirectUrlUseCase`.
- **Infrastructure**: `PostgresUrlRepositoryImpl` implementing standard JPA queries against `urls` table. `NoopAnalyticsAdapter` is active (no analytics recorded).

## v1.1.0-redis (Read-through caching)

### Evolution summary
Introduces a Redis caching layer using the Decorator design pattern to cache URL resolution results. This shields the database from repetitive lookup load.

### Key changes
- **No Domain Changes**: Core business logic and use cases remain unchanged.
- **Repository Caching Decorator**: `CachedUrlRepositoryImpl` wraps `PostgresUrlRepositoryImpl`.
  - On write (`save`): Writes to Postgres, then writes to Redis (write-through).
  - On read (`findByShortCode`): Checks Redis first. If a cache miss occurs, retrieves from Postgres and writes back to Redis (cache-aside / read-through).
- **Technology**: Added `spring-boot-starter-data-redis` and `jackson-databind`.

## v1.2.0-sync-analytics (Synchronous tracking)

### Evolution summary
Adds redirection click and visit analytics tracking. The application writes analytics data directly to the database during the redirection request.

### Key changes
- **New Use Case**: `SaveVisitUseCase` (Input Port) coordinates saving redirection analytics metadata (`shortCode`, `ipAddress`, `userAgent`, `clickedAt`).
- **New Output Port**: `VisitDatabasePort` for persisting visits, implemented by `PostgresVisitDatabaseAdapter`.
- **Redirection Logic**: Updates `RedirectUrlUseCase` to trigger visit tracking via `AnalyticsPort.recordVisit(visit)`.
- **Implementation**: `SyncAnalyticsAdapter` (active under `v3` profile) implements `AnalyticsPort` and calls `SaveVisitUseCase` synchronously.

## v2.0.0-kafka-async (Asynchronous event-driven analytics)

### Evolution summary
Transitions analytics logging to an asynchronous event-driven system using Apache Kafka. This removes database write latency from the client's critical path.

### Key changes
- **No Domain Changes**: `RedirectUrlUseCase` still calls `AnalyticsPort.recordVisit(visit)`.
- **Asynchronous Adapter**: Swaps out the sync adapter with `KafkaAnalyticsAdapter`. It publishes visit JSON payloads to the Kafka topic `url-analytics` asynchronously and returns immediately.
- **Driving Adapter**: `KafkaAnalyticsConsumer` listens to the topic, receives the event, and delegates it to `SaveVisitUseCase` in the background.
- **Technology**: Added `spring-kafka` dependency.
