import subprocess
import re
import time


def _run_adb(command):
    full_cmd = f"adb -s emulator-5554 {command}"
    result = subprocess.run(full_cmd, capture_output=True, text=True, shell=True)
    return result.stdout.strip()


def get_memory_pss(package_name):
    output = _run_adb(f"shell dumpsys meminfo {package_name}")
    for line in output.split("\n"):
        line = line.strip()
        if line.startswith("TOTAL"):
            parts = re.split(r"\s+", line)
            if len(parts) >= 2:
                return int(parts[1])
    return None


def get_cpu_usage(package_name):
    output = _run_adb(f"shell dumpsys cpuinfo | findstr {package_name}")
    for line in output.split("\n"):
        if package_name in line:
            match = re.search(r"(\d+(?:\.\d+)?)%", line)
            if match:
                return float(match.group(1))
    return None


_START_TIME_PATTERN = re.compile(r"(?:ThisTime|TotalTime):\s*(\d+)")


def measure_cold_start_time(package_name, main_activity):
    _run_adb(f"shell am force-stop {package_name}")
    time.sleep(1.5)
    output = _run_adb(f"shell am start -W -n {package_name}/{main_activity}")
    match = _START_TIME_PATTERN.search(output)
    if match:
        return int(match.group(1))
    return None


def measure_warm_start_time(package_name, main_activity):
    output = _run_adb(f"shell am start -W -n {package_name}/{main_activity}")
    match = _START_TIME_PATTERN.search(output)
    if match:
        return int(match.group(1))
    return None


def get_gfx_histogram(package_name):
    output = _run_adb(f"shell dumpsys gfxinfo {package_name}")
    match = re.search(r"HISTOGRAM:\s*(.*?)(?:\n|$)", output, re.DOTALL)
    if not match:
        return []
    hist_str = match.group(1).strip()
    frames = []
    for part in hist_str.split():
        if "ms=" in part:
            try:
                bucket = part.split("=")
                ms = int(bucket[0].rstrip("ms"))
                count = int(bucket[1])
                frames.extend([ms] * count)
            except (ValueError, IndexError):
                pass
    return frames


def analyze_frames(frames):
    if not frames:
        return {
            "total_frames": 0,
            "avg_frame_time_ms": 0,
            "max_frame_time_ms": 0,
            "jank_count": 0,
            "estimated_fps": 0,
        }
    jank_count = sum(1 for f in frames if f > 16)
    avg_frame_time = sum(frames) / len(frames)
    max_frame_time = max(frames)
    smooth_frames = [f for f in frames if f <= 16]
    fps = len(smooth_frames) / (sum(smooth_frames) / 1000) if smooth_frames else 0
    return {
        "total_frames": len(frames),
        "avg_frame_time_ms": round(avg_frame_time, 2),
        "max_frame_time_ms": max_frame_time,
        "jank_count": jank_count,
        "estimated_fps": round(min(fps, 60)),
    }


def get_gfx_summary(package_name):
    output = _run_adb(f"shell dumpsys gfxinfo {package_name}")
    result = {}
    for line in output.split("\n"):
        line = line.strip()
        if "Total frames rendered" in line:
            match = re.search(r"(\d+)", line)
            if match:
                result["total_frames"] = int(match.group(1))
        elif "Janky frames" in line and "legacy" not in line:
            match = re.search(r"(\d+)", line)
            if match:
                result["janky_frames"] = int(match.group(1))
        elif "50th percentile" in line:
            match = re.search(r"(\d+)ms", line)
            if match:
                result["p50_ms"] = int(match.group(1))
        elif "90th percentile" in line:
            match = re.search(r"(\d+)ms", line)
            if match:
                result["p90_ms"] = int(match.group(1))
        elif "95th percentile" in line:
            match = re.search(r"(\d+)ms", line)
            if match:
                result["p95_ms"] = int(match.group(1))
    return result


def collect_all_metrics(package_name, main_activity=None, driver=None):
    metrics = {}
    metrics["memory_pss_kb"] = get_memory_pss(package_name)
    metrics["cpu_percent"] = get_cpu_usage(package_name)
    gfx = get_gfx_summary(package_name)
    metrics.update(gfx)
    return metrics
