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

Consume with `pub-cache.bbclass`:

    require app-pubcache.inc
    inherit pub-cache
    PUBSPEC_APP_DIR = "${S}"

The class asserts the in-tree lockfile matches PUBSPEC_LOCK_SHA256,
synthesizes hosted-hashes/*.sha256 for newer Dart content verification,
and runs `dart pub get --offline --enforce-lockfile` before do_compile.

Invariants guarded by tests (tests/test_pubvendor.py):
- zero network on the default path; archive URLs constructed from the
  pub API layout, checksums from the lockfile
- git SRCREV == lockfile resolved-ref, never a branch head
- git cache destsuffix == <repo>-sha1(raw lockfile URL), matching pub's
  cache keying (validate per-SDK with an offline `pub get` CI job)
- scp-style URLs (git@host:org/repo.git) normalized for the git fetcher
- BitBake-safe unique name= idents, collisions suffixed with url-hash

## Running the tests

    python3 -m pytest tools/pubvendor/tests -q

Requires `pyyaml` and `pytest`. Not yet wired into CI -- `tools/**` is in
the build workflow's `paths-ignore`, so a separate job is needed.
