#!/usr/bin/env python3
"""Install a .mrpack into a directory, as a launcher or server installer would (used by the CI boot test).

Usage: mrpack_install.py <pack.mrpack> <target-dir> [--side server|client]

Downloads every file of modrinth.index.json whose env for that side is not "unsupported", checks its sha1 and sha512,
then copies overrides/ and <side>-overrides/. Only the download hosts the .mrpack format allows are fetched.
"""
import argparse
import hashlib
import json
import pathlib
import sys
import time
import urllib.parse
import urllib.request
import zipfile

ALLOWED_HOSTS = {"cdn.modrinth.com", "github.com", "raw.githubusercontent.com", "gitlab.com"}


def fetch(url: str, attempts: int = 4) -> bytes:
    for attempt in range(1, attempts + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "vocana-resonance-ci (github.com/slappyfish167)"})
            with urllib.request.urlopen(req, timeout=120) as resp:
                return resp.read()
        except Exception as exc:  # network hiccups on shared runners
            if attempt == attempts:
                raise
            print(f"  retry {attempt} for {url}: {exc}", file=sys.stderr)
            time.sleep(5 * attempt)
    raise AssertionError("unreachable")


def safe_path(root: pathlib.Path, rel: str) -> pathlib.Path:
    out = (root / rel).resolve()
    if root.resolve() not in out.parents:
        raise ValueError(f"path escapes the instance: {rel}")
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("mrpack")
    ap.add_argument("target")
    ap.add_argument("--side", choices=("server", "client"), default="server")
    args = ap.parse_args()

    target = pathlib.Path(args.target)
    target.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(args.mrpack) as z:
        index = json.loads(z.read("modrinth.index.json"))
        files = index.get("files", [])
        skipped = 0
        for entry in files:
            env = entry.get("env", {})
            if env.get(args.side) == "unsupported":
                skipped += 1
                continue
            dest = safe_path(target, entry["path"])
            url = entry["downloads"][0]
            host = urllib.parse.urlparse(url).hostname or ""
            if host not in ALLOWED_HOSTS:
                print(f"error: {entry['path']} comes from a host the format does not allow: {host}", file=sys.stderr)
                return 1
            data = fetch(url)
            for algo in ("sha1", "sha512"):
                want = entry.get("hashes", {}).get(algo)
                if want and hashlib.new(algo, data).hexdigest() != want:
                    print(f"error: {algo} mismatch for {entry['path']}", file=sys.stderr)
                    return 1
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
        copied = 0
        for name in z.namelist():
            for prefix in ("overrides/", f"{args.side}-overrides/"):
                if name.startswith(prefix) and not name.endswith("/"):
                    dest = safe_path(target, name[len(prefix):])
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    dest.write_bytes(z.read(name))
                    copied += 1
    print(f"installed {len(files) - skipped} files for the {args.side} ({skipped} not for this side), {copied} overrides")
    print(f"dependencies: {json.dumps(index.get('dependencies', {}))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
