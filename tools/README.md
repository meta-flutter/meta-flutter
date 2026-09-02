# Tools

## How to roll meta-flutter

1. Fork https://github.com/meta-flutter/meta-flutter
2. Run the roll: `tools/roll_meta_flutter.py`
3. Test
4. Open a PR saying what was tested, and on which targets

*Only stable branch versions are accepted.*

## Selecting a version

The roll updates `conf/include/releases_linux.json` first, then picks from it,
so the channel it resolves is the one the channel points at now rather than
whatever was last committed.

```
tools/roll_meta_flutter.py                     # stable
tools/roll_meta_flutter.py --channel=beta
tools/roll_meta_flutter.py --channel=dev
tools/roll_meta_flutter.py --version=3.47.1    # a specific version
```

It rolls `FLUTTER_SDK_TAG`, the dart-sdk recipe, and every app in
`meta-flutter-apps/conf/flutter-apps.json`.

## Reading the output

The roll is not silent and is not always successful. Three things it reports
are worth reading before opening the PR.

### Failures

A repo that cannot be cloned, whose declared license does not match its source,
or whose manifest entry is unusable **fails the roll**, and every failure is
reported rather than only the first:

```
2 of 24 repos failed to roll
  https://github.com/example/one.git
      RollError: license_type joins licenses with "&", but this branch uses "AND"
  https://github.com/example/two.git
      CalledProcessError: Command '['git', 'clone', ...]' returned non-zero exit status 128
```

Before #863 these were swallowed -- the work ran in a thread pool whose futures
were never read, so the roll printed "Repos Cloned" and exited 0 having
generated nothing for that repo. A roll that exits 0 has rolled everything it
was asked to.

### Skipped apps

An app whose declared `environment` excludes the pinned SDK is skipped with a
reason rather than generating a recipe that fails to build later:

```
1 app(s) skipped: the pinned SDK is outside their declared environment
  packages/foo/example: needs Dart >=3.20.0 <4.0.0, this layer pins Dart 3.13.1
```

This is deliberately conservative: a constraint it cannot parse, a missing
`environment`, or an unknown pinned version all mean *do not skip*. A gate that
misfires drops an app silently, which is worse than the build failure it
prevents. Use the manifest's `ignore` list for deliberate exclusions, so
"we do not want this" and "this cannot work here" stay distinguishable.

### Vendored lockfiles

For an app with `"pubvendor": true`, the roll says where the lockfile came
from, and the generated `.inc` records it too -- the fragment looks identical
in all three cases:

| | |
|---|---|
| `the project's own, unchanged` | it held against the pinned SDK under `--enforce-lockfile` |
| `re-resolved by the roll; the shipped one did not hold` | it did not |
| `created by the roll; none is shipped` | there was nothing to preserve |

An app declaring `resolution: workspace` has no lockfile of its own; the roll
follows the `workspace` list to the root and uses the one there, and says
`(pub workspace)`. See `tools/pubvendor/README.md`.

### Hand-written recipes that vendor

The generator only ever emits `inherit flutter-app` or `inherit flutter-web`. A
recipe that needs anything else -- `flutter-app-native`, an extra task, a
`do_install:append` -- has to be written by hand, and generating over it would
quietly drop whatever made it work.

`"generate_recipes": false` covers that case: the roll clones the repository and
regenerates the app's `-pubcache.inc` and `-pubspec.lock` against the pinned SDK,
and writes no recipe. Without it a vendored fragment ages silently against a
moving SDK, and the eventual failure is a `PUBSPEC_LOCK_SHA256` mismatch that
says nothing about why.

### Flutter SDK apps

The apps the SDK itself ships have no manifest entry and no SRCREV: they move
with `FLUTTER_SDK_TAG`, so the roll regenerates their recipes rather than
tracking them. It clones `flutter/flutter` at the commit the pinned release
names -- blobless and narrowed to three directories, about 130 MB -- and emits
one recipe per candidate into `recipes-graphics/flutter-sdk/apps/`, plus a
single fragment vendoring the workspace's pub cache for all of them.

Apps that are not candidates are reported with a reason rather than dropped.
Anything hand-tuned belongs in `sdk-apps-overrides.json`, keyed by app path, so
the next roll cannot lose it.

If the clone fails the roll says so and continues: it has already updated the
pinned SDK by that point, and one unreachable remote should not discard that.
The warning matters, though -- recipes left describing the previous SDK keep
parsing, and only the two built in CI would notice.

    tools/sdk_apps.py --path .              # regenerate by hand
    tools/sdk_apps.py --clone /path/to/ff   # reuse an existing checkout

## Manifest keys

`meta-flutter-apps/conf/flutter-apps.json`, one entry per repository. `uri` and
`branch` are required -- an entry missing either is an error, not something to
skip past.

| key | |
|---|---|
| `uri`, `branch`, `rev` | where to clone from, and optionally what to pin to |
| `license_file`, `license_type` | checked against the text the recipe ships |
| `license_validate` | set false for a license the detector reads wrong |
| `author` | recorded as `AUTHOR` in the generated recipe |
| `folder` | `first-party` or `third-party` |
| `ignore` | paths not to generate recipes for |
| `rdepends` | runtime dependencies, per app path |
| `output_folder`, `variables`, `src_folder`, `src_files`, `entry_files` | per-app recipe overrides |
| `compiler_requires_network` | apps whose build hooks fetch |
| `pubvendor` | `true` to vendor the pub cache, `"resolve"` to always replace a committed lockfile |
| `generate_recipes` | `false` for a repository whose recipes are hand-written; the roll still vendors its pub cache |

## Requirements

Python 3.10 or newer, and:

    pip install -r tools/requirements.txt

`pycurl` and `certifi` download `releases_linux.json` and the version files;
`pyyaml` reads every `pubspec.yaml`. None is optional, and `pycurl` needs
libcurl headers to build (`libcurl4-openssl-dev` on Debian, `libcurl-devel` on
Fedora).

The list is checked against the code: `tools/tests/test_requirements.py` walks
every import under `tools/` and fails if one is missing from the file, or if
the file names something nothing imports. A hand-kept list drifted twice before
that existed.

The roll also needs `git`, and `flutter` on `PATH` for any app that sets
`"pubvendor"` -- at the version this layer pins, since that is what the
lockfile is resolved against.

## Tests

```
python -m pytest tools -q
```

Runs in CI on every change under `tools/`, against two Python versions, plus a
real offline `pub get` against two Dart SDKs -- the staged pub cache layout is
a pub internal, so only a real resolve shows it is one pub accepts.
