# URL Shortener Architectural Evolutions

This document details the software development steps and architectural differences across the four tagged milestones.

---

## 📍 v1.0.0-postgres (PostgreSQL Redirection)

### Summary
The initial baseline version of the URL shortener. Redirection metadata is read and written directly to a PostgreSQL database.

### Key Components
- **Domain Entity**: `UrlData` record containing `id`, `originalUrl`, `shortCode`, `createdAt`, `updatedAt`.
- **Use Cases**: `CreateUrlUseCase` (generates 6-character random code if custom code is not provided) and `RedirectUrlUseCase`.
- **Infrastructure**: `PostgresUrlRepositoryImpl` implementing standard JPA queries against `urls` table. `NoopAnalyticsAdapter` is active (no analytics recorded).

---

## 📍 v1.1.0-redis (Transparent Read-Through Caching)

### Summary
Introduces a Redis caching layer using the **Decorator Design Pattern** to cache URL resolution results, shielding the database from repetitive lookup load.

### Key Changes
- **No Domain Changes**: Core business logic and use cases remain 100% unchanged.
- **Repository Caching Decorator**: `CachedUrlRepositoryImpl` wraps `PostgresUrlRepositoryImpl`.
  - On write (`save`): Writes to Postgres, then writes to Redis (write-through).
  - On read (`findByShortCode`): Checks Redis first. If a cache miss occurs, retrieves from Postgres and writes back to Redis (cache-aside / read-through).
- **Technology**: Added `spring-boot-starter-data-redis` and `jackson-databind`.

---

## 📍 v1.2.0-sync-analytics (Synchronous Tracking)

### Summary
Adds redirection click/visit analytics tracking. Analytics data is written directly to the database during the redirection request.

### Key Changes
- **New Use Case**: `SaveVisitUseCase` (Input Port) coordinates saving redirection analytics metadata (`shortCode`, `ipAddress`, `userAgent`, `clickedAt`).
- **New Output Port**: `VisitDatabasePort` for persisting visits, implemented by `PostgresVisitDatabaseAdapter`.
- **Redirection Logic**: `RedirectUrlUseCase` is updated to trigger visit tracking via `AnalyticsPort.recordVisit(visit)`.
- **Implementation**: `SyncAnalyticsAdapter` (active under `v3` profile) implements `AnalyticsPort` and calls `SaveVisitUseCase` synchronously.

---

## 📍 v2.0.0-kafka-async (Asynchronous Event-Driven Analytics)

### Summary
Transitions analytics logging to an asynchronous event-driven system using Apache Kafka to remove database write latency from the client's critical path.

### Key Changes
- **No Domain Changes**: `RedirectUrlUseCase` still calls `AnalyticsPort.recordVisit(visit)`.
- **Asynchronous Adapter**: Swaps out the sync adapter with `KafkaAnalyticsAdapter`. It publishes visit JSON payloads to the Kafka topic `url-analytics` asynchronously and returns immediately.
- **Driving Adapter**: `KafkaAnalyticsConsumer` listens to the topic, receives the event, and delegates it to `SaveVisitUseCase` in the background.
- **Technology**: Added `spring-kafka` dependency.
