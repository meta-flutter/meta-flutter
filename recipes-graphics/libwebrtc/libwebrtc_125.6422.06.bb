#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "libwebrtc"
DESCRIPTION = "A C++ wrapper for binary release, mainly used for \
               flutter-webrtc desktop (windows, linux, embedded)."
AUTHOR = "webrtc team"
HOMEPAGE = "https://github.com/webrtc-sdk/libwebrtc"
BUGTRACKER = "https://github.com/webrtc-sdk/libwebrtc/issues/list"
SECTION = "graphics"
LICENSE = "BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=ad296492125bc71530d06234d9bfebe0 \
    file://libwebrtc/LICENSE;md5=166d54ea842ed1a582dabbd844fa4c80 \
    "

DEPENDS += "\
    glib-2.0 \
    gtk+3 \
    pipewire \
    "

SRCREV = "543121ba1cd47780e92d48546b880333265b37b5"
SRC_URI = "\
    gn://github.com/webrtc-sdk/webrtc.git;gn_name=src \
    git://github.com/webrtc-sdk/libwebrtc.git;;protocol=https;lfs=0;branch=main;destsuffix=src/libwebrtc;name=libwebrtc \
    file://add-libwebrtc-target.patch \
    file://toolchain.gn.in \
    "

SRCREV_FORMAT .= "_libwebrtc"
SRCREV_libwebrtc = "a6522062f83cac380a4544e036114015cf061ffa"

S = "${WORKDIR}/src"
B = "${WORKDIR}/src/out/Linux-${GN_TARGET_ARCH_NAME}"

inherit gn-fetcher pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

GN_ARGS = '\
    target_os=\"linux\" \
    target_cpu=\"${GN_TARGET_ARCH_NAME}\" \
    target_triple=\"${TARGET_SYS}\" \
    target_sysroot=\"${STAGING_DIR_TARGET}\" \
    is_debug=false \
    rtc_include_tests=false \
    rtc_use_h264=true \
    ffmpeg_branding=\"Chrome\" \
    is_component_build=false \
    use_rtti=true \
    use_custom_libcxx=true \
    rtc_enable_protobuf=false \
    ozone_auto_platforms=false \
    ozone_platform_wayland=true \
'

do_configure() {

    # stage runtime binaries for linking
    cd ${STAGING_DIR_TARGET}/usr/lib

    test -e crtbeginS.o && rm crtbeginS.o
    test -e crtendS.o && rm crtendS.o
    test -e libgcc.a && rm libgcc.a

    ln -s "$(find -iname crtbeginS.o)" crtbeginS.o
    ln -s "$(find -iname crtendS.o)" crtendS.o
    ln -s "$(find -iname libgcc.a)" libgcc.a

    cd ${S}

    #
    # configure toolchain file
    #

    cp ${WORKDIR}/toolchain.gn.in ${S}/build/toolchain/linux/BUILD.gn

    sed -i "s|@GN_TARGET_ARCH_NAME@|${GN_TARGET_ARCH_NAME}|g" ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@TARGET_SYS@|${TARGET_SYS}|g"                   ${S}/build/toolchain/linux/BUILD.gn

    gn gen "${B}" --args="${GN_ARGS}"
}

do_compile() {
    ninja -C ${B} libwebrtc $PARALLEL_MAKE
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    install -d ${D}${libdir}
    install -m 0755 ${B}/libwebrtc.so ${D}${libdir}/libwebrtc.so.${PV}

    cd ${D}${libdir}
    ln -sf libwebrtc.so.${PV} libwebrtc.so.125
    ln -sf libwebrtc.so.125 libwebrtc.so

    install -d ${D}${includedir}/libwebrtc
    cp -R ${S}/libwebrtc/include/* ${D}${includedir}/libwebrtc
}
