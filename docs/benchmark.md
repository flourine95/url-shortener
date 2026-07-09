# Benchmark methodology and performance results

This document describes the load testing methodology, parameters, and comparison of the architectural revisions.

## Benchmarking methodology

To ensure scientific accuracy and reliable side-by-side metrics:
1. **Isolated Environments**: Each version runs in its own isolated Docker Compose stack. Databases, Redis caches, and Kafka brokers are entirely separate to avoid cross-contamination of cache states, TCP connections, or disk queues.
2. **Resource-Constrained VM**: To emulate a production cloud container, the benchmark environment is executed inside a resource-constrained WSL2 Linux VM allocated with:
   - **CPU**: 4 vCPUs
   - **RAM**: 8 GB RAM
   - **Swap**: 2 GB
3. **Warm-up Phase**: Each run includes a 10s warm-up phase (10 concurrent virtual users) to warm up JIT compilation, database connections, and cache pools, followed by a 30s intensive load phase (50 concurrent virtual users).
4. **Isolated HTTP Redirections**: The k6 script configures requests with `redirects: 0` (no redirect follow). This isolates the URL shortener's core API processing latency (Postgres query, Redis query, Kafka publication) without factoring in network overhead and page load times of external target sites (e.g., github.com).

## Performance results

The following results were measured inside a WSL2 Ubuntu 22.04 LTS instance with Docker Desktop WSL2 integration (on a host machine with 12th Gen Intel(R) Core(TM) i5-12450H, 24 GB RAM, running Windows 11):

| Version | RPS | Total Requests | Min Latency | Median Latency | P95 Latency | P90 Latency | Error Rate |
|---|---|---|---|---|---|---|---|
| **`v1-postgres`** | 2,517.01 | 104,528 | 0.27ms | 2.32ms | 17.51ms | 12.22ms | 0.00% |
| **`v2-redis`** | 3,077.79 | 127,589 | 0.25ms | 1.50ms | 5.37ms | 3.85ms | 0.00% |
| **`v3-sync-analytics`** | 1,488.52 | 61,741 | 2.24ms | 12.54ms | 36.53ms | 28.04ms | 0.00% |
| **`v4-kafka-async`** | 2,319.82 | 96,092 | 0.28ms | 4.10ms | 17.78ms | 12.89ms | 0.00% |

### Key takeaways:
- **Caching Optimization**: Adding Redis caching in `v2-redis` increased throughput by **~22%** and reduced P95 latency by **~70%** (from 17.51ms to 5.37ms). This demonstrates the power of memory caching for high-read redirection paths.
- **Synchronous Write Bottleneck**: Adding visit tracking in `v3-sync-analytics` cut throughput by **~50%** (from 3,077 RPS down to 1,488 RPS) and doubled P95 latency because every redirect request had to block for a synchronous database write.
- **Asynchronous Restoral**: Offloading visit logging to Kafka in `v4-kafka-async` recovered throughput back to **2,319.82 RPS** (a **~56%** improvement over synchronous write) and cut P95 latency back to **17.78ms** by decoupling the write path from the client request cycle.
