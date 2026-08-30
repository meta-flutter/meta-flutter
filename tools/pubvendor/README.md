# pubvendor.py

Generates a BitBake `.inc` from `pubspec.lock` so pub dependencies flow
through do_fetch/do_unpack with checksums, enabling `BB_NO_NETWORK`
Flutter app builds. Clean-room replacement for SeungkyunKim/pub2yocto
(concept only; see design notes in the script header).

    python3 tools/pubvendor/pubvendor.py -i app/pubspec.lock -o recipes-apps/app/app-pubcache.inc

Options: `--style old` for pre-kirkstone `_append` syntax,
`--resolve-missing` for pre-Dart-2.19 lockfiles lacking sha256 fields
(one-time fetch at generation, digest pinned into the .inc),
`--download-prefix` for DL_DIR layout.

## From the roll

Set `"pubvendor": true` on an entry in `meta-flutter-apps/conf/flutter-apps.json`
and `roll_meta_flutter.py` does the rest: it re-resolves the app's lockfile
against the SDK this layer pins, writes `<recipe>-pubcache.inc` and
`<recipe>-pubspec.lock` beside the recipe, and emits the lines below into it.
`--style` comes from `OVERRIDE_STYLE` in `tools/common.py`, which is
branch-specific in the same way `LICENSE_OPERATOR` is.

The re-resolve is deliberate. An app's committed lockfile was resolved
against whatever SDK its authors used, which is why generated recipes have
always carried `PUBSPEC_IGNORE_LOCKFILE = "1"` and deleted it at build time,
leaving every builder to re-resolve independently. Resolving once at roll
time and committing the result is that same concession made once, visibly,
and identically for everyone.

An app that cannot resolve against the pinned SDK is reported and skipped
rather than failing the roll, and its recipe is written *without* the
`require` line -- a recipe requiring a fragment that was never generated is
a parse error for the whole layer.

## Consuming a fragment by hand

    FILESEXTRAPATHS:prepend := "${THISDIR}:"
    SRC_URI += "file://app-pubspec.lock"
    require app-pubcache.inc
    inherit pub-cache
    PUBSPEC_APP_DIR = "${S}/${FLUTTER_APPLICATION_PATH}"
    PUBSPEC_LOCK_FILE = "app-pubspec.lock"
    PUBSPEC_IGNORE_LOCKFILE = "0"

`PUBSPEC_IGNORE_LOCKFILE` must be `"0"`: the fragment is generated from that
lockfile, and the class refuses to build when the two disagree.

`pub-cache.bbclass` then installs the vendored lockfile over the app's own,
checks it against `PUBSPEC_LOCK_SHA256`, synthesizes the
`hosted-hashes/*.sha256` files newer Dart SDKs verify against, and runs
`flutter pub get --offline --enforce-lockfile` before do_compile. It also
marks `do_archive_pub_cache` and `do_restore_pub_cache` noexec, so the
layer's own networked pub cache path stands down rather than fighting it.

`flutter pub get` rather than `dart pub get`: both resolve offline from the
staged cache, but only the flutter one writes
`.flutter-plugins-dependencies`, which is what tells the build which plugins
to register. With `dart pub get` an app builds green and registers nothing.

## Invariants guarded by tests

`tests/test_pubvendor.py`:

- zero network on the default path; archive URLs constructed from the
  pub API layout, checksums from the lockfile
- git SRCREV == lockfile resolved-ref, never a branch head
- git cache destsuffix == `<repo>-sha1(raw lockfile URL)`, matching pub's
  cache keying
- scp-style URLs (`git@host:org/repo.git`) normalized for the git fetcher
- BitBake-safe unique `name=` idents, collisions suffixed with url-hash

`offline_check.py` covers what those cannot: that the staged layout is one
pub accepts. It generates a fragment, fetches and stages exactly what its
SRC_URI entries name, runs `pub get --offline`, and fails if anything
resolved from outside the staged cache. Its fixture carries both a hosted
and a git dependency on purpose, and it rejects one that does not.

## CI

`.github/workflows/tools-tests.yml`, on a GitHub-hosted runner so it never
queues behind the Yocto builds:

- `pytest` on Python 3.10 (the floor -- `str | None` in a dataclass field)
  and current
- `offline_check.py` against two Dart SDKs, since the cache layout is a pub
  internal rather than a documented contract and can change under us

Run the unit tests locally with:

    python3 -m pytest tools -q

Requires `pyyaml` and `pytest`.
