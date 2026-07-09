#!/usr/bin/env bash
set -euo pipefail

# Setup directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
BENCHMARK_DIR="$ROOT_DIR/benchmark"

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: 'jq' is required but not installed. Please run: sudo apt install jq" >&2
    exit 1
fi

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo "Error: 'k6' is required but not installed. Please install it first." >&2
    exit 1
fi

# Build Application
echo "============================================="
echo "Building url-shortener-app:latest Docker image"
echo "============================================="
cd "$ROOT_DIR"
./gradlew bootJar --no-daemon -x test
docker build -t url-shortener-app:latest -f docker/Dockerfile .

# Array of stacks: Name,Port,ComposeFile
CONFIGS=(
  "v1-postgres:8081:docker/docker-compose.v1.yml"
  "v2-redis:8082:docker/docker-compose.v2.yml"
  "v3-sync-analytics:8083:docker/docker-compose.v3.yml"
  "v4-kafka-async:8084:docker/docker-compose.v4.yml"
)

results=()

for config in "${CONFIGS[@]}"; do
    IFS=":" read -r name port compose_file <<< "$config"
    
    echo ""
    echo "================================================="
    echo "Starting Stack: $name on Port $port"
    echo "================================================="
    
    # Run docker-compose up
    docker compose -f "$compose_file" up -d
    
    # Wait for service to become healthy
    echo "Waiting for service to become healthy at http://localhost:$port/actuator/health..."
    healthy=false
    for i in {1..30}; do
        if curl -s -f "http://localhost:$port/actuator/health" | grep -q '"status":"UP"'; then
            healthy=true
            break
        fi
        sleep 2
    done
    
    if [ "$healthy" = false ]; then
        echo "Error: Stack $name failed to become healthy. Check docker container logs." >&2
        docker compose -f "$compose_file" logs --tail 50
        docker compose -f "$compose_file" down -v
        exit 1
    fi
    
    echo "Service is healthy! Initializing 10s warm-up and 30s load test..."
    
    # Run k6 load test
    json_summary="$BENCHMARK_DIR/summary-$name.json"
    target_url="http://localhost:$port"
    
    k6 run --summary-export="$json_summary" -e TARGET_URL="$target_url" "$BENCHMARK_DIR/k6-script.js"
    
    # Stop stack and clean volumes
    echo "Stopping Stack: $name..."
    docker compose -f "$compose_file" down -v
    
    # Parse k6 results using jq
    if [ -f "$json_summary" ]; then
        reqs=$(jq -r '.metrics.http_reqs.count' "$json_summary")
        rps=$(jq -r '.metrics.http_reqs.rate | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_reqs.rate' "$json_summary")
        rps=$(printf "%.2f" "$rps")
        
        min=$(jq -r '.metrics.http_req_duration.min | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_req_duration.min' "$json_summary")
        min=$(printf "%.2f" "$min")
        
        med=$(jq -r '.metrics.http_req_duration.med | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_req_duration.med' "$json_summary")
        med=$(printf "%.2f" "$med")
        
        p95=$(jq -r '.metrics.http_req_duration."p(95)" | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_req_duration."p(95)"' "$json_summary")
        p95=$(printf "%.2f" "$p95")
        
        p90=$(jq -r '.metrics.http_req_duration."p(90)" | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_req_duration."p(90)"' "$json_summary")
        p90=$(printf "%.2f" "$p90")
        
        fail_rate=$(jq -r '.metrics.http_req_failed.value | double // 0' "$json_summary" 2>/dev/null || jq -r '.metrics.http_req_failed.value' "$json_summary")
        fail_rate=$(awk "BEGIN {print $fail_rate * 100}")
        fail_rate=$(printf "%.2f" "$fail_rate")
        
        results+=("$name|$rps|$reqs|${min}ms|${med}ms|${p95}ms|${p90}ms|${fail_rate}%")
        echo "Completed $name: RPS = $rps, P95 = ${p95}ms"
    else
        echo "Warning: Summary file not found for $name"
    fi
done

# Output Markdown Table Results
echo ""
echo "============================================="
echo "BENCHMARKING RESULTS COMPARISON"
echo "============================================="

md_header="| Version | RPS | Total Requests | Min Latency | Median Latency | P95 Latency | P90 Latency | Error Rate |"
md_divider="|---|---|---|---|---|---|---|---|"

echo "$md_header"
echo "$md_divider"
for row in "${results[@]}"; do
    IFS="|" read -r v r t min med p95 p90 f <<< "$row"
    echo "| $v | $r | $t | $min | $med | $p95 | $p90 | $f |"
done

# Update docs/benchmark.md if exists
if [ -f "$ROOT_DIR/docs/benchmark.md" ]; then
    # Generate the Markdown table block
    table_content="$md_header\n$md_divider\n"
    for row in "${results[@]}"; do
        IFS="|" read -r v r t min med p95 p90 f <<< "$row"
        table_content+="| **\`$v\`** | $r | $t | $min | $med | $p95 | $p90 | $f |\n"
    done
    
    # We replace the table block in docs/benchmark.md. 
    # To do this safely in bash, we'll write the results to benchmark/results.md
    echo -e "$table_content" > "$BENCHMARK_DIR/results.md"
    echo "Results table saved to benchmark/results.md"
fi
