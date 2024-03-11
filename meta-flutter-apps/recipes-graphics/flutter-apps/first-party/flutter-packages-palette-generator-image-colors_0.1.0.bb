#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "image_colors"
DESCRIPTION = "A simple example of how to use the PaletteGenerator to load the palette from an image file."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "1e46a6a2fb31ed8c45159d7a41fdd4ea6ea595c7"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "image_colors"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-palette-generator-image-colors"
FLUTTER_APPLICATION_PATH = "packages/palette_generator/example"

inherit flutter-app
