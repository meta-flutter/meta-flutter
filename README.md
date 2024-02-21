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
