# meta-flutter

Yocto Layer for Flutter related artifacts.

Recommended development flow starts with making an embedder run on desktop, then target.  This approach will save you a good deal of time and aggrevation.

Note: In theory Swift Shader (CPU render) engine builds should work with the right build flags.  Be warned it won't work out of the box.  Select a SoC with a GPU that support OpenGL 3.0+ and save yourself the Engineering NRE.

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

Targets known to work
* DragonBoard 410c - aarch64
* Intel MinnowBoard Max (BayTrail) - intel-icore7-64
* NVidia Nano Dev Kit - aarch64
* NVidia Xavier NX Dev Kit - aarch64
* Raspberry Pi 3 - armv7hf
* Raspberry Pi 4 / Compute - aarch64
* Renesas R-Car m3ulcb - aarch64
* etc


NVidia local.conf addition to include Flutter Engine, Flutter Gallery App, Sony DRM eglstream embedder

```
TARGET_GCC_VRSION = "8.3.0"
FLUTTER_CHANNEL = "master"
IMAGE_INSTALL += "\
    flutter-drm-eglstream-backend \
    flutter-gallery \
"
```
    
Sony notes
- not accepting PRs
- weston >= 8 does not work
- no multi-engine
- no platform view / hybrid composition
- no vsync support
- not enough debug spew for debug builds
- too much boiler plate - my opinion

