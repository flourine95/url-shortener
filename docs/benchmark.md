# Benchmark methodology and performance results

Load testing methodology, parameters, and a side-by-side comparison of four architectural revisions of the URL shortener.

## Benchmarking methodology

Four constraints keep the results comparable across versions:

1. **Isolated environments**: Each version runs in its own Docker Compose stack. Databases, Redis caches, and Kafka brokers are separate to prevent cross-contamination of cache states, TCP connections, or disk queues
2. **Resource-constrained VM**: The benchmark runs inside a WSL2 Linux VM allocated 4 vCPUs, 8 GB memory, and 2 GB swap. This keeps the test closer to a constrained deployment and avoids relying on the full host capacity
3. **Warm-up phase**: Each run starts with a 10 s warm-up (10 concurrent virtual users) to prime JIT compilation, database connections, and cache pools. A 30 s load phase (50 concurrent virtual users) follows
4. **No redirect follow**: The k6 script sets `redirects: 0` to measure only the shortener's core processing latency, without adding network round-trips to external target sites

## Performance results

Measured inside WSL2 Ubuntu 22.04 LTS with Docker Desktop WSL2 integration. Host machine: 12th Gen Intel Core i5-12450H, 24 GB RAM, Windows 11.

| Version | RPS | Total requests | Min latency | Median latency | P95 latency | P90 latency | Error rate |
|---|---|---|---|---|---|---|---|
| `v1-postgres` | 2,517.01 | 104,528 | 0.27 ms | 2.32 ms | 17.51 ms | 12.22 ms | 0.00% |
| `v2-redis` | 3,077.79 | 127,589 | 0.25 ms | 1.50 ms | 5.37 ms | 3.85 ms | 0.00% |
| `v3-sync-analytics` | 1,488.52 | 61,741 | 2.24 ms | 12.54 ms | 36.53 ms | 28.04 ms | 0.00% |
| `v4-kafka-async` | 2,319.82 | 96,092 | 0.28 ms | 4.10 ms | 17.78 ms | 12.89 ms | 0.00% |

### How each version affects throughput and latency

- **Caching optimization**: Adding Redis caching in `v2-redis` raised RPS from 2,517 to 3,077 (22% increase) and dropped P95 latency from 17.51 ms to 5.37 ms (69% reduction). Redis serves cached URL lookups in under 2 ms, bypassing Postgres entirely on cache hits
- **Synchronous write bottleneck**: Adding synchronous analytics in `v3-sync-analytics` dropped RPS from 3,077 to 1,488 (52% decrease) and pushed P95 latency to 36.53 ms. Every redirect request blocks until the visit record commits to Postgres
- **Asynchronous analytics recovery**: Offloading analytics to Kafka in `v4-kafka-async` raised RPS back to 2,319 (56% improvement over `v3`) and cut P95 latency to 17.78 ms. The redirect handler publishes a JSON event to Kafka and returns HTTP 302 immediately. A background consumer persists the visit record to Postgres without blocking the client

## Testing a specific version manually

To spin up a single stack and explore the APIs:

1. Start the stack (version 4 in this example):
   ```bash
   docker compose -f docker/docker-compose.v4.yml up -d
   ```
2. Shorten a URL:
   ```bash
   curl -X POST http://localhost:8084/api/urls \
     -H "Content-Type: application/json" \
     -d '{"originalUrl": "https://github.com", "customCode": "myrepo"}'
   ```
3. Trigger redirection:
   ```bash
   curl -i http://localhost:8084/myrepo
   ```
4. Tear down the stack when done:
   ```bash
   docker compose -f docker/docker-compose.v4.yml down -v
   ```
