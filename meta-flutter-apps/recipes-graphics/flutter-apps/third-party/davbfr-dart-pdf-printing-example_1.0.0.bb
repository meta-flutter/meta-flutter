#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "printing_example"
DESCRIPTION = "Pdf Printing Example"
AUTHOR = "David PHAM-VAN"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5335066555b14d832335aa4660d6c376"

SRCREV = "8ff08f8eff877f7562bc286ced8882e3bf13f3f0"
SRC_URI = "git://github.com/DavBfr/dart_pdf.git;lfs=0;branch=master;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "printing_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "davbfr-dart-pdf-printing-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "printing/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    pdfium \
"
