#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Google URL launcher Example"
DESCRIPTION = "A Flutter plugin for launching a URL Example"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/packages"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "e73b364bc7556aad4e51b9524816dc29294d8658"
SRC_URI = "git://github.com/flutter/packages.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "url_launcher_example"
FLUTTER_APPLICATION_PATH = "packages/url_launcher/url_launcher/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
