# meta-flutter

Yocto Layer for Flutter related artifacts.

Recommended development flow starts with making an embedder run on desktop, then target.  This approach will save you a good deal of time and aggrevation.

Note: In theory Swift Shader (CPU render) engine builds should work with the right build flags.  Be warned it won't work out of the box.  Select a SoC with a GPU that supports OpenGL 3.0+ and save yourself the Engineering NRE.

Note: If you get a gray screen running the Gallery app, chances are you don't have `LC_ALL` set.  Check `/usr/share/locale/` on your target for available locale, and set LC_ALL appropriately.  Example: `export LC_ALL=en_GB`

## Layers dependencies

* meta-clang Toyota and Sony embedders

## Overview

Target BSP is expected to have a GPU with OpenGLES v3.0+ support.

This layer includes recipes to build

* flutter-engine (channel selection, default is beta)
* flutter-sdk (channel selection, default is beta)
* fltter-gallery Application (interpreted and AOT - requires master channel override)
* flutter-pi (DRM w/VSync)
* flutter-wayland (basic POC) / waylandpp/ipugxml (archived)
* Toyota ivi-homescreen (Recommended)
* Sony embedders

## Notes

Targets flutter-engine is known to work on

* AGL QEMU images - x86_64
* DragonBoard 410c - aarch64
* Intel MinnowBoard Max (BayTrail) - intel-icore7-64
* NVIDIA Nano Dev Kit - aarch64
* NVIDIA Xavier NX Dev Kit - aarch64
* Raspberry Pi 3 / Compute - aarch64 / armv7hf
* Raspberry Pi 4 / Compute - aarch64
* Renesas R-Car m3ulcb - aarch64
* STM32MP157x - cortexa7t2hf
* etc, etc

Building for ARM (32-bit) has an outstanding P4 bug on this:  https://github.com/flutter/flutter/issues/83765


### NVIDIA Xavier/Nano

local.conf changes

    TARGET_GCC_VERSION = "8.3.0"
    FLUTTER_CHANNEL = "master"
    IMAGE_INSTALL_append = " flutter-drm-eglstream-backend"
    IMAGE_INSTALL_append = " flutter-gallery"

OR

    TARGET_GCC_VERSION = "8.3.0"
    FLUTTER_CHANNEL = "master"
    CORE_IMAGE_EXTRA_INSTALL += "\
        flutter-drm-eglstream-backend \
        flutter-gallery \
    "

Build EGL image

    bitbake demo-image-egl

Run Flutter application on target (defaults to AOT)

    FLUTTER_DRM_DEVICE=/dev/dri/card0 flutter-drm-eglstream-backend -b /usr/share/flutter-gallery/sony

### NXP i.MX 8QuadXPlus MEK

```
repo init -u https://source.codeaurora.org/external/imx/imx-manifest -b imx-linux-gatesgarth -m imx-5.10.9-1.0.0.xml
repo sync -j20
DISTRO=fslc-wayland MACHINE=imx8qxpmek source setup-environment build
pushd ../sources
git clone -b dunfell https://github.com/jwinarske/meta-flutter.git
popd
bitbake-layers add-layer ../sources/meta-clang ../sources/meta-flutter
echo -e 'TARGET_GCC_VERSION = "10.2.0"' >> conf/local.conf
echo -e 'FLUTTER_CHANNEL = "dev"' >> conf/local.conf
echo -e 'IMAGE_INSTALL_append = " flutter-wayland"' >> conf/local.conf
echo -e 'IMAGE_INSTALL_append = " flutter-gallery"' >> conf/local.conf
bitbake fsl-image-multimedia
...
Build Configuration:
BB_VERSION           = "1.48.0"
BUILD_SYS            = "x86_64-linux"
NATIVELSBSTRING      = "universal"
TARGET_SYS           = "aarch64-fslc-linux"
MACHINE              = "imx8qxpmek"
DISTRO               = "fslc-wayland"
DISTRO_VERSION       = "3.2-snapshot-20210616"
TUNE_FEATURES        = "aarch64 armv8a crc cortexa35 crypto"
TARGET_FPU           = ""
```

