# Benchmark methodology and performance results

This document describes the load testing methodology, parameters, and comparison of the architectural revisions.

## Benchmarking methodology

To ensure scientific accuracy and reliable side-by-side metrics:
1. **Isolated Environments**: Each version runs in its own isolated Docker Compose stack. Databases, Redis caches, and Kafka brokers are entirely separate to avoid cross-contamination of cache states, TCP connections, or disk queues.
2. **Identical Hardware**: You must run all tests on the same physical host machine under similar system conditions.
3. **Warm-up Phase**: Each run includes a 10s warm-up phase (10 concurrent virtual users) to warm up JIT compilation, database connections, and cache pools, followed by a 30s intensive load phase (50 concurrent virtual users).
4. **Isolated HTTP Redirections**: The k6 script configures requests with `redirects: 0` (no redirect follow). This isolates the URL shortener's core API processing latency (Postgres query, Redis query, Kafka publication) without factoring in network overhead and page load times of external target sites (e.g., github.com).

## Performance results

The following results were measured on a local development machine (AMD Ryzen 7 5800X, 12 Core, 32 GB RAM, PCIe Gen4 SSD, running Docker Desktop):

| Version | RPS | Total Requests | Min Latency | Median Latency | P95 Latency | P90 Latency | Error Rate |
|---|---|---|---|---|---|---|---|
| **`v1.0.0-postgres`** | 2,588.45 | 106,665 | 0.51ms | 3.47ms | 10.02ms | 7.59ms | 0.0% |
| **`v1.1.0-redis`** | 2,477.76 | 103,986 | 0.00ms | 3.36ms | 11.58ms | 8.39ms | 0.0% |
| **`v1.2.0-sync-analytics`** | 1,335.97 | 55,607 | 3.68ms | 14.20ms | 39.80ms | 32.07ms | 0.0% |
| **`v2.0.0-kafka-async`** | 1,901.53 | 79,801 | 0.57ms | 6.86ms | 21.22ms | 16.37ms | 0.0% |

### Key takeaways:
- **Local Postgres Speed vs. Redis Caching**: In local single-host testing with a small dataset (100 pre-generated URLs), PostgreSQL reads perform similarly to Redis reads (~2,500 RPS). This occurs because the database indices reside entirely in Postgres's RAM, and the network overhead of routing requests to a separate Dockerized Redis container offsets caching gains.
- **Analytics Write Bottleneck**: Adding synchronous database logging in `v1.2.0-sync-analytics` drops throughput by **~48%** (from 2,588 to 1,335 RPS) and increases P95 latency to **39.8ms** because every redirection request must block to write analytics records synchronously.
- **Asynchronous Restoral**: Switching to event-driven logging in `v2.0.0-kafka-async` restores performance to **1,901.53 RPS** (a **~42%** improvement over synchronous write) and reduces P95 latency to **21.22ms** by offloading analytics writes to the Kafka broker asynchronously.
