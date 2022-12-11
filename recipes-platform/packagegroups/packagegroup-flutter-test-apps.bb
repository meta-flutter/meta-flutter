SUMMARY = "Package of Flutter Test Apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-gallery \
    flutter-test-frb \
    flutter-test-membrane \
    flutter-test-animated-background \
    flutter-test-membrane \
    flutter-test-plugins \
    flutter-test-secure-storage \
    flutter-test-texture-egl \
    flutter-test-video-player \
    flutter-test-localization \
"

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"
