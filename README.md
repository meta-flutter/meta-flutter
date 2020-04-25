# meta-flutter

Yocto Layer for Flutter Engine


### Project Status

Building Engine master for 32-bit and 64-bit.
Engine pulls in Clang 11.0.  This is used for both build flavors.

Gallery validated on Raspberry Pi 3 64-bit.  To get text to show up properly, not only do you need the fonts installed on the target, but the engine needs to be built with fontconfig enabled.

*Next Up*

* wayland client on top of the wlroots compositor

* client for https://wiki.automotivelinux.org/subsystem/graphics/weston-ivi-shell



rpi3-64 running the Flutter Gallery App

![Running in Weston](flutter-rpi3-64.jpg)


## Overview

This layer includes everything to build:
    
    Flutter Engine for armv7/aarch64/x86/x86_64
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

Platform Channel Callbacks

    Accessibility - stubbed
    Platform Channel - stubbed
    Text Input - stubbed
    Platform Views - stubbed
    URL Launcher - complete
    Connectivity / Status - stubbed
    Video Player / Events - stubbed
GStreamer playback to Texture via DBUS control is my current plan.

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

This workspace can be used for *any* Raspberry Pi machine type
https://github.com/jwinarske/manifests/blob/zeus/rpi64-flutter.xml
