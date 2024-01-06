#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Sentry SDK for C, C++ and native applications."
DESCRIPTION = "Sentry Native SDK"
AUTHOR = "Sentry"
HOMEPAGE = "https://github.com/getsentry/sentry-native"
BUGTRACKER = "https://github.com/getsentry/sentry-native"
SECTION = "devtools"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=16ed57a2dfbb53c2aa8807af4e5d44da"

DEPENDS += "\
    curl \
    "

SRCREV ??= "a3d58622a807b9dda174cb9fc18fa0f98c89d043"
SRC_URI = "gitsm://github.com/getsentry/sentry-native.git;protocol=https;branch=master"

S = "${WORKDIR}/git"

inherit cmake pkgconfig 

EXTRA_OECMAKE += "\
    -D SENTRY_BUILD_TESTS=OFF \
    -D SENTRY_BUILD_EXAMPLES=OFF \
    -D SENTRY_BUILD_FORCE32=OFF \
"
