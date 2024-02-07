#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "printing_demo"
DESCRIPTION = "Pdf Printing Demo"
AUTHOR = "David PHAM-VAN"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5335066555b14d832335aa4660d6c376"

SRCREV = "19d9f4bf6e5dc69e53799681c062970f62b3eef9"
SRC_URI = "\
    git://github.com/DavBfr/dart_pdf.git;lfs=0;branch=master;protocol=https;destsuffix=git \
    http://pigment.github.io/fake-logos/logos/vector/color/auto-speed.svg;subdir=demo/assets/;downloadfilename=logo.svg;name=logo \
    https://www.fakepersongenerator.com/Face/female/female20151024334209870.jpg;subdir=demo/assets/;downloadfilename=profile.jpg;name=profile \
"
SRC_URI[logo.sha256sum] = "9717756f813692f1ac2c5b1d3d813f2b239075355379223bfb45618c664d0f28"
SRC_URI[profile.sha256sum] = "40638e9fad1e2b372420651a8ac41ffa7864e87672a884a85cea1d62514a4982"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "printing_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "davbfr-dart-pdf-demo"
FLUTTER_APPLICATION_PATH = "demo"

inherit flutter-app
