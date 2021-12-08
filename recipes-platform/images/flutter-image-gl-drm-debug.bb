SUMMARY = "Baseline Flutter Weston Image for Development"

LICENSE = "MIT"

require flutter-image-gl-drm.inc

CORE_IMAGE_BASE_INSTALL:append = " \
    flutter-pi-debug \
    flutter-gallery-debug \
    "
