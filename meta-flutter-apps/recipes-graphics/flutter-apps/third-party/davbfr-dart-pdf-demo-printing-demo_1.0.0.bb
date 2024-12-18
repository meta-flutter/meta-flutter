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

SRCREV = "7e4cc457149ab2a5803446a217cb06ce12f13aca"
SRC_URI = "git://github.com/DavBfr/dart_pdf.git;lfs=0;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "printing_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "davbfr-dart-pdf-demo-printing-demo"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "demo"

inherit flutter-app

RDEPENDS:${PN} += " \
    pdfium \
"
