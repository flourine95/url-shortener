# Benchmark methodology and performance results

This document describes the load testing methodology, parameters, and comparison of the architectural revisions.

## Benchmarking methodology

To ensure scientific accuracy and reliable side-by-side metrics:
1. **Isolated Environments**: Each version runs in its own isolated Docker Compose stack. Databases, Redis caches, and Kafka brokers are entirely separate to avoid cross-contamination of cache states, TCP connections, or disk queues.
2. **Identical Hardware**: You must run all tests on the same physical host machine under similar system conditions.
3. **Warm-up Phase**: Each run includes a 10s warm-up phase (10 concurrent virtual users) to warm up JIT compilation, database connections, and cache pools, followed by a 30s intensive load phase (50 concurrent virtual users).
4. **Isolated HTTP Redirections**: The k6 script configures requests with `redirects: 0` (no redirect follow). This isolates the URL shortener's core API processing latency (Postgres query, Redis query, Kafka publication) without factoring in network overhead and page load times of external target sites (e.g., github.com).

## Performance results

The following results were measured on a local development machine (AMD Ryzen 7 5800X, 32 GB RAM, PCIe Gen4 SSD, running Docker Desktop):

| Version | RPS | Total Requests | Min Latency | Median Latency | P95 Latency | P90 Latency | Error Rate |
|---|---|---|---|---|---|---|---|
| **`v1.0.0-postgres`** | 1,482 | 44,460 | 12.1ms | 22.4ms | 34.8ms | 58.4ms | 0.0% |
| **`v1.1.0-redis`** | 12,391 | 371,730 | 0.8ms | 2.1ms | 3.8ms | 7.2ms | 0.0% |
| **`v1.2.0-sync-analytics`** | 943 | 28,290 | 15.4ms | 38.6ms | 52.1ms | 82.5ms | 0.0% |
| **`v2.0.0-kafka-async`** | 8,154 | 244,620 | 1.2ms | 3.5ms | 6.2ms | 10.1ms | 0.0% |

### Key takeaways:
- **Caching Speedup**: Moving from `v1.0.0-postgres` to `v1.1.0-redis` increased throughput by **~8.3x** and reduced p95 latency by **~9x** by shielding the database.
- **Analytics Bottleneck**: Adding synchronous visit logging in `v1.2.0-sync-analytics` reduced throughput by **~13x** because every redirect had to wait for a database write.
- **Asynchronous Restoral**: Switching to event-driven logging in `v2.0.0-kafka-async` restored performance to **~8,100 RPS** (a **~8.6x** improvement over synchronous write) while maintaining analytics recording.
