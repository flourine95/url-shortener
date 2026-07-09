# PowerShell script to orchestrate isolated benchmarks for v1, v2, v3, and v4 URL Shortener stacks

$ErrorActionPreference = "Stop"

# Paths
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$RootDir = Resolve-Path "$PSScriptRoot/.."
$DockerDir = "$RootDir/docker"
$BenchmarkDir = "$RootDir/benchmark"

# Versions Configuration
$versions = @(
    [PSCustomObject]@{ Name = "v1-postgres";        ComposeFile = "$DockerDir/docker-compose.v1.yml"; Port = 8081 },
    [PSCustomObject]@{ Name = "v2-redis";           ComposeFile = "$DockerDir/docker-compose.v2.yml"; Port = 8082 },
    [PSCustomObject]@{ Name = "v3-sync-analytics";  ComposeFile = "$DockerDir/docker-compose.v3.yml"; Port = 8083 },
    [PSCustomObject]@{ Name = "v4-kafka-async";     ComposeFile = "$DockerDir/docker-compose.v4.yml"; Port = 8084 }
)

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Building url-shortener-app:latest Docker image" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Set-Location $RootDir
& ./gradlew bootJar --no-daemon -x test
& docker build -t url-shortener-app:latest -f docker/Dockerfile .

$results = @()

foreach ($v in $versions) {
    Write-Host ""
    Write-Host "=================================================" -ForegroundColor Green
    Write-Host "Starting Stack: $($v.Name) on Port $($v.Port)" -ForegroundColor Green
    Write-Host "=================================================" -ForegroundColor Green

    # Cleanup any leftovers
    & docker compose -f $v.ComposeFile down -v --remove-orphans

    # Boot stack
    & docker compose -f $v.ComposeFile up -d

    # Wait for healthcheck (check actuator health endpoint)
    $url = "http://localhost:$($v.Port)/actuator/health"
    $maxAttempts = 30
    $attempt = 1
    $healthy = $false

    Write-Host "Waiting for service to become healthy at $url..." -ForegroundColor Yellow
    while ($attempt -le $maxAttempts) {
        try {
            $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 2
            if ($response.status -eq "UP") {
                $healthy = $true
                break
            }
        } catch {
            # Ignore connection errors during startup
        }
        Start-Sleep -Seconds 2
        $attempt++
    }

    if (-not $healthy) {
        Write-Error "Stack $($v.Name) failed to become healthy. Check docker container logs."
    }

    Write-Host "Service is healthy! Initializing 10s warm-up and 30s load test..." -ForegroundColor Green
    
    # Run k6 load test
    $jsonSummary = "$BenchmarkDir/summary-$($v.Name).json"
    $targetUrl = "http://localhost:$($v.Port)"
    
    & k6 run --summary-export=$jsonSummary -e TARGET_URL=$targetUrl "$BenchmarkDir/k6-script.js"

    # Stop stack and clean volumes
    Write-Host "Stopping Stack: $($v.Name)..." -ForegroundColor Yellow
    & docker compose -f $v.ComposeFile down -v

    # Parse k6 results
    if (Test-Path $jsonSummary) {
        $summary = Get-Content $jsonSummary | ConvertFrom-Json
        
        $reqs = $summary.metrics.http_reqs.values.count
        $rps = [Math]::Round($summary.metrics.http_reqs.values.rate, 2)
        $min = [Math]::Round($summary.metrics.http_req_duration.values.min, 2)
        $med = [Math]::Round($summary.metrics.http_req_duration.values.med, 2)
        $p95 = [Math]::Round($summary.metrics.http_req_duration.values."p(95)", 2)
        $p99 = [Math]::Round($summary.metrics.http_req_duration.values."p(99)", 2)
        $failRate = [Math]::Round($summary.metrics.http_req_failed.values.rate * 100, 2)

        $results += [PSCustomObject]@{
            Version = $v.Name
            RPS = $rps
            TotalReqs = $reqs
            Min = "$($min)ms"
            Median = "$($med)ms"
            P95 = "$($p95)ms"
            P99 = "$($p99)ms"
            ErrorRate = "$($failRate)%"
        }
        Write-Host "Completed $($v.Name): RPS = $rps, P95 = $($p95)ms" -ForegroundColor Green
    } else {
        Write-Host "Warning: Summary file not found for $($v.Name)" -ForegroundColor Red
    }
}

# Output Markdown Table Results
Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "BENCHMARKING RESULTS COMPARISON" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

$mdHeader = "| Version | RPS | Total Requests | Min Latency | Median Latency | P95 Latency | P99 Latency | Error Rate |"
$mdDivider = "|---|---|---|---|---|---|---|---|--"
$mdRows = @()

foreach ($r in $results) {
    $mdRows += "| $($r.Version) | $($r.RPS) | $($r.TotalReqs) | $($r.Min) | $($r.Median) | $($r.P95) | $($r.P99) | $($r.ErrorRate) |"
}

$mdContent = @($mdHeader, $mdDivider) + $mdRows | Out-String

Write-Output $mdContent
$mdContent | Out-File -FilePath "$BenchmarkDir/results.md" -Encoding utf8

Write-Host "Results saved to benchmark/results.md" -ForegroundColor Green
