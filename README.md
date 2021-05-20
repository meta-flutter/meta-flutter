# meta-flutter

Yocto Layer for Flutter related artifacts.

Recommended development flow starts with making an embedder run on desktop, then target.  This approach will save you a good deal of time and aggrevation.

Note: In theory Swift Shader (CPU render) engine builds should work with the right build flags.  Be warned it won't work out of the box.  Select a SoC with a GPU that supports OpenGL 3.0+ and save yourself the Engineering NRE.

Note: If you get a gray screen running the Gallery app, chances are you don't have `LC_ALL` set.  Check `/usr/share/locale/` on your target for available locale, and set LC_ALL appropriately.  Example: `export LC_ALL=en_GB`

## Layers dependencies

* meta-clang (embedders only)

## Overview

Target BSP is expected to have a GPU with OpenGLES v3.0+ support.

This layer includes recipes to build

* flutter-engine (channel selection, default is beta)
* flutter-sdk (channel selection, default is beta)
* fltter-gallery Application (interpreted and AOT - requires master channel override)
* flutter-pi (DRM w/VSync)
* flutter-wayland (basic POC) / waylandpp/ipugxml
* Sony embedders (No VSync)

## Notes

Targets flutter-engine is known to work on

* DragonBoard 410c - aarch64
* Intel MinnowBoard Max (BayTrail) - intel-icore7-64
* NVidia Nano Dev Kit - aarch64
* NVidia Xavier NX Dev Kit - aarch64
* Raspberry Pi 3 - armv7hf
* Raspberry Pi 4 / Compute - aarch64
* Renesas R-Car m3ulcb - aarch64
* AGL QEMU images - x86_64
* etc, etc


### NVidia Xavier/Nano

local.conf changes

    TARGET_GCC_VRSION = "8.3.0"
    FLUTTER_CHANNEL = "master"
    IMAGE_INSTALL_append = " flutter-drm-eglstream-backend"
    IMAGE_INSTALL_append = " flutter-gallery"

OR

    TARGET_GCC_VRSION = "8.3.0"
    FLUTTER_CHANNEL = "master"
    CORE_IMAGE_EXTRA_INSTALL += "\
        flutter-drm-eglstream-backend \
        flutter-gallery \
    "

Build EGL image

    bitbake demo-image-egl

Run Flutter application on target (defaults to AOT)

    FLUTTER_DRM_DEVICE=/dev/dri/card0 flutter-drm-eglstream-backend -b /usr/share/flutter-gallery/sony
   

### Sony notes
- not accepting PRs
- no vsync support
- weston >= 8 does not work
- no multi-engine
- no platform view / hybrid composition
- not enough debug spew for debug builds
- code difficult to follow
- too much boiler plate
