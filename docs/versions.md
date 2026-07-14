
# How did the architecture evolve across milestones?

This page traces the evolution of the URL shortener architecture from a simple database-backed implementation to a high-throughput, event-driven system.

## v1.0.0-postgres: PostgreSQL redirection

The baseline architecture queries PostgreSQL directly for every redirect request. This setup serves as the performance baseline.

- **URL Repository**: `PostgresUrlRepositoryImpl` queries the database table using Spring Data JPA.
- **Analytics**: The configuration disables analytics tracking using `NoopAnalyticsAdapter`.
- **Core Components**: The domain defines the `UrlData` record and use case ports `CreateUrlUseCase` and `RedirectUrlUseCase`.

### Advantages and disadvantages

- **Pros**: The system uses a simple design with minimal moving parts.
- **Cons**: The database server handles every read query, which limits throughput under high concurrent traffic.

## v1.1.0-redis: Read-through caching

To shield the primary database from redundant read traffic, the architecture adds a Redis cache using the Decorator pattern.

- **URL Repository**: `CachedUrlRepositoryImpl` decorates the PostgreSQL implementation.
- **Write Path**: Persists data to PostgreSQL and updates the Redis cache state.
- **Read Path**: Queries Redis first, falling back to PostgreSQL on cache misses and updating Redis.
- **Dependencies**: Adds `spring-boot-starter-data-redis` and `jackson-databind`.

### Advantages and disadvantages

- **Pros**: Throughput increases by serving hot redirect paths directly from memory.
- **Cons**: High write loads still block on database writes, and cache sync lags can occur.

## v1.2.0-sync-analytics: Synchronous database writes

This milestone records analytics metrics for every redirect event, writing visit metadata directly to PostgreSQL before returning the HTTP response.

- **URL Repository**: The cache-aside repository handles redirect lookups.
- **Analytics Adapter**: `SyncAnalyticsAdapter` implements `AnalyticsPort` and invokes `SaveVisitUseCase` synchronously.
- **Components**: The system adds `SaveVisitUseCase` and `VisitDatabasePort` to persist visit logs.

### Advantages and disadvantages

- **Pros**: Provides transactional guarantees that ensure every click records to PostgreSQL.
- **Cons**: Redirect speed drops because HTTP request threads block until PostgreSQL finishes disk commits.

## v2.0.0-kafka-async: Asynchronous event-driven analytics

To decouple redirect execution from analytics persistence, the system publishes visit events to Apache Kafka.

- **URL Repository**: Serves redirect queries from Redis.
- **Analytics Adapter**: `KafkaAnalyticsAdapter` publishes visit DTOs as JSON strings to the `url-analytics` topic.
- **Consumer**: `KafkaAnalyticsConsumer` listens to the topic and writes visit logs to PostgreSQL in the background.
- **Dependencies**: Adds `spring-boot-starter-kafka`.

### Advantages and disadvantages

- **Pros**: The redirect path returns HTTP 302 location headers immediately, recovering the high throughput of the cached read path.
- **Cons**: Adds operational complexity with the introduction of a Kafka message broker.

## Future Roadmaps (v3.0.0 / v5 ideas)

To scale the system further, the following design improvements are proposed:

- **Horizontal Scaling & Database Replication**: Introduce PostgreSQL read replicas to scale read query capacity, and run multiple stateless Spring Boot instances behind a Load Balancer (Nginx/HAProxy) to distribute HTTP traffic.
- **API Rate Limiting**: Implement API rate limiting (using Redis Token Bucket algorithm or Spring Cloud Gateway) to protect the write path and prevent DDoS/abuse on short code generation.
- **Custom Short Code Aliases**: Further extend custom short code validation and custom expiration policies (fully supported at the domain layer, can be exposed with dynamic validation).
- **High-Availability Kafka & Partitioning**: Partition the `url-analytics` topic to distribute message processing across multiple consumer instances, ensuring parallelized click logging.
