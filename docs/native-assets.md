# Native build hook configuration

Dart native-asset build hooks read their settings only from
`hooks: user_defines:` in the pub workspace's `pubspec.yaml`; the hook runner
does not pass the environment through. Recipes inheriting `flutter-app-native`
set them with `FLUTTER_NATIVE_USER_DEFINES`, a JSON object keyed by Dart
package name.

For example, have the `sqlite3` package load the image's SQLite library
instead of downloading a prebuilt binary during `do_compile`:

```bitbake
RDEPENDS:${PN}:append = " libsqlite3"
FLUTTER_NATIVE_USER_DEFINES = '{"sqlite3": {"source": "system", "name": "${libdir}/libsqlite3.so.0"}}'
```

In this example the absolute `name` is deliberate. A bare `"name": "sqlite3"`
makes the hook look for the unversioned `libsqlite3.so`, which OE ships only
in `libsqlite3-dev`.

The example comes from `flatpak-minimal-appstream-dart-flathub-catalog` in
meta-flatpak-minimal. That recipe injects the same block with its own
`do_inject_user_defines` task today, and is the first to move to the variable.

The variable is not specific to `sqlite3`. It takes settings for any number
of packages, direct or transitive dependencies, and passes them through
unchanged. The keys each package accepts are defined by its hook; read the
package's `hook/build.dart`. A package whose hook offers no way to use a
library the layer built, or to build from source, cannot be made to build
offline this way.

## Supported values

Supported values make a hook use a library the layer built and packaged,
such as `"source": "system"` for `sqlite3`, or build from source in the work
directory. Give a source path relative to `pubspec.yaml`; hooks resolve
relative paths against it, and an absolute path under `TMPDIR` is rejected
(see below).

Pointing a hook at a download is not a supported configuration, whatever
keys the hook offers. A binary a hook fetches has no `SRC_URI`, checksum,
license or CVE data, and nothing in the image records it.

The class does not inspect values. Two things catch the usual cases:

- `do_compile` has no network, so a value that selects a download fails
  there rather than shipping a binary.
- The build directory path check rejects prefetching the prebuilt into
  `DL_DIR` and pointing the hook's download URL at it.

Neither catches a hook mode that loads a binary from a local directory, such
as `sqlite3`'s `test-sqlite3`. Review is the only check for those.

## How it is applied

`do_configure_native_hooks` runs after `do_patch` and before
`do_archive_pub_cache`, so the settings are in place before pub resolves.
It appends them to `${FLUTTER_PUB_ROOT}/pubspec.yaml` as a single
`hooks:` line, a JSON flow mapping, after a marker comment. The rest of the
file is left as it is.

The block is rewritten on every run, so changing the value replaces it and
clearing the value removes it. Under `devtool modify` the block is written
into the source tree being worked on.

## Restrictions

- `pubspec.yaml` must not already have a `hooks:` section. A recipe whose
  upstream pubspec has one edits that section itself. The one exception
  exists only to migrate meta-flatpak-minimal's
  `flatpak-minimal-appstream-dart-flathub-catalog` off its
  `do_inject_user_defines`, which this variable replaces: the unmarked
  `sqlite3: source: system` block that task appends is removed and rewritten
  as the marked block.
- `${FLUTTER_PUB_ROOT}` must be the pub workspace root. Flutter reads
  user defines only from the root's `pubspec.yaml`; a member's
  (`resolution: workspace`) are ignored, so the task fails instead of
  writing them there.
- Nothing may follow the marked block. Content after it fails the task
  instead of being dropped on the next rewrite.
- Values must not contain build directory paths (`TOPDIR`, `TMPDIR`,
  `DL_DIR`): the value reaches the hooks and names the pub cache archive in
  `DL_DIR`, so it would differ between build directories. Rejecting them
  also closes the `DL_DIR` route around the offline `do_compile`. Use target
  paths such as `${libdir}`, or paths relative to `pubspec.yaml`.
- Invalid JSON, or a value that does not map package names to objects, fails
  at parse time.
- The class sets no package's settings itself. Recipes declare the libraries
  a hook loads (`RDEPENDS`) or links against (`DEPENDS`) themselves.
