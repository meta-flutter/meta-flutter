#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "file_selector_linux_example"
DESCRIPTION = "Local testbed for Linux file_selector implementation."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "1e46a6a2fb31ed8c45159d7a41fdd4ea6ea595c7"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "file_selector_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-file-selector-file-selector-linux-file-selector-linux-example"
FLUTTER_APPLICATION_PATH = "packages/file_selector/file_selector_linux/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    zenity \
"
