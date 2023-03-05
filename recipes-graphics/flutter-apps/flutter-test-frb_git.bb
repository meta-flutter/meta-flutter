#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Rust Bridge Flutter App Example"
DESCRIPTION = "Flutter Rust Bridge Test"
AUTHOR = "fzyzcjy"
HOMEPAGE = "https://github.com/fzyzcjy/flutter_rust_bridge"
BUGTRACKER = "https://github.com/fzyzcjy/flutter_rust_bridge/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=480e9b5af92d888295493a5cc7f2238e"

RDEPENDS:${PN} += " flutter-rust-bridge-example"

SRCREV = "8c984efe63e0ed306cf2ab497788d4e94392e539"
SRC_URI = "git://github.com/fzyzcjy/flutter_rust_bridge.git;lfs=0;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_rust_bridge_example"
FLUTTER_APPLICATION_PATH = "frb_example/with_flutter"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
