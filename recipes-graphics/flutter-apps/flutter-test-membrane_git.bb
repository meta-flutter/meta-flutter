#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Membrane Test"
DESCRIPTION = "Flutter Membrane Test"
AUTHOR = "Jerel Unruh"
HOMEPAGE = "https://github.com/jerel/membrane_template"
BUGTRACKER = "https://github.com/jerel/membrane_template/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=47da47e9ceb852b136a5566db4ebec53"

RDEPENDS:${PN} += " membrane-example"

SRCREV = "402133efc123fbf86997118209b3a84007e4000d"
SRC_URI = "git://github.com/jerel/membrane_template.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "membrane_example"
FLUTTER_APPLICATION_PATH = "flutter_example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

do_compile:prepend() {

    sed -i "s/name: flutter_example/name: ${PUBSPEC_APPNAME}/g" flutter_example/pubspec.yaml
}

inherit flutter-app
