# meta-flutter

Yocto Layer for Google Flutter related projects.

_Updates_:

March 17, 2023
* APP_GEN_SNAPSHOT_FLAGS - allows setting gen_snapshot flags like `--no-use-integer-division`
* FLUTTER_APP_RUNTIME_MODES - allows setting runtime mode per app.  Deprecates use of FLUTTER_APP_SKIP_DEBUG_INSTALL.
* APP_AOT_EXTRA_DART_DEFINES - allows setting Dart defines for AOT builds
* Local version patch override. allows overriding patch set applied.  Could also be in bbappend.

March 5, 2023
* 3.7.6 support introduced
* Toyota OSS 0223
* local resolution of Flutter SDK and Engine Versions - LTS

Dec 26, 2022

* dart-sdk added - building/linking with the Yocto Clang toolchain.
* Package Groups added - flutter-agl-apps, flutter-test-apps
* Container Image added - app-container-image, app-container-image-flutter-auto
* Breaking Changes

  Removed BBCLASS implementation for -runtimedebug, -runtimeprofile, -runtimerelease
  Removed FLUTTER_RUNTIME
  Flutter Engine runtime variants are now built based on PACKAGECONFIG values: debug, profile, release, jit_release.  The default is release.
  To add additional runtime variants in addition to `release` use this pattern in local.conf:
      `PACKAGECONFIG:append:pn-flutter-engine = " profile debug"`
  flutter-app.bbclass installs app for each engine build available in the target sysroot.
  By default Flutter Apps are not installed for runtime=debug.  This can be overriden in local.conf using `FLUTTER_APP_SKIP_DEBUG_INSTALL = "false"`.

* Breaking Change
  
  Suffix for flutter runtime types has been changed to better define it.  Less confusing. 

  -runtimerelease (was -release)
  -runtimeprofile (was -profile)
  -runtimedebug (was -debug)

* FLUTTER_CHANNEL support has been deprecated

* FLUTTER_SDK_TAG - New approach.  Allows locking SDK and Engine to specific commit hash.
  Valid values for FLUTTER_SDK_TAG are here:  https://github.com/flutter/flutter/tags
  
* Flutter Engine Commit
  The Flutter Engine Commit is based on the value of `FLUTTER_SDK_TAG.`
  The default value of `FLUTTER_SDK_TAG` is set in `conf/include/flutter-version.inc`.  If `FLUTTER_SDK_TAG` is overriden with `"AUTOINC"` in local.conf, stable channel is used for the engine commit.

* build failure due to gn unknown parameter for `--no-build-embedder-examples`.  One solution to resolve this is to exclude `disable-embedder-examples` from PACKAGECONFIG in local.conf using:

  ```
  PACKAGECONFIG:pn-flutter-engine-runtimerelease = "disable-desktop-embeddings embedder-for-target fontconfig release"
  PACKAGECONFIG:pn-flutter-engine-runtimedebug = "disable-desktop-embeddings embedder-for-target fontconfig debug"
  PACKAGECONFIG:pn-flutter-engine-runtimeprofile = "disable-desktop-embeddings embedder-for-target fontconfig profile"
   ```
  This issue is related to missing gn options `--build-embedder-examples` and `--no-build-embedder-examples` from certain builds.  I have `disable-embedder-examples` defined in PACKAGECONFIG by default, so if you have an engine commit that is missing this option, you need to use the PACKAGECONFIG sequence above.  Once the gn option rolls into all channels this override will no longer be needed.

### Recommended development flow:
* Create flutter workspace using ./tools/setup_flutter_workspace.py
* Debug and validate application on host using flutter-auto, AGL QEMU, or Linux GTK.
  - 1P Linux plugins should be avoided.  They may work on a runtime=debug image, but will not work in an AOT (runtime=release) image.
* Create Yocto Recipe for your Flutter application using `flutter-gallery-*` or one of the many app recipes as the template.
  Nested projected are supported using FLUTTER_APPLICATION_PATH.
  Passing Dart defines are done with FLUTTER_EXTRA_BUILD_ARGS.
* Add your app, and selected embedder to your release image.  The flutter engine will be implicitly added to the image.
* Image device

## Layers dependencies

* meta-clang

Clang generates smaller runtime images, and is used by Google to build the the flutter engine for Android and iOS.  Changing toolchains should be avoided, as this would provide a path with little to no test milage.

## Overview

Target BSP is expected to have a GPU with OpenGLES v2.0+ support.  
If you selecting a part go with v3.0+, ideally one with Vulkan support.

## Notes

* There are no OSS Linux embedders (that I am aware of that currently support software rendering).  The engine does support it.

* `flutter-auto` is the `agl` branch of https://github.com/toyota-connected/ivi-homescreen
  the `main` branch has moved to quarterly releases, the `agl` branch is directly supporting AGL development work.

### CI Jobs

* kirkstone-agl-renesas-m3.yml - Renesas M3 build.  Time boxed GPU driver (30 minutes?).  AGL canaray build.

* kirkstone-agl-x86_64.yml - meta-flutter QEMU image used with tools/seup_flutter_workspace.py.  Test build for AGL downstream work.

* kirkstone-imx8mmevk.yml - NXP imx8mmevk baseline Wayland image.

* kirkstone-linux-dummy.yml - Tests all recipes in the layer without a dummy Linux kernel (save build time).

* kirkstone-qc-dragonboard.yml - DB410C and DB820C Wayland images.

* kirkstone-rpi-zero2w-64.yml - RPI Zero2W Wayland image (flutter-auto).  Farily full featured with Network Manager, BT, WiFi, etc.

* kirkstone-stm32mp15.yml - eglfs (flutter-pi) st-core-image+SDK and wayland (flutter-auto) st-core-image+SDK.

Notes: CI job sstate is cleared between builds for all meta-flutter recipes; clean builds.


### General

Targets flutter-engine-* is known to work on

* AGL QEMU images - aarch64/x86_64
* Intel icore7-64
* NVIDIA Nano, Xavier Dev Kits - aarch64
* NXP iMX7 (caveats), iMX8
* Qualcomm DragonBoard DB410c, DB820, SA6155P, SA8xxx - aarch64
* Raspberry Pi 3 / Compute - aarch64 / armv7hf
* Raspberry Pi 4 / Compute - aarch64
* Raspberry Pi ZeroW / Zero2W - aarch64
* Renesas R-Car M3/H3 - aarch64
* STM32MP157x - cortexa7t2hf
* etc

## Include the Flutter SDK into Yocto SDK

Add to local.conf file:

    TOOLCHAIN_HOST_TASK:append = " nativesdk-flutter-sdk"

Then run:

    bitbake <image name> -c populate_sdk


Note: when using SDK you may need to add the following after installation:

    $ export SDK_ROOT=<install folder>/sysroots/x86_64-nodistrosdk-linux/usr/share/flutter/sdk
    $ git config --global --add safe.directory $SDK_ROOT

## General Yocto Notes

When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT:remove = "uninative"

## Flutter Workspace Automation
Please visit [here](tools/README.md) for how to setup Flutter workspace automatically.
