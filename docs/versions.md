# URL shortener architectural evolutions

This document details the software development steps and architectural differences across the four tagged milestones.

## v1.0.0-postgres (PostgreSQL redirection)

- **Summary**: Queries and records redirection metadata directly in a PostgreSQL database.
- **URL Repository**: `PostgresUrlRepositoryImpl` implementing standard JPA queries against the `urls` table.
- **Analytics**: `NoopAnalyticsAdapter` (no analytics recorded).
- **Core Components**: `UrlData` domain record and standard `CreateUrlUseCase` / `RedirectUrlUseCase`.

## v1.1.0-redis (Read-through caching)

- **Summary**: Adds read-through caching with Redis.
- **URL Repository**: `CachedUrlRepositoryImpl` wraps `PostgresUrlRepositoryImpl` using the Decorator design pattern.
  - On write: Writes to Postgres, then writes to Redis (write-through).
  - On read: Checks Redis first. On cache miss, retrieves from Postgres and writes back to Redis (cache-aside / read-through).
- **Analytics**: `NoopAnalyticsAdapter` (no analytics recorded).
- **Technology**: Added `spring-boot-starter-data-redis` and `jackson-databind`.

## v1.2.0-sync-analytics (Synchronous tracking)

- **Summary**: Records visit events synchronously.
- **URL Repository**: `CachedUrlRepositoryImpl` (Redis + DB).
- **Analytics**: `SyncAnalyticsAdapter` implements `AnalyticsPort` and delegates calls to `SaveVisitUseCase` synchronously to write visit metadata (`shortCode`, `ipAddress`, `userAgent`, `clickedAt`) directly to PostgreSQL.
- **Core Components**: Introduced `SaveVisitUseCase` (Input Port) and `VisitDatabasePort` (Output Port) to decouple DB writes.

## v2.0.0-kafka-async (Asynchronous event-driven analytics)

- **Summary**: Publishes visit events to Kafka and persists them asynchronously.
- **URL Repository**: `CachedUrlRepositoryImpl` (Redis + DB).
- **Analytics**: `KafkaAnalyticsAdapter` implements `AnalyticsPort` and publishes visit JSON payloads to the Kafka topic `url-analytics` asynchronously, returning immediately.
- **Consumer**: `KafkaAnalyticsConsumer` listens to the topic and invokes `SaveVisitUseCase` in the background.
- **Technology**: Added `spring-kafka` dependency.
