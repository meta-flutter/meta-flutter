#
# Copyright (c) 2020-2026 Joel Winarske. All rights reserved.
#
# Shared body for the ivi-homescreen v3.0 integration test applications, which
# live under test/integration in the embedder repository.
#
# Each app drives one embedder subsystem -- keyboard, multi-touch, multi-view,
# watchdog, OSGi, MCP -- through its platform channels, so an app is only
# meaningful against the embedder it was written for. They pin the same revision
# as the v3 embedder recipes rather than following the branch: HOMESCREEN_COMMIT
# is the one knob for both, and a mismatched pair tests nothing useful.
#
# test/integration also holds app-test, flutter_view-test and texture-test.
# Those are C++ sources built by the embedder's own CMake, not Flutter
# applications, so they have no recipe here.
#
# Consumers set PUBSPEC_APPNAME; the application path follows from it.
#

AUTHOR = "Toyota Connected North America"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

HOMESCREEN_COMMIT ??= "b869f3e973299db8e8fe771fb0b6d0d7d63c99d3"
SRCREV = "${HOMESCREEN_COMMIT}"

# Plain git rather than the embedder's gitsm. The apps are pure Dart and none of
# the submodules contribute to a bundle, so cloning them would only cost time.
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=v3.0"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

FLUTTER_APPLICATION_PATH = "test/integration/${PUBSPEC_APPNAME}"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "${PN}"

inherit flutter-app
