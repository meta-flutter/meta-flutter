#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
# Copyright (c) 2026 Ahmed Wafdy. All rights reserved.
#
SUMMARY = "flathub_catalog"
DESCRIPTION = "Flutter Linux desktop app demonstrating the appstream_dart package."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/flatpak-minimal/appstream_dart"
BUGTRACKER = "https://github.com/flatpak-minimal/appstream_dart/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=641bdc36389b26ea9787acb6844e4b22"

SRCREV = "c44e741569f4fb5d6f9dd5bdc6960d91314b174c"
SRC_URI = "gitsm://github.com/flatpak-minimal/appstream_dart.git;branch=main;protocol=https"

# appstream_core links sqlite3.
DEPENDS += "sqlite3"

FLUTTER_APPLICATION_PATH = "example/flathub_catalog"
PUBSPEC_APPNAME = "flathub_catalog"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "appstream-dart-example-flathub-catalog"

inherit flutter-app-native

# The sqlite3 Dart package (via drift) otherwise downloads a prebuilt
# libsqlite3 from GitHub during `flutter build`, which fails in the
# network-isolated do_compile. Have its build hook load the image's library
# instead, by absolute path so no unversioned symlink is needed in any mode.
RDEPENDS:${PN}:append = " libsqlite3"
FLUTTER_NATIVE_USER_DEFINES = '{"sqlite3": {"source": "system", "name": "${libdir}/libsqlite3.so.0"}}'
