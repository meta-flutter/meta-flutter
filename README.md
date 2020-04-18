# meta-flutter

Yocto Layer for Flutter Engine


### Project Status

Running on a Raspberry Pi 3 64-bit.  Should build/run on any Aarch64 target.  

After sorting out ARM patch, my next effort will be an implementation for the wlroots compositor.


![Running in Weston](flutter-rpi3-64.jpg)


## Overview

This layer includes everything to build:
    
    Flutter Engine for Aarch64
    Wayland Flutter Shell
    Flutter Gallery Demo

### depot_tools
### flutter-engine
flutter-engine depends on depot_tools-native.  Wayland is not required to build.

### pugixml
### waylandpp
waylandpp-native is dependent on pugixml

### futter-wayland
depends on waylandpp

### flutter-sdk
### flutter-gallery
depdends on SDK.  Provides pattern to follow for the remaining examples.

The Flutter engine build uses the GN meta-build system in conjunction with Chrome Infrastructure Package Deployment (CIPD).

The build uses depot-tools to sync repos, and CI packages.  A pre-built Clang toolchain is downloaded as part of the build.


## Layers used in development

    meta
    meta-poky
    meta-oe
    meta-multimedia
    meta-networking
    meta-perl
    meta-python
    meta-raspberrypi
    meta-flutter


## Development image

This workspace can be used for any Raspberry Pi machine type:
https://github.com/jwinarske/manifests/blob/zeus/rpi64-flutter.xml
