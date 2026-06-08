#!/usr/bin/env python3
import argparse
import json
import signal
import subprocess
import sys
import time
from collections import defaultdict
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
JS_TEMPLATE = SCRIPT_DIR / "frida_trace_callgraph.js"
DEFAULT_OUTPUT = SCRIPT_DIR / "output"


def build_agent(target_package: str, exclude_patterns: list[str]) -> str:
    if not JS_TEMPLATE.exists():
        raise FileNotFoundError(f"JS template not found: {JS_TEMPLATE}")

    import frida_tools
    bridges_dir = Path(frida_tools.__file__).resolve().parent / "bridges"
    bridge_js_path = bridges_dir / "java.js"
    if not bridge_js_path.exists():
        raise FileNotFoundError(f"Java bridge not found: {bridge_js_path}")

    bridge_js = bridge_js_path.read_text(encoding="utf-8")
    bridge_init = "\nObject.defineProperty(globalThis, 'Java', { value: bridge, enumerable: true, configurable: true });\n"

    hook_js = JS_TEMPLATE.read_text(encoding="utf-8")
    exclude_regex = ",".join(f"/{p}/" for p in exclude_patterns)
    hook_js = hook_js.replace("%TARGET_PACKAGE%", target_package)
    hook_js = hook_js.replace("%EXCLUDE_REGEX%", exclude_regex)

    return bridge_js + bridge_init + hook_js


def build_graph(edges: list[dict]) -> dict:
    nodes: dict[str, int] = {}
    node_list: list[dict] = []
    links: list[dict] = []
    adj: defaultdict[str, defaultdict[str, int]] = defaultdict(lambda: defaultdict(int))

    for e in edges:
        caller = e.get("caller")
        callee = e.get("callee")
        count = e.get("count", 1)
        if caller and callee:
            adj[caller][callee] += count

    for caller, callees in adj.items():
        if caller not in nodes:
            nodes[caller] = len(node_list)
            node_list.append({"id": caller})
        for callee, count in callees.items():
            if callee not in nodes:
                nodes[callee] = len(node_list)
                node_list.append({"id": callee})
            links.append({"source": nodes[caller], "target": nodes[callee], "value": count})

    return {"nodes": node_list, "links": links}


def main():
    signal.signal(signal.SIGINT, lambda *_: (_ for _ in ()).throw(KeyboardInterrupt))

    parser = argparse.ArgumentParser(description="Frida Call Graph Builder")
    parser.add_argument("--package", default="com.ayse.todocompose", help="Target package")
    parser.add_argument("--include", help="Java package prefix to hook")
    parser.add_argument("--exclude", action="append", default=[],
                        help="Regex patterns to exclude (repeat for multiple)")
    parser.add_argument("--duration", type=int, default=60,
                        help="Trace duration in seconds (default: 60)")
    parser.add_argument("--output", default=str(DEFAULT_OUTPUT),
                        help="Output directory")

    args = parser.parse_args()
    target_package = args.include or args.package

    default_excludes = [
        r".*\$Companion.*", r".*\$DefaultImpls.*",
        r".*\$inlined.*", r".*WhenMappings.*", r".*\$ExternalSynthetic.*",
    ]
    all_excludes = default_excludes + args.exclude

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)
    json_path = output_dir / "callgraph.json"
    html_src = SCRIPT_DIR / "callgraph_viewer.html"
    html_dst = output_dir / "callgraph_viewer.html"

    import frida

    print(f"[*] Target package: {target_package}")
    print(f"[*] Excludes: {all_excludes}")
    print(f"[*] Output: {json_path}")

    agent_js = build_agent(target_package, all_excludes)

    print("[*] Waiting for USB device...")
    usb_device = frida.get_usb_device()
    print(f"[*] Device: {usb_device}")

    subprocess.run(["adb", "shell", "am", "force-stop", args.package],
                   capture_output=True, timeout=15)
    time.sleep(1)
    subprocess.run(["adb", "shell", "am", "start", "-n",
                    f"{args.package}/.MainActivity"],
                   capture_output=True, timeout=15)
    time.sleep(3)

    print(f"[*] Attaching to {args.package}...")
    session = None
    for attempt in range(10):
        try:
            session = usb_device.attach(args.package)
            break
        except Exception:
            pass
        try:
            for p in usb_device.enumerate_processes():
                if args.package in p.name or p.name.startswith("ToDo"):
                    session = usb_device.attach(p.pid)
                    break
        except Exception:
            pass
        if session is not None:
            break
        if attempt < 9:
            time.sleep(2)
    if session is None:
        print("[!] Could not attach to process. Make sure the app is running.")
        return 1

    collected_edges: list[dict] = []
    start_time = time.time()

    def on_message(message, data):
        if message.get("type") == "send":
            payload = message.get("payload", {})
            if not isinstance(payload, dict):
                return

            ptype = payload.get("type")

            if ptype == "edges":
                collected_edges.extend(payload.get("data", []))
                elapsed = time.time() - start_time
                print(f"\r[*] Edges: {len(collected_edges)} (t={elapsed:.0f}s)", end="", flush=True)

            elif ptype == "scanDone":
                print(f"\n[*] Hook ready: {payload.get('hookedCount', 0)} classes, "
                      f"{payload.get('hookedMethods', 0)} methods hooked")

            elif ptype == "log":
                print(f"\n[LOG] {payload.get('msg', '')}")

            elif ptype == "error":
                print(f"\n[!] JS Error: {payload.get('msg', payload.get('description', ''))}", file=sys.stderr)

        elif message.get("type") == "error":
            desc = message.get("description", "")
            stack = message.get("stack", "")
            print(f"\n[!] Frida Error: {desc}", file=sys.stderr)
            if stack:
                print(f"    {stack}", file=sys.stderr)

    script = session.create_script(agent_js)
    script.on("message", on_message)
    script.load()

    print(f"[*] Agent injected! Tracing for {args.duration}s...")
    print("[*] Go interact with the app on the emulator!\n")

    try:
        remaining = args.duration
        while remaining > 0:
            time.sleep(1)
            remaining -= 1
            elapsed = int(time.time() - start_time)
            print(f"\r[*] Elapsed: {elapsed}s / {args.duration}s | Edges: {len(collected_edges)}   ",
                  end="", flush=True)
    except KeyboardInterrupt:
        print("\n[*] Stopped by user")
    finally:
        elapsed = time.time() - start_time
        print(f"\n[*] Building graph from {len(collected_edges)} raw edges...")
        graph = build_graph(collected_edges)
        graph["metadata"] = {
            "package": args.package,
            "target": target_package,
            "duration": round(elapsed, 1),
            "total_edges": len(collected_edges),
            "unique_nodes": len(graph["nodes"]),
            "unique_links": len(graph["links"]),
        }

        json_data = json.dumps(graph, indent=2, ensure_ascii=False)
        json_path.write_text(json_data, encoding="utf-8")
        print(f"[*] JSON written: {json_path}")

        if html_src.exists():
            html_template = html_src.read_text(encoding="utf-8")
            embed_marker = "/*__EMBED_DATA__*/"
            if embed_marker in html_template:
                standalone_html = html_template.replace(
                    embed_marker,
                    "var EMBEDDED_GRAPH_DATA = " + json_data + ";"
                )
            else:
                standalone_html = html_template
            html_dst.write_text(standalone_html, encoding="utf-8")
            print(f"[*] Viewer written: {html_dst}")

        print(f"\n[*] Done! Open in browser:")
        print(f"    file:///{html_dst.as_posix()}")

        session.detach()


if __name__ == "__main__":
    raise SystemExit(main())
