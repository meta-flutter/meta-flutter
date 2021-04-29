# meta-flutter

Yocto Layer for Flutter Engine


## Overview

This layer includes recipes to build:

    flutter-engine (channel selection, default is beta)
    flutter-pi (DRM w/VSync)
    flutter-wayland (basic POC)
        waylandpp/ipugxml
    Sony embedders (No VSync, less x11, and EGL streaming)

## Notes
    Sony summary
    - not accepting PRs
    - weston >= 8 does not work
    - no multi-engine
    - no platform view / hybrid composition
    - no vsync support
    - lots of messy boiler plate
    
## Layers dependencies

    * layers to build DRM, KMS, or Wayland based image
    meta-clang
