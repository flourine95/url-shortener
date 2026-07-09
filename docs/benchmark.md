# Benchmark methodology and performance results

This document describes the load testing methodology, parameters, and comparison of the architectural revisions.

## Benchmarking methodology

To ensure scientific accuracy and reliable side-by-side metrics:
1. **Isolated Environments**: Each version runs in its own isolated Docker Compose stack. Databases, Redis caches, and Kafka brokers are entirely separate to avoid cross-contamination of cache states, TCP connections, or disk queues.
2. **Identical Hardware**: You must run all tests on the same physical host machine under similar system conditions.
3. **Warm-up Phase**: Each run includes a 10s warm-up phase (10 concurrent virtual users) to warm up JIT compilation, database connections, and cache pools, followed by a 30s intensive load phase (50 concurrent virtual users).
4. **Isolated HTTP Redirections**: The k6 script configures requests with `redirects: 0` (no redirect follow). This isolates the URL shortener's core API processing latency (Postgres query, Redis query, Kafka publication) without factoring in network overhead and page load times of external target sites (e.g., github.com).

## Performance results

Once you execute the benchmark suite using `benchmark/run-benchmarks.ps1`, the script automatically generates and writes the performance comparison table below:

*(Run the benchmark script to populate this table)*
