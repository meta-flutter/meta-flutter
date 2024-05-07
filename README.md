# meta-flutter

Yocto Layer for Google Flutter related projects.

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

| Variable                             | Description                                                                                                                         |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `FLUTTER_APPLICATION_PATH`           | This is the path relative to the root of the repository. Override in your application recipe.                                       |
| `FLUTTER_PREBUILD_CMD`               | If set will run before Flutter build step.                                                                                          |
| `PUB_CACHE_EXTRA_ARCHIVE_CMD`        | Command that is run prior to archive step of pub cache fetch. e.g. melos bootstrap.                                                 |
| `PUB_CACHE_EXTRA_ARCHIVE_PATH`       | Appends a path to `$PATH` which affects `PUB_CACHE_EXTRA_ARCHIVE_CMD`                                                               |
| `APP_AOT_EXTRA`                      | Allows passing dart defines to AOT step. e.g. `-DFLUTTER_APP_FLAVOR=prod`.                                                          |
| `APP_AOT_ENTRY_FILE`                 | Allows overriding the entry file. Default is `main.dart`.                                                                           |
| `APP_GEN_SNAPSHOT_FLAGS`             | Additional flags to pass to gen_snapshot. Default is `--obfuscate`.                                                                 |
| `FLUTTER_APP_RUNTIME_MODES`          | Allows overriding modes that install app. Default is `release`.                                                                     |
| `FLUTTER_APPLICATION_INSTALL_PREFIX` | Install prefix for flutter application install. Overriding enables installing into user directory. Default is `${datadir}/flutter`. |
| `FLUTTER_APPLICATION_INSTALL_SUFFIX` | Install suffix for flutter application install. Default is "${PUBSPEC_APPNAME}".                                                    |
| `PUBSPEC_IGNORE_LOCKFILE`            | Delete the `pubspec.lock` file during the build process                                                                             |
| `PUBSPEC_ENFORCE_LOCKFILE`           | Run `flutter pub get` with the `--enforce-lockfile` flag, preventing dependencies from being upgraded during build`                 |

### Supported Engine Variants

* debug
* profile
* release
* jit_release

## Dynamic Layers

* clang-layer

* gnome-layer
  
Zenity is used for fileselector plugin on ivi-homescreen.  To enable this add meta-gnome to your layers.

## Overview

Target BSP is expected to have a GPU with OpenGLES v2.0+ support.  
If you selecting a part go with v3.0+, ideally one with Vulkan support.

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

## Process to Auto Roll Flutter Applications

Origin of truth for Flutter Applications:

    conf/include/flutter-apps.json

Example roll

    cd /mnt/raid10
    git clone https://github.com/meta-flutter/meta-flutter
    git clone https://github.com/meta-flutter/workspace-automation
    cd workspace-automation
    ./roll_meta_flutter.py --path /mnt/raid10/meta-flutter --json /mnt/raid10/meta-flutter/conf/include/flutter-apps.json --patch-dir=/mnt/raid10/workspace-automation/patches

## Process to update Flutter SDK version

1. Follow process to Auto Roll Flutter Applications.  This step provides the latest `conf/include/releases_linux.json`
2. Review `conf/include/releases_linux.json` to determine the Flutter SDK stable version.
3. Update `conf/include/flutter-version.inc` with updated stable version.
4. Determine version of Dart SDK for stable channel and update recipe `recipes-devtools/dart/dart-sdk_x.x.x.bb`
5. Update `recipes-platform/images/app-container-user/dev_profile`

## conf/include/flutter-apps.json

This file is the origin of truth for all of the Flutter Applications present in this layer, and is consumed by [roll_meta_flutter.py](https://github.com/meta-flutter/workspace-automation/blob/main/roll_meta_flutter.py).

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
