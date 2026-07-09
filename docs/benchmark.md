# Benchmark Methodology & Performance Results

This document describes the load testing methodology, parameters, and comparison of the architectural revisions.

---

## 🔬 Benchmarking Methodology

To ensure scientific accuracy and reliable side-by-side metrics:
1. **Isolated Environments**: Each version runs in its own isolated Docker Compose stack. Databases, Redis caches, and Kafka brokers are entirely separate to avoid cross-contamination of cache states, TCP connections, or disk queues.
2. **Identical Hardware**: All tests are run on the same physical host machine under similar system conditions.
3. **Warm-up Phase**: Each run includes a 10-second warm-up phase (10 concurrent virtual users) to warm up JIT compilation, database connections, and cache pools, followed by a 30-second intensive load phase (50 concurrent virtual users).
4. **Isolated HTTP Redirections**: k6 requests are configured with `redirects: 0` (no redirect follow). This isolates the URL shortener's core API processing latency (Postgres query, Redis query, Kafka publication) without factoring in network overhead and page load times of external target sites (e.g., github.com).

---

## 📈 Performance Results

Once you execute the benchmark suite using `benchmark/run-benchmarks.ps1`, the performance comparison table will be automatically generated and written below:

*(Run the benchmark script to populate this table)*
