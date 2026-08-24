#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
SUMMARY = "flutter_remote_manager"
DESCRIPTION = "Flutter Linux desktop app demonstrating the flatpak_dart package."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/flatpak-minimal/flatpak_dart"
BUGTRACKER = "https://github.com/flatpak-minimal/flatpak_dart/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2ae04493455a6ed09a6df86162e3cbb4"

SRCREV = "1b31c7ef6ba4b8f29cd26382f9c66a348544c115"
SRC_URI = "git://github.com/flatpak-minimal/flatpak_dart.git;branch=main;protocol=https"

# native/CMakeLists.txt: pkg_check_modules(flatpak, glib-2.0), both REQUIRED.
DEPENDS += "\
    flatpak \
    glib-2.0 \
"

FLUTTER_APPLICATION_PATH = "example/flutter_remote_manager"
PUBSPEC_APPNAME = "flutter_remote_manager"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flatpak-dart-example-flutter-remote-manager"

# flatpak itself declares REQUIRED_DISTRO_FEATURES = "polkit"; declare it here
# too so this app is skipped with a clear reason on a distro without polkit,
# rather than failing with "Nothing PROVIDES 'flatpak'".
REQUIRED_DISTRO_FEATURES = "polkit"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit features_check flutter-app-native

RDEPENDS:${PN} += "flatpak"
