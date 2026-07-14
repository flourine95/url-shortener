import os
import json
import pandas as pd
import matplotlib.pyplot as plt

# Output directory for images
IMAGE_DIR = "docs/images"
os.makedirs(IMAGE_DIR, exist_ok=True)

# File paths for summaries
versions = {
    "v1-postgres": "benchmark/data/summary-v1-postgres.json",
    "v2-redis": "benchmark/data/summary-v2-redis.json",
    "v3-sync-analytics": "benchmark/data/summary-v3-sync-analytics.json",
    "v4-kafka-async": "benchmark/data/summary-v4-kafka-async.json"
}

# 1. Parse JSON files for Throughput and Latency
throughput_data = {}
latency_data = []

for name, path in versions.items():
    if os.path.exists(path):
        with open(path, "r") as f:
            data = json.load(f)
            # Extract http_reqs rate
            rps = data["metrics"]["http_reqs"]["rate"]
            throughput_data[name] = rps
            
            # Extract http_req_duration median and p(95)
            med = data["metrics"]["http_req_duration"]["med"]
            p95 = data["metrics"]["http_req_duration"]["p(95)"]
            latency_data.append({
                "Version": name,
                "Median": med,
                "P95": p95
            })
    else:
        print(f"Warning: {path} not found.")

# Style settings
plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "default")
plt.rcParams.update({
    "font.family": "sans-serif",
    "font.size": 11,
    "figure.titlesize": 14,
    "axes.labelsize": 12,
    "axes.titlesize": 13,
    "xtick.labelsize": 10,
    "ytick.labelsize": 10
})

colors = ["#3b82f6", "#10b981", "#ef4444", "#8b5cf6"]

# --- Plot 1: Throughput (RPS) ---
if throughput_data:
    fig, ax = plt.subplots(figsize=(8, 4.5))
    names = list(throughput_data.keys())
    rates = list(throughput_data.values())
    
    bars = ax.barh(names, rates, color=colors, height=0.55, edgecolor="none")
    ax.invert_yaxis()  # top-down order
    
    # Add values at the end of each bar
    for bar in bars:
        width = bar.get_width()
        ax.text(width + 50, bar.get_y() + bar.get_height()/2, f"{width:,.2f} RPS", 
                va="center", ha="left", fontweight="bold", color="#1e293b")
        
    ax.set_title("Throughput Comparison (Requests per Second)", pad=15, fontweight="bold", color="#0f172a")
    ax.set_xlabel("Throughput (RPS)")
    ax.set_xlim(0, max(rates) * 1.2)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.spines["left"].set_color("#cbd5e1")
    ax.spines["bottom"].set_color("#cbd5e1")
    
    plt.tight_layout()
    plt.savefig(os.path.join(IMAGE_DIR, "throughput_comparison.png"), dpi=150, bbox_inches="tight")
    plt.close()
    print("Generated throughput_comparison.png")

# --- Plot 2: Latency (Median vs P95) ---
if latency_data:
    df_lat = pd.DataFrame(latency_data)
    fig, ax = plt.subplots(figsize=(8, 5.5))
    
    y = range(len(df_lat))
    height = 0.3
    
    rects1 = ax.barh([pos - height/2 for pos in y], df_lat["Median"], height, label="Median Latency", color="#3b82f6")
    rects2 = ax.barh([pos + height/2 for pos in y], df_lat["P95"], height, label="P95 Latency", color="#ef4444")
    
    ax.set_yticks(y)
    ax.set_yticklabels(df_lat["Version"])
    ax.invert_yaxis()
    
    # Add labels
    for rect in rects1:
        width = rect.get_width()
        ax.text(width + 2, rect.get_y() + rect.get_height()/2, f"{width:.2f}ms", 
                va="center", ha="left", color="#3b82f6", fontsize=9)
        
    for rect in rects2:
        width = rect.get_width()
        ax.text(width + 2, rect.get_y() + rect.get_height()/2, f"{width:.2f}ms", 
                va="center", ha="left", color="#ef4444", fontsize=9, fontweight="bold")
        
    ax.set_title("Latency Comparison (Median vs P95)", pad=15, fontweight="bold", color="#0f172a")
    ax.set_xlabel("Latency (ms)")
    ax.set_xlim(0, max(df_lat["P95"]) * 1.15)
    ax.legend(frameon=True, facecolor="white", edgecolor="none")
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.spines["left"].set_color("#cbd5e1")
    ax.spines["bottom"].set_color("#cbd5e1")
    
    plt.tight_layout()
    plt.savefig(os.path.join(IMAGE_DIR, "latency_comparison.png"), dpi=150, bbox_inches="tight")
    plt.close()
    print("Generated latency_comparison.png")

# --- Plot 3: Resource Stability (Memory Usage Timeline) ---
soak_path = "benchmark/reports/soak_resource_usage.csv"
if os.path.exists(soak_path):
    df_soak = pd.read_csv(soak_path)
    
    # Convert seconds to minutes
    df_soak["elapsed_min"] = df_soak["elapsed_sec"] / 60.0
    
    fig, ax = plt.subplots(figsize=(9, 5.5))
    
    ax.plot(df_soak["elapsed_min"], df_soak["app_mem_mb"], label="App (Spring Boot)", color="#3b82f6", linewidth=2.5)
    ax.plot(df_soak["elapsed_min"], df_soak["kafka_mem_mb"], label="Kafka", color="#8b5cf6", linewidth=2.5)
    ax.plot(df_soak["elapsed_min"], df_soak["db_mem_mb"], label="PostgreSQL", color="#10b981", linewidth=2.5)
    ax.plot(df_soak["elapsed_min"], df_soak["redis_mem_mb"], label="Redis", color="#f97316", linewidth=2.5)
    
    ax.set_title("Memory Stability Timeline (1-Hour Endurance Test)", pad=15, fontweight="bold", color="#0f172a")
    ax.set_xlabel("Elapsed Time (minutes)")
    ax.set_ylabel("Memory Consumption (MB)")
    ax.set_ylim(0, max(df_soak["app_mem_mb"].max(), df_soak["kafka_mem_mb"].max()) * 1.15)
    ax.set_xlim(0, df_soak["elapsed_min"].max())
    ax.legend(frameon=True, facecolor="white", edgecolor="none", loc="upper left")
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.spines["left"].set_color("#cbd5e1")
    ax.spines["bottom"].set_color("#cbd5e1")
    
    plt.tight_layout()
    plt.savefig(os.path.join(IMAGE_DIR, "resource_stability.png"), dpi=150, bbox_inches="tight")
    plt.close()
    print("Generated resource_stability.png")
else:
    print(f"Warning: {soak_path} not found.")
