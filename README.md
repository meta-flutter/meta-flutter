# meta-flutter

Yocto Layer for Google Flutter related projects.

_Updates_: 
* FLUTTER_CHANNEL support has been deprecated.
* FLUTTER_SDK_TAG - New approach.  Allows locking SDK and Engine to specific commit hash.
  Valid values for FLUTTER_SDK_TAG are here:  https://github.com/flutter/flutter/tags 
* Flutter Engine Commit
  If FLUTTER_SDK_TAG is set to "AUTOINC", the engine commit used is master channel.  Otherwise the engine.version file in flutter/flutter is used to set the engine commit.

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
* vk-flutter-engine (tracks engine.version from FLUTTER_SDK_TAG) Vulkan Embedder PR
* flutter-gallery Application (debug, profile, and release) requires master
* ivi-homescreen (Toyota/AGL - Wayland Embedder)
* flutter-pi (DRM w/VSync - Recommended embedder for DRM)
* flutter-wayland (POC) / waylandpp/ipugxml (archived)
* Sony embedders

## Notes

### CI Jobs

* dunfell-agl-aarch64.yml - AGL emaultor build that uses non-locked values.  This is used as health gate for rolling commits in AGL.  No Vulkan support.

* dunfell-agl-x86_64.yml - AGL emaultor build that uses non-locked values.  This is used as health gate for rolling commits in AGL.  No Vulkan support.

* dunfell-dart-mx8m-mini.yml - Variscite DART-MX8M-MINI (NXP iMX8) Weston build + ivi-homescreen.  No Vulkan support.

* dunfell-linux-dummy.yml - meta-flutter canary CI job builds all recipes against linux-dummy kernel

* dunfell-nvidia-jetson-nano-devkit.yml - NVidia Jetson Nano Devkit weston build with ivi-homescreen and Vulkan suport.

* dunfell-nvidia-jetson-xavier-nx-devkit.yml - NVidia Jetson Xavier NX Devkit weston build with ivi-homescreen and Vulkan suport.

* dunfell-rpi4-64.yml - Raspberry PI4 EGL/DRM build with Flutter-pi and Vulkan (Mesa 21.2) support.

* dunfell-stm32mp15.yml - ST Microelectronics STM32MP15 EGL and Weston builds.  EGL build has flutter-pi (currently has rendering issues), and Weston build has ivi-homescreen which works beautifully.

For more Raspberry PI images see the Honister branch.


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

Note: 32-bit ARM builds were broken for a period, so if you have build issues only with 32-bit try moving to a newer tag.

## Include the Flutter SDK into Yocto SDK

Add to local.conf file:

    TOOLCHAIN_HOST_TASK_append = " flutter-sdk-nativesdk"

Then run:

    bitbake <image name> -c populate_sdk

## STM32MP157x Discovery Board

See dunfell-stm32mp15.yml (CI job) for more detail

See Wiki for flashing image and using customdevice:
https://github.com/meta-flutter/meta-flutter/wiki/STM32MP1-Disco-Notes

## NXP i.MX 8QuadXPlus MEK Example

```
repo init -u https://source.codeaurora.org/external/imx/imx-manifest -b imx-linux-gatesgarth -m imx-5.10.9-1.0.0.xml
repo sync -j20
DISTRO=fslc-wayland MACHINE=imx8qxpmek source setup-environment build
pushd ../sources
git clone -b dunfell https://github.com/meta-flutter/meta-flutter.git
popd
echo -e 'FLUTTER_SDK_TAG = "2.10.0-0.2.pre"' >> conf/local.conf
echo -e 'IMAGE_INSTALL_append = " flutter-engine-debug ivi-homescreen-debug flutter-gallery-debug"' >> conf/local.conf
bitbake-layers add-layer ../sources/meta-clang ../sources/meta-flutter
bitbake fsl-image-multimedia
```

## Raspberry PI 3/Zero (aarch64)

See honister branch README.md and CI jobs for more detail

## General Yocto Notes

When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT_remove = "uninative"
