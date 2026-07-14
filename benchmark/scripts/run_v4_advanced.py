import subprocess
import time
import csv
import threading
import sys
import os

COMPOSE_FILE = "docker/docker-compose.v4.yml"
MIX_SCRIPT = "benchmark/k6/k6-script-mix-v4.js"
TARGET_URL = "http://localhost:8084"

containers = {
    "app": "url-shortener-app-v4",
    "db": "url-shortener-db-v4",
    "redis": "url-shortener-redis-v4",
    "kafka": "url-shortener-kafka-v4"
}

stop_collecting = False
stats_data = []

def parse_cpu(cpu_str):
    try:
        return float(cpu_str.replace('%', '').strip())
    except:
        return 0.0

def parse_mem(mem_str):
    try:
        usage = mem_str.split('/')[0].strip().lower()
        if 'gib' in usage:
            return float(usage.replace('gib', '').strip()) * 1024
        elif 'mib' in usage:
            return float(usage.replace('mib', '').strip())
        elif 'kib' in usage:
            return float(usage.replace('kib', '').strip()) / 1024
        elif 'b' in usage:
            return float(usage.replace('b', '').strip()) / (1024 * 1024)
        return float(usage)
    except:
        return 0.0

def collect_stats():
    global stop_collecting, stats_data
    start_time = time.time()
    cmd = ["docker", "stats", "--no-stream", "--format", "{{.Name}}|{{.CPUPerc}}|{{.MemUsage}}"]
    
    while not stop_collecting:
        try:
            result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
            lines = result.stdout.strip().split('\n')
            
            current_stats = {
                "elapsed_sec": round(time.time() - start_time, 1),
                "app_cpu": 0.0, "app_mem_mb": 0.0,
                "db_cpu": 0.0, "db_mem_mb": 0.0,
                "redis_cpu": 0.0, "redis_mem_mb": 0.0,
                "kafka_cpu": 0.0, "kafka_mem_mb": 0.0,
            }
            
            for line in lines:
                if not line: continue
                parts = line.split('|')
                if len(parts) < 3: continue
                name, cpu, mem = parts[0], parts[1], parts[2]
                
                for key, container_name in containers.items():
                    if container_name in name:
                        current_stats[f"{key}_cpu"] = parse_cpu(cpu)
                        current_stats[f"{key}_mem_mb"] = parse_mem(mem)
            
            stats_data.append(current_stats)
        except Exception:
            pass
        time.sleep(5)  # Collect every 5 seconds for longer runs

def check_health():
    import urllib.request
    import json
    try:
        with urllib.request.urlopen("http://localhost:8084/actuator/health", timeout=2) as response:
            if response.status == 200:
                data = json.loads(response.read().decode())
                return data.get("status") == "UP"
    except Exception:
        pass
    return False

def run_test(vus, duration, collect_resources=False, csv_name="resource_usage.csv"):
    global stop_collecting, stats_data
    stop_collecting = False
    stats_data = []
    
    # Reset stack
    print(f"\n=== Cleaning up previous Docker containers ===")
    subprocess.run(["docker", "compose", "-f", COMPOSE_FILE, "down", "-v"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    
    print(f"\n=== Launching Stack v4 ===")
    subprocess.run(["docker", "compose", "-f", COMPOSE_FILE, "up", "-d"], check=True)
    
    print("Waiting for application to boot (checking /actuator/health)...")
    healthy = False
    for _ in range(30):
        if check_health():
            healthy = True
            break
        time.sleep(1)
        sys.stdout.write(".")
        sys.stdout.flush()
        
    if not healthy:
        print("\nError: App failed to boot. Check logs.")
        subprocess.run(["docker", "compose", "-f", COMPOSE_FILE, "down", "-v"])
        return
        
    print("\nApplication is ready!")
    
    stats_thread = None
    if collect_resources:
        print("Starting resource usage collection thread (polling every 5s)...")
        stats_thread = threading.Thread(target=collect_stats)
        stats_thread.daemon = True
        stats_thread.start()
        
    print(f"\n=== Executing k6 benchmark: {vus} VUs, Duration: {duration} ===")
    env = os.environ.copy()
    env["TARGET_URL"] = TARGET_URL
    
    cmd = [
        "k6", "run",
        "--vus", str(vus),
        "--duration", duration,
        MIX_SCRIPT
    ]
    
    try:
        subprocess.run(cmd, env=env, check=True)
    except subprocess.CalledProcessError:
        print("Error: k6 run encountered failures.")
    finally:
        if collect_resources and stats_thread:
            stop_collecting = True
            stats_thread.join()
            
        print("\n=== Tearing down Docker Compose Stack ===")
        subprocess.run(["docker", "compose", "-f", COMPOSE_FILE, "down", "-v"])
        
        if collect_resources and stats_data:
            csv_path = os.path.join("benchmark", "reports", csv_name)
            print(f"\nSaving resource usage statistics to {csv_path}...")
            with open(csv_path, 'w', newline='') as f:
                writer = csv.DictWriter(f, fieldnames=stats_data[0].keys())
                writer.writeheader()
                writer.writerows(stats_data)
            print("Done! You can plot charts with this data.")

def main():
    print("==================================================")
    print("      V4 ADVANCED PERFORMANCE BENCHMARK RUNNER    ")
    print("==================================================")
    print("Choose the test scenario to run:")
    print("  [1] 10-Minute Load Test (500 VUs, Mix 90% Read / 10% Write)")
    print("  [2] 1-Hour Soak Test (200 VUs, Mix 90% Read / 10% Write, Collect Stats)")
    print("  [3] Custom Run (Choose VUs, Duration, and Stats Collection)")
    print("  [q] Quit")
    
    choice = input("\nEnter choice (1/2/3/q): ").strip().lower()
    
    if choice == '1':
        run_test(vus=500, duration="10m", collect_resources=False)
    elif choice == '2':
        run_test(vus=200, duration="1h", collect_resources=True, csv_name="soak_resource_usage.csv")
    elif choice == '3':
        try:
            vus = int(input("Enter number of Virtual Users (VUs): ").strip())
            duration = input("Enter duration (e.g. 5m, 30m, 2h): ").strip()
            collect = input("Collect resource stats to CSV? (y/n): ").strip().lower() == 'y'
            csv_name = "custom_resource_usage.csv"
            if collect:
                csv_name = input("Enter output CSV filename (default: custom_resource_usage.csv): ").strip()
                if not csv_name: csv_name = "custom_resource_usage.csv"
            run_test(vus=vus, duration=duration, collect_resources=collect, csv_name=csv_name)
        except ValueError:
            print("Invalid VUs input. Must be an integer.")
    elif choice == 'q':
        print("Exiting.")
    else:
        print("Invalid choice.")

if __name__ == "__main__":
    main()
