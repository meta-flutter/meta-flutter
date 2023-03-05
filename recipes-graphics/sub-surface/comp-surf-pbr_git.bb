#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Compositor Surface Static Plugin - PBR"
DESCRIPTION = "Compositor Surface PBR"
AUTHOR = "joel.winarske@gmail.com"
HOMEPAGE = "https://github.com/meta-flutter/comp_surf_pbr"
BUGTRACKER = "https://github.com/meta-flutter/comp_surf_pbr/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "\
    file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
"

DEPENDS += "\
    compiler-rt \
    libcxx \
    vulkan-loader \
    "

REQUIRED_DISTRO_FEATURES = "vulkan wayland"

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

SRCREV ??= "08e04ea6ee4e30054b2fff6fcfd34ae86aefd03e"
SRC_URI = "gitsm://github.com/meta-flutter/comp_surf_pbr.git;protocol=https;lfs=0;nobranch=1"

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig

PACKAGECONFIG ??= "wayland"

PACKAGECONFIG[wayland] = "-DUSE_WAYLAND_WSI=ON, -DUSE_WAYLAND_WSI=OFF, wayland wayland-native wayland-protocols"
PACKAGECONFIG[d2d] = "-DUSE_D2D_WSI=ON, -DUSE_D2D_WSI=OFF"
PACKAGECONFIG[x11] = "-DUSE_XCB_WSI=ON, -DUSE_XCB_WSI=OFF"
PACKAGECONFIG[imgui] = "-DENABLE_IMGUI=ON, -DENABLE_IMGUI=OFF"

EXTRA_OECMAKE += "-D RUNTIME_ASSET_PATH=${datadir}/comp_surf_pbr/assets/"

do_install:prepend() {
    touch "${S}/include/ktx.h"
    touch "${S}/include/ktxvulkan.h"
}

FILES:${PN} += "\
    ${datadir} \
    ${libdir}/libcomp_surf_pbr.so \
"

FILES:${PN}-dev += "\
    ${libdir}/libktx.a \
    ${bindir}/draco_decoder* \
    ${bindir}/draco_encoder* \
"