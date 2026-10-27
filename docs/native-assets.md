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

The keys each package accepts are defined by its hook; read the package's
`hook/build.dart`.

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
  upstream pubspec has one edits that section itself.
- Values must not contain build directory paths (`TOPDIR`, `TMPDIR`,
  `DL_DIR`): the value reaches the hooks and names the pub cache archive in
  `DL_DIR`, so it would differ between build directories. Use target paths
  such as `${libdir}`.
- Invalid JSON, or a value that does not map package names to objects, fails
  at parse time.
- The class sets no package's settings itself. Recipes declare the libraries
  a hook loads (`RDEPENDS`) or links against (`DEPENDS`) themselves.
