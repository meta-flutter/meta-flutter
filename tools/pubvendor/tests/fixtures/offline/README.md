Fixture for `offline_check.py`.

Deliberately carries both dependency sources, because they stage by different
routes and the git one is the less documented:

* `path` — hosted, staged as an unpacked archive under `hosted/<host>/`
* `args` — git, staged as a bare clone under `git/cache/<repo>-<sha1(url)>`,
  pinned to `resolved-ref` rather than the `v2.5.0` tag it was resolved from

Regenerate with `dart pub get` in this directory, then commit the lockfile.
Pinning to a tag keeps the resolved-ref stable across regenerations.