### Raspberry PI 3/4 (aarch64)

```
export MACHINE=raspberrypi4-64
```
or
```
export MACHINE=raspberrypi3-64
```

```
mkdir rpi_yocto && cd rpi_yocto
repo init -u https://github.com/jwinarske/manifests.git -m rpi64.xml -b honister
repo sync -j20
. ./setup-environment $MACHINE
echo -e 'FLUTTER_CHANNEL = "dev"' >> conf/local.conf
echo -e 'CORE_IMAGE_EXTRA_INSTALL:append = " flutter-pi"' >> conf/local.conf
echo -e 'CORE_IMAGE_EXTRA_INSTALL:append = " flutter-gallery"' >> conf/local.conf
bitbake core-image-minimal
```
Note: you may want/need to increase/decrease the `GPU_MEM` value.  It's defaulting to 256.

Checkout the Action CI artifacts for a ready to run image:
https://github.com/meta-flutter/meta-flutter/actions/workflows/rpi4-64.yml

Using ready built image run gallery app via:
```
cp /usr/share/homescreen/bundle/libapp.so /usr/share/homescreen/bundle/flutter_assets/app.so
flutter-pi --release /usr/share/homescreen/bundle/flutter_assets
```

### STM32MP157x Discovery Board (doesn't include build hack)

Setup Ubuntu 16.04 for building Yocto images.  envsetup.sh will complain if you're missing a package.  


```
mkdir openstlinux-5.10-dunfell-mp1-21-03-31 && cd openstlinux-5.10-dunfell-mp1-21-03-31
repo init -u https://github.com/STMicroelectronics/oe-manifest.git -b refs/tags/openstlinux-5.10-dunfell-mp1-21-03-31
repo sync -j20
DISTRO=openstlinux-eglfs MACHINE=stm32mp1-disco source layers/meta-st/scripts/envsetup.sh
pushd ../layers
git clone -b dunfell https://github.com/jwinarske/meta-flutter.git
git clone -b dunfell https://github.com/kraj/meta-clang.git
popd
bitbake-layers add-layer ../layers/meta-flutter ../layers/meta-clang
echo -e 'MACHINE_FEATURES_remove = "fip"\n' >> conf/local.conf
echo -e 'DISTRO_FEATURES_remove = "wayland"\n' >> conf/local.conf
echo -e 'DISTRO_FEATURES_remove = "x11"\n' >> conf/local.conf
echo -e 'FLUTTER_CHANNEL = "master"\n' >> conf/local.conf
echo -e 'CORE_IMAGE_EXTRA_INSTALL += " \' >> conf/local.conf
echo -e '  flutter-pi \' >> conf/local.conf
echo -e '  flutter-drm-gbm-backend \' >> conf/local.conf
echo -e '  flutter-gallery \' >> conf/local.conf
echo -e '"' >> conf/local.conf
cat conf/local.conf
bitbake st-image-core
...
TARGET_SYS      = "arm-ostl-linux-gnueabi"
MACHINE         = "stm32mp1-disco"
DISTRO          = "openstlinux-eglfs"
DISTRO_VERSION  = "3.1-snapshot-20210602"
TUNE_FEATURES   = "arm vfp cortexa7 neon vfpv4 thumb callconvention-hard"
TARGET_FPU      = "hard"
```

See release notes regarding "fip": https://wiki.st.com/stm32mpu/wiki/STM32MP15_OpenSTLinux_release_note

Build EGL image

    bitbake demo-image-egl

Run Flutter application on target (defaults to AOT)

    FLUTTER_DRM_DEVICE=/dev/dri/card0 flutter-drm-eglstream-backend -b /usr/share/flutter-gallery/sony

### General Yocto Notes

When building on systems with GCC version > than uninative in Yocto distro add the following to conf/local.conf

    INHERIT_remove = "uninative"
