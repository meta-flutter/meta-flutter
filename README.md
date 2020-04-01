# meta-flutter

Yocto Layer for Flutter Engine


### Project Status


![Running in Weston](flutter-rpi3-64.jpg)


### Overview

The Flutter engine build uses the GN meta-build system in conjunction with Chrome Infrastructure Package Deployment (CIPD).

depot-tools-native is a depdenency.
curl-native override adding a cert store to the sysroot is also a dependency.  CIPD is used to download large binary payloads, such as the clang toolchain packge.

A build system, within a build system.


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

https://github.com/jwinarske/manifests/blob/zeus/rpi64-flutter.xml
