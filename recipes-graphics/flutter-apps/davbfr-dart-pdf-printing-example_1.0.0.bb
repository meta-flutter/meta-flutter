#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "printing_example"
DESCRIPTION = "Pdf Printing Example"
AUTHOR = ""
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "19d9f4bf6e5dc69e53799681c062970f62b3eef9"
SRC_URI = "git://github.com/DavBfr/dart_pdf.git;lfs=0;branch=master;protocol=https;destsuffix=git \
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "printing_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "davbfr-dart-pdf-printing-example"
FLUTTER_APPLICATION_PATH = "printing/example"

inherit flutter-app
