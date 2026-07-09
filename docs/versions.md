# URL shortener architectural evolutions

Four tagged milestones track how the URL shortener evolved from a Postgres-only redirect service to a cached, event-driven system with asynchronous analytics.

## v1.0.0-postgres: PostgreSQL redirection

The baseline version. Redirect lookups query Postgres directly. No analytics are recorded.

- **URL repository**: `PostgresUrlRepositoryImpl` runs standard JPA queries against the `urls` table
- **Analytics**: `NoopAnalyticsAdapter` (disabled)
- **Core components**: `UrlData` domain record, `CreateUrlUseCase`, `RedirectUrlUseCase`

## v1.1.0-redis: read-through caching

Adds a Redis cache layer using the Decorator pattern to shield Postgres from repeated read queries.

- **URL repository**: `CachedUrlRepositoryImpl` wraps `PostgresUrlRepositoryImpl`
  - On write: writes to Postgres, then writes to Redis (write-through)
  - On read: checks Redis first. On cache miss, reads from Postgres and stores the result in Redis (read-through)
- **Analytics**: `NoopAnalyticsAdapter` (disabled)
- **Added dependency**: `spring-boot-starter-data-redis`, `jackson-databind`

## v1.2.0-sync-analytics: synchronous tracking

Records visit events on every redirect. The write happens synchronously inside the redirect request, blocking the HTTP response until Postgres commits.

- **URL repository**: `CachedUrlRepositoryImpl` (Redis + DB)
- **Analytics**: `SyncAnalyticsAdapter` implements `AnalyticsPort`. It delegates to `SaveVisitUseCase`, which writes visit metadata (`shortCode`, `ipAddress`, `userAgent`, `clickedAt`) to Postgres
- **Added components**: `SaveVisitUseCase` (input port), `VisitDatabasePort` (output port)

## v2.0.0-kafka-async: asynchronous event-driven analytics

Replaces synchronous database writes with Kafka event publishing. The redirect handler returns HTTP 302 immediately. A background consumer persists visit records.

- **URL repository**: `CachedUrlRepositoryImpl` (Redis + DB)
- **Analytics**: `KafkaAnalyticsAdapter` implements `AnalyticsPort`. It publishes visit JSON payloads to the `url-analytics` Kafka topic and returns without waiting for persistence
- **Consumer**: `KafkaAnalyticsConsumer` listens to the topic and invokes `SaveVisitUseCase` in the background
- **Added dependency**: `spring-boot-starter-kafka`
