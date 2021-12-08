SUMMARY = "Baseline Flutter Weston Image for Release"

LICENSE = "MIT"

require flutter-image-gl-drm.inc

CORE_IMAGE_BASE_INSTALL:append = " \
    flutter-pi-release \
    flutter-gallery-release \
    "
