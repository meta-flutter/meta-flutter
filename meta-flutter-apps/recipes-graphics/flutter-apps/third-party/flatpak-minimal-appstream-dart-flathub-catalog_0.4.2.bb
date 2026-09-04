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

SRCREV = "486c103fe5a0accec2bba2b4952387e671c5691e"
SRC_URI = "gitsm://github.com/flatpak-minimal/appstream_dart.git;branch=main;protocol=https"

# appstream_core links sqlite3.
DEPENDS += "sqlite3"

FLUTTER_APPLICATION_PATH = "example/flathub_catalog"
PUBSPEC_APPNAME = "flathub_catalog"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "appstream-dart-example-flathub-catalog"

inherit flutter-app-native

# Vendored pub cache: dependencies arrive through SRC_URI with checksums
# rather than a networked `pub get` during the build. See
# tools/pubvendor/README.md.
FILESEXTRAPATHS:prepend := "${THISDIR}:"
SRC_URI += "file://flatpak-minimal-appstream-dart-flathub-catalog-pubspec.lock"
require flatpak-minimal-appstream-dart-flathub-catalog-pubcache.inc
inherit pub-cache
PUBSPEC_APP_DIR = "${S}/${FLUTTER_APPLICATION_PATH}"
PUBSPEC_LOCK_FILE = "flatpak-minimal-appstream-dart-flathub-catalog-pubspec.lock"

# The sqlite3 Dart package (via drift) otherwise downloads a prebuilt
# libsqlite3 from GitHub during `flutter build`, which fails in the
# network-isolated do_compile. Tell its build hook to resolve sqlite3 from the
# system library instead (DynamicLoadingSystem -> dlopen libsqlite3.so at
# runtime). user_defines are read from the app being built and must live in its
# pubspec.yaml -- pubspec_overrides.yaml only accepts dependency_overrides /
# resolution / workspace, so append the hooks section to pubspec.yaml itself.
#
# Run as a dedicated task between do_patch and do_archive_pub_cache so the edit
# is in place before the first, network-enabled dependency resolution. Editing
# pubspec.yaml afterwards makes the offline pub get in do_compile re-resolve,
# which tries to fetch security advisories and fails with no network. The added
# hooks section carries no dependency change, so the lockfile is unaffected.
do_inject_user_defines() {
    # Remove any pubspec_overrides.yaml a prior build left behind: pub rejects a
    # `hooks:` section there, and a stale one breaks `flutter pub get`.
    rm -f ${S}/${FLUTTER_APPLICATION_PATH}/pubspec_overrides.yaml
    if ! grep -q '^hooks:' ${S}/${FLUTTER_APPLICATION_PATH}/pubspec.yaml; then
        cat >> ${S}/${FLUTTER_APPLICATION_PATH}/pubspec.yaml <<'HOOKS_EOF'

hooks:
  user_defines:
    sqlite3:
      source: system
HOOKS_EOF
    fi
}
addtask inject_user_defines after do_patch before do_archive_pub_cache
do_inject_user_defines[dirs] = "${S}"

# libsqlite3.so is resolved from the system at runtime, so it must be on the
# image, and Dart looks for the unversioned name.
RDEPENDS:${PN} += "libsqlite3"

do_install:append() {
    ln -sf ${libdir}/libsqlite3.so.0 \
        ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libsqlite3.so
}
