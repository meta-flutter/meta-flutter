SUMMARY = "Baseline Flutter Weston Image for Profiling"

LICENSE = "MIT"

require flutter-image-gl-drm.inc

CORE_IMAGE_BASE_INSTALL:append = " \
    flutter-pi-profile \
    flutter-gallery-profile \
    "
