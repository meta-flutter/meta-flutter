# meta-flutter

Yocto Layer for Google Flutter related projects.

_Updates_:

* Breaking Change
  
  Suffix for flutter runtime types has been changed to better define it.  Less confusing. 

  -runtimerelease (was -release)

  -runtimeprofile (was -profile)

  -runtimedebug (was -debug)

* FLUTTER_CHANNEL support has been deprecated

* FLUTTER_SDK_TAG - New approach.  Allows locking SDK and Engine to specific commit hash.
  Valid values for FLUTTER_SDK_TAG are here:  https://github.com/flutter/flutter/tags
  
* Flutter Engine Commit
  If `FLUTTER_SDK_TAG` is set to `"AUTOINC"` or not defined in local.conf, the engine commit used is master channel.  Otherwise the engine.version file in flutter/flutter is used to set the engine commit.

* build failure due to gn unknown parameter for `--no-build-embedder-examples`.  One solution to resolve this is to exclude `disable-embedder-examples` from PACKAGECONFIG in local.conf using:

  ```
  PACKAGECONFIG:pn-flutter-engine-runtimerelease = "disable-desktop-embeddings embedder-for-target fontconfig release"
  
  PACKAGECONFIG:pn-flutter-engine-runtimedebug = "disable-desktop-embeddings embedder-for-target fontconfig debug"

  PACKAGECONFIG:pn-flutter-engine-runtimeprofile = "disable-desktop-embeddings embedder-for-target fontconfig profile"
   ```
  This issue is related to missing gn options `--build-embedder-examples` and `--no-build-embedder-examples` from certain builds.  I have `disable-embedder-examples` defined in PACKAGECONFIG by default, so if you have an engine commit that is missing this option, you need to use the PACKAGECONFIG sequence above.  Once the gn option rolls into all channels this override will no longer be needed.

* When using the Sony embedders you need to specify FLUTTER_RUNTIME that matches other elements being installed, or make image will fail with package conflict. Example to avoid conflict with other recipes:
  ```
  echo 'FLUTTER_RUNTIME:pn-flutter-drm-gbm-backend = "debug"' >> ./conf/local.conf
  echo 'FLUTTER_RUNTIME:pn-flutter-wayland-client = "debug"' >> ./conf/local.conf
  ```


### Recommended development flow:
* Build Flutter application using desktop tools
* Use Flutter Engine Runtime=Debug build confirming it works on target.  Debug as needed using customdevices
* Create Yocto Recipe for your Flutter application using `flutter-gallery-*` as the template.
  Nested projected are supported using FLUTTER_APPLICATION_PATH.
  Passing Dart defines are done with FLUTTER_EXTRA_BUILD_ARGS.
* Add flutter-gallery, selected embedder, flutter-engine runtime=Release to your release image.
* Image device

Note: If you get a gray screen running the Gallery app, chances are you don't have a locale set.  Ensure your platform has a valid locale set.  See GLIBC_GENERATE_LOCALES and IMAGE_LINGUAS in one of the NVidia CI projects on how to do this.

Note: In theory Swift Shader (CPU render) engine builds should work with the right build flags.  Be warned it won't work out of the box.  Select a SoC with a GPU that supports OpenGL 3.0+ and save yourself the Engineering NRE.

## Layers dependencies

* meta-clang

Clang generates smaller runtime images, and is used by Google to build the the flutter engine for Android and iOS.  Changing toolchains should be avoided, as this would provide a path with little to no test milage.  If you are trying to port the flutter-engine to QNX 7.0 feel free to contact me.

## Overview

Target BSP is expected to have a GPU with OpenGLES v2.0+ support.  
If you selecting a part go with v3.0+, ideally one with Vulkan support.

This layer includes recipes to build

* flutter-sdk (channel selection, default is master if FLUTTER_SDK_TAG is not set)
* flutter-engine (tracks engine.version from FLUTTER_SDK_TAG)
* flutter-gallery Application (debug, profile, and release) requires master
* ivi-homescreen (Toyota/AGL - Wayland Embedder)
* flutter-pi (DRM w/VSync - Recommended embedder for DRM)
* flutter-wayland (POC) / waylandpp/ipugxml (archived)
* Sony embedders

## Notes

### CI Jobs

* linux-dummy.yml - meta-flutter canary CI job builds all recipes against linux-dummy kernel (except x11 client)

* rpi-32 (DRM, flutter-pi, flutter-gallery, vulkan)

    RPI3 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Vulkan driver responds to Vulkan)

    RPI4 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Mesa Vulkan Driver functional)

* rpi-64 (DRM, flutter-pi, flutter-gallery, vulkan)

    RPI3 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Vulkan driver loaded and not functional)

    RPI4 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Mesa Vulkan Driver functional)

Notes: CI job sstate is cleared between builds for all meta-flutter recipes; clean builds.


### General

Targets flutter-engine-* is known to work on

* AGL QEMU images - aarch64/x86_64 (CI job)
* DragonBoard 410c - aarch64
* Intel MinnowBoard Max (BayTrail) - intel-icore7-64
* NVIDIA Nano Dev Kit - aarch64 (CI job)
* NVIDIA Xavier NX Dev Kit - aarch64 (CI job)
* Raspberry Pi 3 / Compute - aarch64 / armv7hf (CI job)
* Raspberry Pi 4 / Compute - aarch64 (CI job)
* Renesas R-Car m3ulcb - aarch64
* STM32MP157x - cortexa7t2hf (CI job)
* etc, etc

## Include the Flutter SDK into Yocto SDK

Add to local.conf file:

    TOOLCHAIN_HOST_TASK:append = " nativesdk-flutter-sdk"

Then run:

    bitbake <image name> -c populate_sdk

## NXP i.MX 8QuadXPlus MEK

```
repo init -u https://source.codeaurora.org/external/imx/imx-manifest -b imx-linux-gatesgarth -m imx-5.10.9-1.0.0.xml
repo sync -j20
DISTRO=fslc-wayland MACHINE=imx8qxpmek source setup-environment build
pushd ../sources
git clone -b honister https://github.com/meta-flutter/meta-flutter.git
popd
bitbake-layers add-layer ../sources/meta-clang ../sources/meta-flutter
echo -e 'FLUTTER_SDK_TAG = "2.10.4"' >> conf/local.conf
echo -e 'IMAGE_INSTALL:append = " flutter-engine-runtimerelease"' >> conf/local.conf
echo -e 'IMAGE_INSTALL:append = " ivi-homescreen-runtimerelease"' >> conf/local.conf
echo -e 'IMAGE_INSTALL:append = " flutter-gallery-runtimerelease"' >> conf/local.conf
bitbake fsl-image-multimedia
```

## Raspberry PI 3/4 (aarch64)

See rpi-32.yml/rpi-64.yml for example usage, or download build artifacts from this repo.

## STM32MP157x Discovery Board

See `dunfell` branch for CI job example.

## General Yocto Notes

When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT:remove = "uninative"
