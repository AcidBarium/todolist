import csv
import os
from datetime import datetime
from config import REPORT_DIR


def save_csv(rows, filename=None):
    os.makedirs(REPORT_DIR, exist_ok=True)
    if filename is None:
        filename = f"perf_data_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
    path = os.path.join(REPORT_DIR, filename)
    with open(path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerows(rows)
    return path


def generate_html_report(all_results, filename=None):
    os.makedirs(REPORT_DIR, exist_ok=True)
    if filename is None:
        filename = f"perf_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.html"
    path = os.path.join(REPORT_DIR, filename)

    rows_html = ""
    for i, r in enumerate(all_results, 1):
        test_name = r.get("test", "?")
        metrics = r.get("metrics", {})
        cells = f"<td>{i}</td><td>{test_name}</td>"
        for key in ["cold_start_ms", "warm_start_ms", "memory_pss_kb", "cpu_percent",
                     "response_time_ms", "avg_frame_time_ms", "max_frame_time_ms",
                     "jank_count", "estimated_fps"]:
            val = metrics.get(key, "")
            cells += f"<td>{val}</td>"
        rows_html += f"<tr>{cells}</tr>"

    html = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head><meta charset="utf-8"><title>ToDo-Compose 性能测试报告</title>
<style>
body {{ font-family: sans-serif; margin: 20px; }}
h1 {{ color: #333; }}
table {{ border-collapse: collapse; width: 100%; margin-top: 10px; }}
th, td {{ border: 1px solid #ccc; padding: 6px 10px; text-align: center; font-size: 13px; }}
th {{ background: #4a90d9; color: #fff; }}
tr:nth-child(even) {{ background: #f5f5f5; }}
.good {{ color: green; }}
.warn {{ color: orange; }}
.bad {{ color: red; }}
</style></head><body>
<h1>ToDo-Compose Appium 性能测试报告</h1>
<p>生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
<table>
<thead><tr>
<th>#</th><th>测试用例</th><th>冷启动(ms)</th><th>热启动(ms)</th>
<th>内存PSS(KB)</th><th>CPU(%)</th><th>响应时间(ms)</th>
<th>平均帧(ms)</th><th>最大帧(ms)</th><th>Jank数</th><th>FPS</th>
</tr></thead>
<tbody>
{rows_html}
</tbody></table></body></html>"""

    with open(path, "w", encoding="utf-8") as f:
        f.write(html)
    return path
