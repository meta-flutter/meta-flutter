#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The ihs_wayland_server example: a Flutter app that starts the embedded
# Wayland server and shows a client toplevel in a platform view. Run it under
# ivi-homescreen; with LAUNCH=<client> in its environment it launches that
# client itself and binds it by activation token.
#
# It takes the library ihs-wl-server installs rather than letting the Dart
# package's build hook run cargo: system_library is set in its pubspec before
# pub resolves (do_ihs_wl_system_library), and the app loads
# libihs_wl_server.so by name.
#

SUMMARY = "ihs_wl_server example app"
DESCRIPTION = "Flutter app that shows Wayland clients inside ivi-homescreen \
platform views through the ihs_wl_server module."
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ihs_wl_server"
BUGTRACKER = "https://github.com/toyota-connected/ihs_wl_server/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1bb8e4c9af5dd10ccb6c8bae63fb4a2d"

# The same commit as ihs-wl-server: the Dart package and the library it loads
# have to agree.
SRC_URI = "git://github.com/toyota-connected/ihs_wl_server.git;protocol=https;branch=main"
SRCREV = "c3c2ecca02b5a6509ee83e08bfb2fc66e7b2d9ff"

PV = "0.1.0+git"

PUBSPEC_APPNAME = "ihs_wayland_server_example"
FLUTTER_APPLICATION_PATH = "dart/ihs_wayland_server/example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "${PN}"

# The example's pubspec.lock is resolved against whatever Flutter the
# upstream developer had; resolve against this SDK instead.
PUBSPEC_IGNORE_LOCKFILE = "1"

RDEPENDS:${PN} += "ihs-wl-server"

inherit flutter-app

python do_ihs_wl_system_library() {
    import os
    pubspec = os.path.join(d.getVar('S'), d.getVar('FLUTTER_APPLICATION_PATH'),
                           'pubspec.yaml')
    with open(pubspec) as f:
        text = f.read()
    if 'system_library:' in text:
        return
    with open(pubspec, 'a') as f:
        f.write('\nhooks:\n'
                '  user_defines:\n'
                '    ihs_wayland_server:\n'
                '      system_library: true\n')
}
# Before pub resolves: an edit afterwards would have it re-resolve, which
# reaches for the network.
addtask ihs_wl_system_library after do_patch before do_archive_pub_cache do_configure
