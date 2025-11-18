#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Sentry SDK for C, C++ and native applications."
DESCRIPTION = "Sentry Native SDK"
AUTHOR = "Sentry"
HOMEPAGE = "https://github.com/getsentry/sentry-native"
BUGTRACKER = "https://github.com/getsentry/sentry-native/issues"
SECTION = "devtools"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=16ed57a2dfbb53c2aa8807af4e5d44da"

DEPENDS += "\
    curl \
    "

SRCREV ??= "6129d36d717b77d53c8af8fe439ed0370fb63ea4"
SRC_URI = " \
    gitsm://github.com/getsentry/sentry-native.git;protocol=https;branch=master \
    file://0001-version-SO.patch \
"

S = "${WORKDIR}/git"

inherit cmake pkgconfig 

EXTRA_OECMAKE += "\
    -D SENTRY_BUILD_TESTS=OFF \
    -D SENTRY_BUILD_EXAMPLES=OFF \
    -D SENTRY_BUILD_FORCE32=OFF \
"
