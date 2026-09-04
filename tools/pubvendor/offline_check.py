#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2026 Joel Winarske
# SPDX-License-Identifier: MIT
#
# offline_check.py - prove a generated .inc yields a cache pub can resolve
# from with no network.
#
# The unit tests assert what pubvendor.py *emits*. They cannot assert that
# the staged layout is one pub accepts, because that layout is a pub
# internal reconstructed from behaviour rather than a documented contract.
# Only running `pub get --offline` against a staged cache shows that, and
# only for the SDK it is run with -- hence the matrix in CI.
#
# Replays what bitbake would do: generate the fragment, fetch and stage
# exactly what its SRC_URI entries name, then run the command
# pub-cache.bbclass runs. Needs network to populate the cache; the pub get
# itself must not.

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tarfile
import tempfile
import urllib.request
from pathlib import Path

HOSTED_RE = re.compile(
    r'SRC_URI:?_?append = " (?P<url>\S+);name=(?P<name>\w+);'
    r'subdir=\$\{PUB_CACHE_LOCAL\}/(?P<subdir>\S+?);downloadfilename=\S+"'
)
GIT_RE = re.compile(
    r'SRC_URI:?_?append = " git://(?P<addr>\S+?);name=(?P<name>\w+);'
    r'protocol=(?P<proto>\w+);destsuffix=\$\{PUB_CACHE_LOCAL\}/(?P<dest>\S+?);'
)
SUM_RE = re.compile(r'SRC_URI\[(\w+)\.sha256sum\] = "([0-9a-f]+)"')
SRCREV_RE = re.compile(r'SRCREV_(\w+) = "([0-9a-f]{40})"')


def stage_hosted(inc: str, cache: Path) -> int:
    sums = dict(SUM_RE.findall(inc))
    count = 0
    for m in HOSTED_RE.finditer(inc):
        name, subdir = m.group("name"), m.group("subdir")
        dest = cache / subdir
        dest.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(m.group("url")) as rsp:  # noqa: S310
            blob = rsp.read()
        got = hashlib.sha256(blob).hexdigest()
        if got != sums[name]:
            sys.exit(f"checksum mismatch for {name}: {got} != {sums[name]}")
        with tempfile.NamedTemporaryFile(suffix=".tar.gz") as tmp:
            tmp.write(blob)
            tmp.flush()
            with tarfile.open(tmp.name) as tf:
                tf.extractall(dest, filter="data")
        # pub-cache.bbclass synthesizes these from the checksum flags
        _, host, pkgver = subdir.split("/", 2)
        hashes = cache / "hosted-hashes" / host
        hashes.mkdir(parents=True, exist_ok=True)
        (hashes / f"{pkgver}.sha256").write_text(sums[name])
        count += 1
    return count


def stage_git(inc: str, cache: Path) -> int:
    revs = dict(SRCREV_RE.findall(inc))
    count = 0
    for m in GIT_RE.finditer(inc):
        name, dest = m.group("name"), cache / m.group("dest")
        url = f"{m.group('proto')}://{m.group('addr')}"
        dest.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(
            ["git", "clone", "--bare", "--quiet", url, str(dest)], check=True
        )
        # The fragment pins SRCREV; fail loudly if the clone lacks it rather
        # than letting pub fall back to the network.
        rev = revs[name]
        have = subprocess.run(
            ["git", "-C", str(dest), "cat-file", "-e", f"{rev}^{{commit}}"],
            capture_output=True,
        )
        if have.returncode != 0:
            sys.exit(f"{name}: SRCREV {rev} not present in the clone of {url}")
        count += 1
    return count


def main(argv=None) -> int:
    here = Path(__file__).parent
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument(
        "--fixture", type=Path,
        default=here / "tests" / "fixtures" / "offline",
        help="directory holding pubspec.yaml + pubspec.lock",
    )
    ap.add_argument("--dart", default="dart", help="dart executable")
    args = ap.parse_args(argv)

    sys.path.insert(0, str(here))
    import pubvendor

    with tempfile.TemporaryDirectory() as td:
        work = Path(td)
        app, cache = work / "app", work / "pub_cache"
        shutil.copytree(args.fixture, app)
        shutil.rmtree(app / ".dart_tool", ignore_errors=True)

        inc_path = work / "fixture.inc"
        rc = pubvendor.main(["-i", str(app / "pubspec.lock"), "-o", str(inc_path)])
        if rc != 0:
            return rc
        inc = inc_path.read_text()

        n_hosted = stage_hosted(inc, cache)
        n_git = stage_git(inc, cache)
        print(f"staged {n_hosted} hosted, {n_git} git", file=sys.stderr)
        if not n_hosted or not n_git:
            sys.exit("fixture must exercise both hosted and git staging")

        proc = subprocess.run(
            [args.dart, "pub", "get", "--offline", "--enforce-lockfile"],
            cwd=app, env={**__import__("os").environ, "PUB_CACHE": str(cache)},
        )
        if proc.returncode != 0:
            sys.exit("offline `pub get` failed against the staged cache")

        # Resolution has to come from the cache. A pass with packages resolved
        # from somewhere else would not be testing the staged layout at all.
        cfg = json.loads(
            (app / ".dart_tool" / "package_config.json").read_text()
        )
        root = app.resolve().as_uri()
        stray = [
            p["name"] for p in cfg["packages"]
            if not p["rootUri"].startswith(cache.resolve().as_uri())
            and not p["rootUri"].startswith("../")
            and not p["rootUri"].startswith(root)
        ]
        if stray:
            sys.exit(f"resolved outside the staged cache: {', '.join(stray)}")
        print(
            f"ok: {len(cfg['packages'])} packages resolved from the staged cache",
            file=sys.stderr,
        )
    return 0


if __name__ == "__main__":
    sys.exit(main())
