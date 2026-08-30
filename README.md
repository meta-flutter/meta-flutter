# meta-flutter

Yocto Layer for Google Flutter related projects.

### Discord Server https://discord.gg/cBkecwT65Q

### Recommended development flow

* Create a flutter workspace using [flutter_workspace.py](https://github.com/meta-flutter/workspace-automation/blob/main/flutter_workspace.py)
* Debug and validate application running on your host machine using ivi-homescreen, flutter-auto, AGL QEMU, or Linux GTK.
* Create Yocto Recipe for your Flutter application using one of the pre-existing recipes as your template.
* Add your apps recipe and your selected embedder to IMAGE_INSTALL in your conf/local.conf file.
* Image device

### Supported Flutter Application types

* Flutter Application
* Flutter Web Application

### Flutter Application recipe variables

| Variable                             | Description|
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `FLUTTER_APPLICATION_PATH`           | This is the path relative to the root of the repository. Override in your application recipe.|
| `FLUTTER_PREBUILD_CMD`               | If set will run before Flutter build step.|
| `PUB_CACHE_EXTRA_ARCHIVE_CMD`        | Command that is run prior to archive step of pub cache fetch. e.g. melos bootstrap.|
| `PUB_CACHE_EXTRA_ARCHIVE_PATH`       | Appends a path to `$PATH` which affects `PUB_CACHE_EXTRA_ARCHIVE_CMD`|
| `APP_AOT_EXTRA`                      | Allows passing dart defines to AOT step. e.g. `-DFLUTTER_APP_FLAVOR=prod`.|
| `APP_AOT_ENTRY_FILE`                 | Allows overriding the entry file. Default is `main.dart`.|
| `APP_GEN_SNAPSHOT_FLAGS`             | Additional flags to pass to gen_snapshot. Default is `--obfuscate`.|
| `FLUTTER_APP_RUNTIME_MODES`          | Allows overriding modes that install app. Default is `release`.|
| `FLUTTER_APPLICATION_INSTALL_PREFIX` | Install prefix for flutter application install. Overriding enables installing into user directory. Default is `${datadir}/flutter`.|
| `FLUTTER_APPLICATION_INSTALL_SUFFIX` | Install suffix for flutter application install. Default is "${PUBSPEC_APPNAME}".|
| `PUBSPEC_IGNORE_LOCKFILE`            | Deletes pubspec.lock file if present.  Used in case where lock file does not build.|
| `APP_CONFIG`                         | toml file to install into bundle folder.  File will be installed as config.toml in the bundle root.|

### Supported Engine Variants

* debug
* profile
* release
* jit_release

## Required Layers
* core
* meta-python
* openembedded-layer

## Recommended Layers
* clang-layer

## Dynamic Layers

* clang-layer

* gnome-layer
  
Zenity is used for fileselector plugin on ivi-homescreen.  To enable this add meta-gnome to your layers.

## Overview

Target BSP is expected to have a GPU with OpenGLES v2.0+ support.  
If you are selecting a part go with v3.0+, ideally one with Vulkan support.

## Notes

* There are no OSS Linux embedders (that I am aware of that currently support software rendering).  The engine does support it.

* `flutter-auto` is the `agl` branch of https://github.com/toyota-connected/ivi-homescreen
  the `main` branch has moved to quarterly releases, the `agl` branch is directly supporting AGL development work.


### General

Targets flutter-engine-* is known to work on

* AGL QEMU images - aarch64/x86_64
* Intel icore7-64
* NVIDIA Nano, Xavier Dev Kits - aarch64
* NXP iMX7, iMX8; imx-weston requires patch for ivi-homescreen + flutter-auto
* Qualcomm DragonBoard DB410c, DB820, SA6155P, SA8xxx - aarch64
* Raspberry Pi 3 / Compute - aarch64 / armv7hf
* Raspberry Pi 4 / Compute - aarch64
* Raspberry Pi ZeroW / Zero2W - aarch64
* Renesas R-Car M3/H3 - aarch64
* STM32MP157x - cortexa7t2hf
* etc

### Gesture responsiveness on custom touch controllers

If drags and swipes feel unresponsive on a custom touch controller, the
cause is usually Flutter's touch slop -- the distance a pointer must travel
before a movement counts as a drag rather than a tap. The default suits a
phone digitizer and is often too small for an industrial or resistive panel.

Patch `kTouchSlop` in `flutter-sdk-native`:

    packages/flutter/lib/src/gestures/constants.dart

Raising it from 18 to 64 has made custom touch controllers markedly more
responsive in the field. The right value is panel-specific, so treat 64 as a
starting point rather than a recommendation.

## Include the Flutter SDK into Yocto SDK

Add to local.conf file:

    TOOLCHAIN_HOST_TASK:append = " nativesdk-flutter-sdk"

Then run:

    bitbake <image name> -c populate_sdk


Note: when using SDK you may need to add the following after installation:

    $ export SDK_ROOT=<install folder>/sysroots/x86_64-nodistrosdk-linux/usr/share/flutter/sdk
    $ git config --global --add safe.directory $SDK_ROOT

## General Yocto Notes

* When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT:remove = "uninative"


* The initial fetch with Flutter build will download over 14GB of source code. Running `bitbake -C cleanall flutter-engine` will clear the download cache. However, if an error occurs, the download cache remains intact, allowing you to resume the fetch later.

## Flutter Workspace Automation

Please visit [here](https://github.com/meta-flutter/workspace-automation).

## Using Flutter SDK on target

Append this to your `conf/local.conf`

    IMAGE_INSTALL:append = " packagegroup-flutter-sdk-deps"

You will also need to add `dev` packages to your platform that include libegl.so and libGLESv2.so. If the GPU driver for your platform is Mesa (such as Raspberry Pi 3/4/5), add these recipes to IMAGE_INSTALL in your `conf/local.conf`:

    libegl-mesa-dev
    libgles3-mesa-dev

From target terminal

    cd ~
    git clone https://github.com/flutter/flutter
    cd flutter/bin
    export PATH=$PATH:`pwd`
    export GDK_GL=gles
    flutter config --no-enable-android
    flutter config --no-enable-web
    flutter channel stable
    flutter doctor -v

Test gallery app

    cd ~
    git clone https://github.com/flutter/gallery
    cd gallery
    flutter run -d linux

## Process to Auto Roll Flutter Applications, Flutter SDK version, and Dart-SDK recipe

    git clone https://github.com/meta-flutter/meta-flutter
    cd meta-flutter

channel `stable`

    tools/roll_meta_flutter.py

channel `beta`

    tools/roll_meta_flutter.py --channel=beta

channel `dev`

    tools/roll_meta_flutter.py --channel=dev

specific version

    tools/roll_meta_flutter.py --version=2.40.0

## conf/include/flutter-apps.json

This file is the origin of truth for all of the Flutter Applications present, and is used by tools/roll_meta_flutter.py.

roll_meta_flutter.py autogenerates all of the flutter application recipes.

## Process used to update a Rust recipe

* Update recipe SRCREV to desired version, rename recipe name to match
* Run bitbake on the recipe, it will likely fail
* Open terminal to path of src
* delete Cargo.lock
* delete toolchain file if present
* add the toolchain version used in yocto to your path ahead of any others
* Manually build the crate using this toolchain. This will create a new Cargo.lock against the toolchain version.  This is a critical step.
* Run [root-pkg-ws](https://github.com/jwinarske/root-pkg-ws) --manifest-path=/Cargo.toml
* Copy the output from this tool to the recipe file
* Rebuild and confirm it passes
* Submit PR with updated recipe

If you need a more recent Rust toolchain for Kirkstone, you can use

    https://git.yoctoproject.org/git/meta-lts-mixins

The takeaway should be that Cargo.lock and toolchain versions are tightly coupled in Yocto.  If you don't follow this in theory you could set network enable for compile, and set the cargo bbclass to auto-vend.  This would break all LTS scenarios.

## Mirroring gn-fetched sources

Recipes that fetch with `gn://` (`flutter-engine`, `dart-sdk`, `pdfium`,
`libwebrtc`) pack their gclient tree into a single tarball in `DL_DIR`. That
tarball is an ordinary download as far as bitbake is concerned, so `PREMIRRORS`,
`MIRRORS` and `BB_FETCH_PREMIRRORONLY` work without anything special in the
fetcher. Two details are not obvious:

Match the scheme generically rather than naming `gn`:

    PREMIRRORS:prepend = ".*://.*  file:///path/to/mirror/ \n"

oe-core's mirror sanity check carries a hardcoded protocol list that predates
out-of-tree fetchers and does not include `gn`, so an explicit `gn://` pattern
produces an "Invalid protocol" warning.

Place the tarball under the url's own path, not at the mirror root. For
`gn://github.com/dart-lang/sdk.git` that means:

    <mirror>/dart-lang/dart-sdk-<pv>-<srcrev>-<confighash>.tar.bz2

The `<confighash>` covers the gclient config, `EXTRA_GN_SYNC` and
`GN_DEPS_SED_PATCHES`, so a recipe that changes any of them uses a different
tarball. Take the exact filename from a failed fetch log rather than
constructing it by hand.

To populate a mirror, build once with network access and copy the tarball out of
`DL_DIR`. An offline build then needs:

    BB_FETCH_PREMIRRORONLY = "1"
    BB_NO_NETWORK = "1"

## Flutter FFI plugins that load a system library

A Dart FFI plugin whose build hook emits a code asset resolved from the system
ends up in `NativeAssetsManifest.json` as `["system", "lib<name>.so"]`, and the
engine `dlopen`s that name at runtime. Plugins are written against desktop
Linux, where the unversioned `.so` is always present. On a Yocto image it is
not: oe-core ships `lib<name>.so.0.X.Y` and the SONAME symlink `lib<name>.so.0`
in the runtime package, and the unversioned symlink only in `-dev`, which
production images exclude. The plugin then fails at first use with

    ArgumentError: Failed to load dynamic library 'libsqlite3.so'

Two things have to be true, and fixing one without the other still fails.

**The asset has to resolve to the system library rather than a vendored copy.**
That is an app-side choice, expressed in the app's own `pubspec.yaml` -- a
`hooks:` section is rejected in `pubspec_overrides.yaml`, so a recipe that does
not own the source has to append it:

    hooks:
      user_defines:
        sqlite3:
          source: system

Do that in a task between `do_patch` and `do_archive_pub_cache`. Editing
`pubspec.yaml` after the first dependency resolution makes the offline
`pub get` in `do_compile` re-resolve, which reaches for security advisories and
fails with no network. A `hooks:` section changes no dependency, so the lockfile
is unaffected.

**The unversioned name has to exist on the image.** Depend on the runtime
package and symlink the unversioned name into the bundle's own `lib` directory:

    RDEPENDS:${PN} += "libsqlite3"

    do_install:append() {
        ln -sf ${libdir}/libsqlite3.so.0 \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libsqlite3.so
    }

The bundle's `lib` directory, not `${libdir}`. An unversioned `.so` under
`${libdir}` belongs to `-dev` by oe-core convention and trips the `dev-so` QA
check, so putting it there means suppressing a QA rule that is right. The bundle
directory is already where the embedder finds `libflutter_engine.so`, and
keeping the symlink there also avoids pulling `-dev` -- headers, static
libraries and all -- into a production image.

`flatpak-minimal-appstream-dart-flathub-catalog` carries both halves and is
worth reading before writing a new one.

This is per-app by necessity: only the recipe knows which FFI plugins its app
uses and which libraries they name. There is no layer-wide hook.

### 32-bit targets

Flutter app recipes do not build on 32-bit. `flutter build bundle` accepts only
64-bit Linux target platforms -- as of 3.47.1 `TargetPlatform` carries
`linux_x64`, `linux_arm64` and `linux_riscv64` and nothing 32-bit -- so
`FLUTTER_APP_SUPPORTED_ARCHS` skips app recipes on other architectures at parse
time rather than failing in `do_compile` after a full fetch and `pub get`.

The restriction is narrower than it looks. Only the bundle step is gated, and
what it produces is largely architecture-neutral: assets, fonts and `app.dill`,
which is Dart kernel. The architecture-specific artifact, `libapp.so`, comes
from `gen_snapshot` out of this layer's own `engine_sdk.zip`, built per
`MACHINE` -- and Dart has emitted arm32 for years. The engine and the embedders
still build for armv7 and link against a working 32-bit `libflutter_engine.so`.

So building the bundle as `linux-arm64` purely to satisfy the tool's allowlist,
and letting `gen_snapshot` emit the real arm32 code, looks feasible on paper.
Be aware of what it costs before trying: `--target-platform` is also what keys
`NativeAssetsManifest.json`, so an app with any FFI plugin would get a manifest
keyed `linux_arm64` while the engine looks up `linux_arm`. That turns a build
failure into a silent runtime one -- the key miss described above, with no
`["system", ...]` entry found and the fallback `dlopen` left to fail. For an app
with no native assets the question does not arise.

Closing the gap properly is upstream Flutter work: a `linux_arm` value in
`TargetPlatform`.
