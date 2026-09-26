#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# ihs_wl_server: an embedded Wayland server (smithay) that ivi-homescreen apps
# load over Dart FFI to show Wayland clients as platform views.
#
# Built here rather than by the ihs_wayland_server Dart package's build hook,
# which would run cargo inside `flutter build`, outside the cross environment.
# An app in the image takes this library instead by setting, in its pubspec:
#
#   hooks:
#     user_defines:
#       ihs_wayland_server:
#         system_library: true
#
# and then loads libihs_wl_server.so by name.
#
# scarthgap's own Rust (1.75.0) cannot build smithay, so this recipe is only
# seen with meta-lts-mixins scarthgap/rust (1.98.1). bindgen also needs
# libclang, which scarthgap gets from meta-clang.
#

SUMMARY = "Embedded Wayland server for ivi-homescreen platform views"
DESCRIPTION = "Wayland server (smithay) loaded by a Flutter app on ivi-homescreen \
over Dart FFI. Each client toplevel is shown as a platform view, its buffers \
handed to the shell as zero-copy dma-buf layers."
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ihs_wl_server"
BUGTRACKER = "https://github.com/toyota-connected/ihs_wl_server/issues"
SECTION = "graphics"

# The library itself is Apache-2.0; the crates linked into it are MIT or dual
# MIT/Apache-2.0, and egl-wl-display adds libloading (ISC).
LICENSE = "Apache-2.0 & MIT${@bb.utils.contains('PACKAGECONFIG', 'egl-wl-display', ' & ISC', '', d)}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1bb8e4c9af5dd10ccb6c8bae63fb4a2d"

# smithay comes from a fork until Smithay/smithay#2183 is released (the crate's
# [patch.crates-io]). Its name and destsuffix have cargo_common drop the git
# sources from Cargo.lock; ihs_wl_patch_smithay below points the patch at it.
SRC_URI = "\
    git://github.com/toyota-connected/ihs_wl_server.git;protocol=https;branch=main \
    git://github.com/jwinarske/smithay;protocol=https;nobranch=1;name=smithay;destsuffix=smithay \
"
SRCREV = "6bab94d388d26fa370968704edb565c2fb48979d"
SRCREV_smithay = "e722af8222c1f9f81942af5c8cbeeb9387bf431f"
SRCREV_FORMAT = "default_smithay"

PV = "0.1.0+git"

# The git fetcher unpacks to ${WORKDIR}/git on this release.
S = "${WORKDIR}/git"

require ${BPN}-crates.inc

inherit cargo cargo-update-recipe-crates pkgconfig

# ivi-homescreen-shared: the platform-view ABI (1.16 or newer), through
# ivi-homescreen-shared.pc; bindgen reads its headers, so libclang too.
DEPENDS += "\
    clang-native \
    ivi-homescreen-shared \
    libdrm \
    libxkbcommon \
    virtual/libgbm \
"

# The fork enters through the crate's own [patch.crates-io], which the
# [patch."<git url>"] cargo_common writes does not reach: a patch does not
# apply to another patch. A crates-io [patch] in the cargo config overrides the
# manifest's.
python ihs_wl_patch_smithay() {
    import os
    unpackdir = d.getVar("UNPACKDIR") or d.getVar("WORKDIR")
    config = os.path.join(d.getVar("CARGO_HOME"), "config.toml")
    with open(config, "a") as f:
        f.write('\n[patch.crates-io]\nsmithay = { path = "%s" }\n'
                % os.path.join(unpackdir, "smithay"))
}
do_configure[postfuncs] += "ihs_wl_patch_smithay"

export LIBCLANG_PATH = "${STAGING_LIBDIR_NATIVE}"
export BINDGEN_EXTRA_CLANG_ARGS = "--sysroot=${STAGING_DIR_TARGET} ${TUNE_CCARGS}"

# egl-wl-display: serve Wayland on libwayland-server and bind an EGL display to
# it (EGL_WL_bind_wayland_display), for EGL implementations whose clients
# share buffers over a protocol of their own. libEGL is loaded at run time.
PACKAGECONFIG ??= ""
PACKAGECONFIG[egl-wl-display] = "--features egl-wl-display,,wayland"

# This release's cargo class does not pass PACKAGECONFIG_CONFARGS to cargo.
CARGO_BUILD_FLAGS:append = " ${PACKAGECONFIG_CONFARGS}"

python () {
    if 'clang-layer' not in (d.getVar('BBFILE_COLLECTIONS') or '').split():
        raise bb.parse.SkipRecipe("needs meta-clang (clang-layer) for bindgen's libclang")
}

# A cdylib loaded by name, with no SONAME to version: the .so is the runtime
# library, not a -dev link.
do_install() {
    install -d ${D}${libdir} ${D}${includedir}
    install -m 0755 ${B}/target/${CARGO_TARGET_SUBDIR}/libihs_wl_server.so ${D}${libdir}/
    install -m 0644 ${S}/include/ihs_wl.h ${D}${includedir}/
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/libihs_wl_server.so"
INSANE_SKIP:${PN} += "dev-so"
