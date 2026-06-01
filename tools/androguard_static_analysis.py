#!/usr/bin/env python3
"""Static analysis helper for Android APKs using androguard.

Usage:
    python tools/androguard_static_analysis.py --apk app/build/outputs/apk/debug/app-debug.apk

The script prints a concise summary to stdout and writes a JSON report next to the APK
unless --report is provided.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Dict, Iterable, List, Sequence, Set


URL_RE = re.compile(r"https?://[\w\-./?%&=:#]+", re.IGNORECASE)
EMAIL_RE = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
IP_RE = re.compile(r"\b(?:\d{1,3}\.){3}\d{1,3}\b")
SECRETISH_RE = re.compile(
    r"(?i)(?:api[_-]?key|secret|token|password|passwd|bearer)[^\w]{0,10}([A-Za-z0-9_\-./+=]{8,})"
)

SENSITIVE_PERMISSIONS = {
    "android.permission.READ_SMS",
    "android.permission.SEND_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.READ_CONTACTS",
    "android.permission.WRITE_CONTACTS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
}


def resolve_apk_path(value: str | None, script_dir: Path) -> Path:
    if value:
        return Path(value).expanduser().resolve()

    sibling_apk = script_dir.parent / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
    if sibling_apk.exists():
        return sibling_apk.resolve()

    local_apk = script_dir / "app-debug.apk"
    if local_apk.exists():
        return local_apk.resolve()

    raise FileNotFoundError("无法定位 APK，请通过 --apk 指定路径")


def safe_call(obj: Any, name: str, default: Any = None) -> Any:
    value = getattr(obj, name, None)
    if value is None:
        return default
    try:
        return value() if callable(value) else value
    except Exception:
        return default


def iter_dex_strings(dex_objects: Sequence[Any]) -> Set[str]:
    strings: Set[str] = set()
    for dex in dex_objects:
        for string_value in safe_call(dex, "get_strings", []) or []:
            if isinstance(string_value, str):
                strings.add(string_value)
    return strings


def normalize_class_name(class_name: str) -> str:
    return class_name[1:-1].replace("/", ".") if class_name.startswith("L") and class_name.endswith(";") else class_name


def package_bucket(class_name: str) -> str:
    normalized = normalize_class_name(class_name)
    parts = normalized.split(".")
    if len(parts) >= 3:
        return ".".join(parts[:3])
    if len(parts) >= 2:
        return ".".join(parts[:2])
    return normalized


def collect_instruction_targets(method: Any) -> Iterable[str]:
    code = safe_call(method, "get_code")
    if not code:
        return []

    targets: List[str] = []
    bc = safe_call(code, "get_bc")
    if not bc:
        return targets

    for instruction in safe_call(bc, "get_instructions", []) or []:
        if hasattr(instruction, "get_output"):
            try:
                text = instruction.get_output()
            except Exception:
                text = str(instruction)
        else:
            text = str(instruction)

        for match in re.findall(r"L[^;]+;->[^\s]+", text):
            targets.append(match)
    return targets


def analyze_apk(apk_path: Path) -> Dict[str, Any]:
    try:
        from androguard.misc import AnalyzeAPK
    except Exception as exc:  # pragma: no cover - dependency guidance
        raise RuntimeError(
            "未安装 androguard。请先运行: pip install androguard"
        ) from exc

    apk, dex_objects, _analysis = AnalyzeAPK(str(apk_path))

    files = list(safe_call(apk, "get_files", []) or [])
    permissions = list(safe_call(apk, "get_permissions", []) or [])
    activities = list(safe_call(apk, "get_activities", []) or [])
    services = list(safe_call(apk, "get_services", []) or [])
    receivers = list(safe_call(apk, "get_receivers", []) or [])
    providers = list(safe_call(apk, "get_providers", []) or [])

    class_counter: Counter[str] = Counter()
    package_counter: Counter[str] = Counter()
    call_counter: Counter[str] = Counter()
    method_count = 0
    field_count = 0
    abstract_or_native_methods = 0

    for dex in dex_objects:
        for cls in safe_call(dex, "get_classes", []) or []:
            class_name = safe_call(cls, "get_name", "") or ""
            if class_name:
                class_counter[class_name] += 1
                package_counter[package_bucket(class_name)] += 1

            methods = list(safe_call(cls, "get_methods", []) or [])
            fields = list(safe_call(cls, "get_fields", []) or [])
            method_count += len(methods)
            field_count += len(fields)

            for method in methods:
                code = safe_call(method, "get_code")
                if not code:
                    abstract_or_native_methods += 1
                    continue
                for target in collect_instruction_targets(method):
                    call_counter[target] += 1

    all_strings = iter_dex_strings(dex_objects)
    string_hits = {
        "urls": sorted({match for string_value in all_strings for match in URL_RE.findall(string_value)}),
        "emails": sorted({match for string_value in all_strings for match in EMAIL_RE.findall(string_value)}),
        "ip_addresses": sorted({match for string_value in all_strings for match in IP_RE.findall(string_value)}),
        "secret_like": sorted(
            {
                match.group(0)
                for string_value in all_strings
                for match in SECRETISH_RE.finditer(string_value)
            }
        ),
    }

    manifest_summary = {
        "package": safe_call(apk, "get_package", ""),
        "app_name": safe_call(apk, "get_app_name", ""),
        "main_activity": safe_call(apk, "get_main_activity", ""),
        "version_code": safe_call(apk, "get_androidversion_code", ""),
        "version_name": safe_call(apk, "get_androidversion_name", ""),
        "min_sdk": safe_call(apk, "get_min_sdk_version", ""),
        "target_sdk": safe_call(apk, "get_target_sdk_version", ""),
        "permissions": permissions,
        "sensitive_permissions": sorted(set(permissions) & SENSITIVE_PERMISSIONS),
        "activities": activities,
        "services": services,
        "receivers": receivers,
        "providers": providers,
    }

    file_types: Dict[str, int] = defaultdict(int)
    for file_name in files:
        suffix = Path(file_name).suffix.lower() or "[no_ext]"
        file_types[suffix] += 1

    top_calls = call_counter.most_common(100)
    top_packages = package_counter.most_common(50)

    report = {
        "apk_path": str(apk_path),
        "manifest": manifest_summary,
        "dex": {
            "dex_count": len(dex_objects),
            "class_count": len(class_counter),
            "method_count": method_count,
            "field_count": field_count,
            "abstract_or_native_methods": abstract_or_native_methods,
            "files_count": len(files),
            "file_types": dict(sorted(file_types.items(), key=lambda item: item[0])),
            "top_packages": [{"package": package, "count": count} for package, count in top_packages],
            "top_call_targets": [{"target": target, "count": count} for target, count in top_calls],
        },
        "strings": string_hits,
    }
    return report


def print_summary(report: Dict[str, Any]) -> None:
    manifest = report["manifest"]
    dex = report["dex"]
    strings = report["strings"]

    print(f"APK: {report['apk_path']}")
    print(f"Package: {manifest.get('package')}")
    print(f"App: {manifest.get('app_name')} | Version: {manifest.get('version_name')} ({manifest.get('version_code')})")
    print(f"Main activity: {manifest.get('main_activity')}")
    print(f"SDK: min={manifest.get('min_sdk')} target={manifest.get('target_sdk')}")
    print(
        f"DEX: {dex['dex_count']} | classes={dex['class_count']} | methods={dex['method_count']} | fields={dex['field_count']}"
    )

    if manifest.get("sensitive_permissions"):
        print("Sensitive permissions:")
        for permission in manifest["sensitive_permissions"]:
            print(f"  - {permission}")

    for label, values in strings.items():
        if values:
            print(f"{label}: {len(values)} hits")

    if dex["top_packages"]:
        print("Top packages:")
        for item in dex["top_packages"][:10]:
            print(f"  - {item['package']}: {item['count']}")

    if dex["top_call_targets"]:
        print("Top call targets:")
        for item in dex["top_call_targets"][:10]:
            print(f"  - {item['target']}: {item['count']}")


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Static analysis for Android APKs using androguard.")
    parser.add_argument("--apk", help="APK file path. Defaults to sibling app-debug.apk if omitted.")
    parser.add_argument(
        "--report",
        help="Output JSON report path. Defaults to <apk-stem>.androguard-report.json next to the APK.",
    )
    parser.add_argument(
        "--pretty",
        action="store_true",
        help="Pretty-print the JSON report to stdout after the summary.",
    )
    return parser


def main(argv: Sequence[str]) -> int:
    parser = build_arg_parser()
    args = parser.parse_args(argv)

    script_dir = Path(__file__).resolve().parent
    apk_path = resolve_apk_path(args.apk, script_dir)
    if not apk_path.exists():
        print(f"APK 不存在: {apk_path}", file=sys.stderr)
        return 2

    report = analyze_apk(apk_path)
    report_path = Path(args.report).expanduser().resolve() if args.report else apk_path.with_name(f"{apk_path.stem}.androguard-report.json")
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    print_summary(report)
    print(f"\nJSON report written to: {report_path}")
    if args.pretty:
        print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))