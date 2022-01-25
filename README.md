# meta-flutter

Yocto Layer for Google Flutter related projects.

Recommended development flow:
* Build Flutter application using desktop tools
* Use Flutter Engine Runtime=Debug build confirming it works on target.  Debug as needed via `flutter attach`
* Create Yocto Recipe for Flutter application using `flutter-gallery` as template.
* Add flutter-gallery, selected embedder, flutter-engine runtime=Release to your release image.
* Image device

Note: If you get a gray screen running the Gallery app, chances are you don't have `LC_ALL` set.  Check `/usr/share/locale/` on your target for available locale, and set LC_ALL appropriately.  Example: `export LC_ALL=en_GB`

Note: In theory Swift Shader (CPU render) engine builds should work with the right build flags.  Be warned it won't work out of the box.  Select a SoC with a GPU that supports OpenGL 3.0+ and save yourself the Engineering NRE.

## Layers dependencies

* meta-clang

Clang generates smaller runtime images, and is used by Google to build the the flutter engine for Android and iOS.  Changing toolchains should be avoided, as this would provide a path with little to no test milage.  If you are trying to port the flutter-engine to QNX 7.0 feel free to contact me.

## Overview

Target BSP is expected to have a GPU with OpenGLES v2.0+ support.  
If you selecting a part go with v3.0+, ideally one with Vulkan support.

This layer includes recipes to build

* Toyota ivi-homescreen (Recommended embedder for Wayland)
* flutter-pi (DRM w/VSync - Recommended embedder for DRM)
* flutter-engine (channel selection, default is beta)
* flutter-sdk (channel selection, default is beta)
* flutter-gallery Application (interpreted and AOT - requires dev channel override)
* flutter-wayland (POC) / waylandpp/ipugxml (archived)
* Sony embedders

## Notes

### CI Jobs

* linux-dummy.yml - meta-flutter canary CI job builds all recipes against linux-dummy kernel (except x11 client)

* rpi-32 (DRM, flutter-pi, flutter-gallery, vulkan)

    RPI3 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Vulkan driver responds to Vulkan)

    RPI4 DRM - 32-bit RaspberryPi3 image - No X11/Wayland (Mesa Vulkan Driver functional)

* rpi-64 (DRM, flutter-pi, flutter-gallery, vulkan)

    RPI3 DRM - 64-bit RaspberryPi3 image - No X11/Wayland (Vulkan driver not present)

    RPI4 DRM - 64-bit RaspberryPi3 image - No X11/Wayland (Mesa Vulkan Driver functional)

    Zero2W - 64-bit Zero2W image - No X11/Wayland (Mesa Vulkan Driver functional)

Notes: CI job sstate is cleared between builds for all meta-flutter recipes; clean builds.

CI jobs for common targets will be added.  STM32MP157x is planned next.

### General

Targets flutter-engine is known to work on

* AGL QEMU images - x86_64
* DragonBoard 410c - aarch64
* Intel MinnowBoard Max (BayTrail) - intel-icore7-64
* NVIDIA Nano Dev Kit - aarch64
* NVIDIA Xavier NX Dev Kit - aarch64
* Raspberry Pi 3 / Compute - aarch64 / armv7hf (CI job)
* Raspberry Pi 4 / Compute - aarch64 (CI job)
* Renesas R-Car m3ulcb - aarch64
* STM32MP157x - cortexa7t2hf
* etc, etc

Note: 32-bit ARM builds currently require Flutter Channel = Master until commit makes it into dev->beta->stable.

## Include the Flutter SDK into Yocto SDK

Add to local.conf file:

    TOOLCHAIN_HOST_TASK_append = " flutter-sdk-nativesdk"

Then run:

    bitbake <image name> -c populate_sdk

## NVIDIA Xavier/Nano

local.conf changes

    FLUTTER_SDK_TAG = "2.10.0-0.2.pre"
    IMAGE_INSTALL_append = " flutter-drm-eglstream-backend"
    IMAGE_INSTALL_append = " flutter-gallery"

OR

    FLUTTER_SDK_TAG = "2.10.0-0.2.pre"
    CORE_IMAGE_EXTRA_INSTALL += "\
        flutter-drm-eglstream-backend \
        flutter-gallery \
    "

Build EGL image

    bitbake demo-image-egl

Run Flutter application on target (defaults to AOT)

    FLUTTER_DRM_DEVICE=/dev/dri/card0 flutter-drm-eglstream-backend -b /usr/share/flutter-gallery/sony

## NXP i.MX 8QuadXPlus MEK

```
repo init -u https://source.codeaurora.org/external/imx/imx-manifest -b imx-linux-gatesgarth -m imx-5.10.9-1.0.0.xml
repo sync -j20
DISTRO=fslc-wayland MACHINE=imx8qxpmek source setup-environment build
pushd ../sources
git clone -b dunfell https://github.com/jwinarske/meta-flutter.git
popd
bitbake-layers add-layer ../sources/meta-clang ../sources/meta-flutter
echo -e 'FLUTTER_SDK_TAG = "2.8.1"' >> conf/local.conf
echo -e 'IMAGE_INSTALL_append = " flutter-wayland"' >> conf/local.conf
echo -e 'IMAGE_INSTALL_append = " flutter-gallery"' >> conf/local.conf
bitbake fsl-image-multimedia
```

## Raspberry PI 3/4 (aarch64)

See honister branch README.md and CI jobs for more detail

## STM32MP157x Discovery Board

See stm32mp15.yml (CI job) for example build

See release notes regarding "fip": https://wiki.st.com/stm32mpu/wiki/STM32MP15_OpenSTLinux_release_note

Run Flutter application on target (defaults to AOT)

    FLUTTER_DRM_DEVICE=/dev/dri/card0 flutter-drm-eglstream-backend -b /usr/share/flutter-gallery/sony

## General Yocto Notes

When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT_remove = "uninative"
