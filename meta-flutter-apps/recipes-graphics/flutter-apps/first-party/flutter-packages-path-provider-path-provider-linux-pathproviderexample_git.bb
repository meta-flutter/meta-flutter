#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "pathproviderexample"
DESCRIPTION = "Demonstrates how to use the path_provider_linux plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "b33d9334f58b1cb6dfc1d6a46deb1c24f4a6425a"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "pathproviderexample"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-path-provider-path-provider-linux-pathproviderexample"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/path_provider/path_provider_linux/example"

inherit flutter-app
