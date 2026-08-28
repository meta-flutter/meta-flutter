#
# Copyright (c) 2024-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "libwebrtc"
DESCRIPTION = "A C++/C-ABI wrapper over WebRTC, used by flutter-webrtc desktop \
               and by ivi-homescreen's WebRTC send consumer and webrtc plugin."
AUTHOR = "webrtc team"
HOMEPAGE = "https://github.com/jwinarske/libwebrtc"
BUGTRACKER = "https://github.com/jwinarske/libwebrtc/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause AND MIT"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=ad296492125bc71530d06234d9bfebe0 \
    file://libwebrtc/LICENSE;md5=166d54ea842ed1a582dabbd844fa4c80 \
    "

# alsa-lib and pulseaudio are UNCONDITIONAL build dependencies, whatever the
# audio PACKAGECONFIG says: modules/audio_device/BUILD.gn adds the ALSA and
# PulseAudio sources unconditionally on Linux, and rtc_include_pulse_audio only
# gates the WEBRTC_ENABLE_LINUX_PULSE *define*, not the sources. Both libraries
# are dlopened at run time (alsasymboltable_linux.cc / pulseaudiosymboltable_
# linux.cc), so they never show up in DT_NEEDED -- only their headers are needed
# here. alsa-lib used to arrive transitively via pulseaudio; declare it.
DEPENDS += "\
    alsa-lib \
    glib-2.0 \
    gtk+3 \
    pulseaudio \
    patchelf-native \
    "

DEPENDS:append:x86-64 = " nasm-native"

#
# WebRTC milestone.
#
# The wrapper is milestone-coupled: it carries a per-milestone
# patches/custom_audio_source_m<NN>.patch that must exist for the branch pinned
# here. libwebrtc currently tops out at m144 (newest tag libwebrtc.m144.7559.09,
# newest patch custom_audio_source_m144.patch), so m144_release is the newest
# combination that builds.
#
# WEBRTC_SRCREV is pinned to the revision the local webrtc-build tree was built
# against (/mnt/dev/webrtc-build/src, m144_release @ 08b94087, 2026-07-16) rather
# than to the branch tip, so the Yocto build matches the locally built library.
#
# To move to tip of tree once libwebrtc gains a matching milestone, set:
#   WEBRTC_BRANCH = "m150_release"          (repo default branch; `master` also exists)
#   WEBRTC_SRCREV = "<rev of that branch>"
#   WEBRTC_AUDIO_PATCH = "custom_audio_source_m150.patch"
# and bump LIBWEBRTC_SRCREV to the matching libwebrtc tag.
#
WEBRTC_BRANCH ??= "m144_release"
WEBRTC_SRCREV ??= "08b9408717876048bca582530ba6f3e732a02479"
WEBRTC_AUDIO_PATCH ??= "custom_audio_source_m144.patch"
LIBWEBRTC_SRCREV ??= "8b79990c08bc5600ad3573fcb6ebf88117a797be"

# v4l2-webrtc-codec is absorbed into libwebrtc.so as a gn source_set. It must
# land as a sibling of src/libwebrtc inside the webrtc checkout, because
# libwebrtc/BUILD.gn refers to it by the absolute gn label //v4l2-webrtc-codec
# (see the lw_enable_v4l2_codec block). Pinned to the revision the local
# flutter-webrtc working tree uses.
V4L2WC_SRCREV ??= "08bddc2b63e709c3419e40d6d2ed6599e0f412be"
V4L2WC_SRC_URI = "git://github.com/jwinarske/v4l2-webrtc-codec.git;protocol=https;branch=main;destsuffix=src/src/v4l2-webrtc-codec;name=v4l2wc"

SRCREV = "${WEBRTC_SRCREV}"
# Layout note: the gn fetcher runs `gclient config` with the solution named by
# gn_name inside gclientdir, so gclient checks the tree out at
# <gclientdir>/<gn_name> -- i.e. ${UNPACKDIR}/src/src, one level below the
# directory the tarball unpacks into. ${UNPACKDIR}/src itself holds only the
# pre-sync shallow clone; the DEPS (build/, third_party/, tools/, buildtools/)
# exist solely under the solution dir. S must therefore be ${UNPACKDIR}/src/src,
# and the sibling checkouts have to land beside the DEPS, not beside the husk.
SRC_URI = "\
    gn://github.com/webrtc-sdk/webrtc.git;gn_name=src;branch=${WEBRTC_BRANCH};destdir=${UNPACKDIR}/src;gclientdir=${UNPACKDIR}/src \
    git://github.com/jwinarske/libwebrtc.git;protocol=https;lfs=0;branch=main;destsuffix=src/src/libwebrtc;name=libwebrtc \
    ${@bb.utils.contains('PACKAGECONFIG', 'v4l2-codec', d.getVar('V4L2WC_SRC_URI'), '', d)} \
    file://toolchain.gn.in \
    "

SRCREV_FORMAT .= "_libwebrtc"
SRCREV_FORMAT .= "${@bb.utils.contains('PACKAGECONFIG', 'v4l2-codec', '_v4l2wc', '', d)}"
SRCREV_libwebrtc = "${LIBWEBRTC_SRCREV}"
SRCREV_v4l2wc = "${V4L2WC_SRCREV}"

S = "${UNPACKDIR}/src/src"
B = "${S}/out/Linux-${GN_TARGET_ARCH_NAME}"

inherit gn-fetcher pkgconfig

# gn writes its output inside the sync directory; keep it out of the
# cached tarball, which is also what a mirror would serve.
GN_PACK_EXCLUDES = "./src/out"

require conf/include/gn-utils.inc

EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

#
# desktop-capture builds libwebrtc's screen/window capture surface. It is NOT
# independent of pipewire: rtc_desktop_capturer_impl.cc calls
# DesktopCaptureOptions::set_allow_pipewire(), which webrtc only declares when
# built with rtc_use_pipewire=true. So enabling it requires 'pipewire' too, and
# the pair drags in meta-multimedia (plus its meta-python dependency).
#
# Both default ON, matching the local desktop build
# (/mnt/dev/webrtc-build/src/out-x64-release/args.gn). This is not merely a
# preference: desktop capture adds virtual methods to libwebrtc's public
# interfaces, so a consumer compiled without RTC_DESKTOP_DEVICE gets a
# mismatched vtable layout. ihs_webrtc_view/native/CMakeLists.txt calls this out
# explicitly for its presenter_live target. Turning them off produces a library
# that links but misbehaves at runtime against such a consumer.
#
# v4l2-codec absorbs the v4l2-webrtc-codec hardware H.264 decoder into
# libwebrtc.so. The decoder is still selected at runtime only when LW_V4L2 is
# set, so enabling it does not change the default code path.
#
# Audio backends follow DISTRO_FEATURES rather than being hardcoded. 'pulseaudio'
# is a DISTRO_FEATURE oe-core ships by default; 'pipewire' is a de-facto one --
# oe-core's libsdl2 and meta-oe/meta-multimedia's libsdl3, mpv, fluidsynth and
# openal-soft all honour it, but it is absent from DISTRO_FEATURES_DEFAULT, so a
# distro opts in. They are not mutually exclusive at build time: pipewire's own
# recipe build-depends on pulseaudio for libpulse while RRECOMMENDing its
# pipewire-pulse drop-in replacement daemon. The exclusivity is a runtime
# property, resolved by packaging, not by DEPENDS.
#
# desktop-capture is tied to pipewire (see the vtable note above), so it is only
# defaulted on where the distro actually has pipewire. It stays optional -- an
# image with no screen-sharing use case can drop it with
#   PACKAGECONFIG:remove:pn-libwebrtc = "desktop-capture"
# which also sheds the X11/gbm/drm capture tail. Do so deliberately though: the
# define is part of the public ABI, so EVERY consumer in that image must be
# built the same way. Mixing is not a link error, it is a silently wrong vtable.
# There is no wayland gn arg: rtc_use_pipewire IS the Wayland capture backend
# (modules/desktop_capture/linux/wayland/* is gated on it), because Wayland has
# no screen-grab API and capture goes through xdg-desktop-portal + PipeWire.
# rtc_use_x11 is the parallel X11 backend. So when the distro has both, prefer
# Wayland and leave the libX* tail out entirely.
PACKAGECONFIG ??= "\
    ${@bb.utils.filter('DISTRO_FEATURES', 'pulseaudio pipewire', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', '', bb.utils.filter('DISTRO_FEATURES', 'x11', d), d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'pipewire', 'desktop-capture', '', d)} \
"
PACKAGECONFIG[pulseaudio] = "rtc_include_pulse_audio=true,rtc_include_pulse_audio=false"
# X11 desktop capture backend (Xcomposite/Xdamage/Xfixes/Xrandr). Enabled only
# when the distro has x11 AND not wayland -- see the PACKAGECONFIG default.
PACKAGECONFIG[x11] = "rtc_use_x11=true,rtc_use_x11=false,\
    libx11 libxcomposite libxdamage libxext libxfixes libxrandr libxtst"
PACKAGECONFIG[desktop-capture] = "libwebrtc_desktop_capture=true,libwebrtc_desktop_capture=false,libdrm virtual/egl"
PACKAGECONFIG[pipewire] = "rtc_use_pipewire=true,rtc_use_pipewire=false,pipewire glib-2.0"
PACKAGECONFIG[v4l2-codec] = "lw_enable_v4l2_codec=true,lw_enable_v4l2_codec=false,libva"

python () {
    pc = set((d.getVar('PACKAGECONFIG') or '').split())
    pn = d.getVar('PN')
    if 'desktop-capture' in pc and 'pipewire' not in pc:
        bb.fatal("%s: 'desktop-capture' requires 'pipewire' -- "
                 "DesktopCaptureOptions::set_allow_pipewire() only exists when "
                 "webrtc is built with rtc_use_pipewire=true" % pn)
}

# Mirrors the gn args the local webrtc-build tree builds with
# (/mnt/dev/webrtc-build/src/out-x64-release/args.gn), plus the Yocto cross
# additions (target_triple / target_sysroot / use_sysroot=false) and the
# generated toolchain file.
#
# use_custom_libcxx deliberately DIVERGES from the local args.gn (which sets it
# false). That native build finds the host's C++ headers on the default include
# path; this cross build drives chromium's clang toolchain with use_sysroot=false
# and a toolchain file that sets only cc/cxx/ld, so there is no C++ include path
# at all -- every <array>/<string>/<cstddef> include fails. true uses webrtc's
# in-tree libc++, which is self-contained.
#
# rtc_include_tests=true is required, not optional: libwebrtc/BUILD.gn declares
# rtc_test("libwebrtc_cpp_api_unittests") unconditionally and it depends on
# //test:test_main, which webrtc only defines when tests are included. With it
# false, `gn gen` fails resolving the dependency graph. Nothing extra is
# compiled -- do_compile builds only the libwebrtc target.
# Include paths gn cannot work out for itself: use_sysroot=false keeps
# chromium's clang away from the Yocto sysroot, and the versioned subdirs
# (pipewire-0.3, spa-0.2, glib-2.0) are normally supplied by pkg-config, which
# is not sysroot-aware in this build.
LIBWEBRTC_EXTRA_CPPFLAGS = "-I${STAGING_DIR_TARGET}${includedir}"
LIBWEBRTC_EXTRA_CPPFLAGS += "${@bb.utils.contains('PACKAGECONFIG', 'pipewire', ' -I${STAGING_DIR_TARGET}${includedir}/pipewire-0.3 -I${STAGING_DIR_TARGET}${includedir}/spa-0.2 -I${STAGING_DIR_TARGET}${includedir}/glib-2.0 -I${STAGING_DIR_TARGET}${libdir}/glib-2.0/include', '', d)}"
# desktop capture's egl_dmabuf.cc pulls in xf86drm.h, whose #include <drm.h>
# resolves only with libdrm's own include dir on the path.
LIBWEBRTC_EXTRA_CPPFLAGS += "${@bb.utils.contains('PACKAGECONFIG', 'desktop-capture', ' -I${STAGING_DIR_TARGET}${includedir}/libdrm', '', d)}"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    target_os=\"linux\" \
    target_cpu=\"${GN_TARGET_ARCH_NAME}\" \
    target_triple=\"${TARGET_SYS}\" \
    target_sysroot=\"${STAGING_DIR_TARGET}\" \
    use_sysroot=false \
    is_debug=false \
    symbol_level=0 \
    enable_stripping=true \
    enable_iterator_debugging=false \
    treat_warnings_as_errors=false \
    rtc_include_tests=true \
    rtc_build_tools=false \
    rtc_build_examples=false \
    rtc_enable_protobuf=false \
    rtc_use_h264=true \
    rtc_use_h265=true \
    rtc_libvpx_build_vp9=true \
    enable_libaom=true \
    ffmpeg_branding=\"Chrome\" \
    is_component_build=false \
    use_rtti=true \
    use_custom_libcxx=true \
    use_custom_libcxx_for_host=false \
    use_llvm_libatomic=false \
    use_clang_modules=false \
'

# The two patches the wrapper needs applied to the webrtc tree ship inside the
# libwebrtc checkout, so they track the milestone rather than going stale here.
# Applied with --check first so a do_configure re-run is a no-op instead of a
# failure.
apply_libwebrtc_patch() {
    if [ ! -f "${S}/libwebrtc/patches/$1" ]; then
        bbfatal "libwebrtc/patches/$1 not found -- WEBRTC_BRANCH=${WEBRTC_BRANCH} has no matching libwebrtc patch"
    fi
    if git -C ${S} apply --check "${S}/libwebrtc/patches/$1" 2>/dev/null; then
        git -C ${S} apply "${S}/libwebrtc/patches/$1"
        bbnote "applied $1"
    else
        bbnote "$1 already applied or not applicable; skipping"
    fi
}

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

    apply_libwebrtc_patch add_libwebrtc_build_target.patch
    apply_libwebrtc_patch ${WEBRTC_AUDIO_PATCH}

    #
    # configure toolchain file
    #
    cp ${UNPACKDIR}/toolchain.gn.in ${S}/build/toolchain/linux/BUILD.gn

    sed -i "s|@GN_TARGET_ARCH_NAME@|${GN_TARGET_ARCH_NAME}|g" ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@TARGET_SYS@|${TARGET_SYS}|g"                   ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@EXTRA_CPPFLAGS@|${LIBWEBRTC_EXTRA_CPPFLAGS}|g" ${S}/build/toolchain/linux/BUILD.gn

    gn gen "${B}" --args="${GN_ARGS}"
}

do_compile() {
    ninja -C ${B} libwebrtc $PARALLEL_MAKE
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

LIBWEBRTC_SOVER = "${@d.getVar('PV').split('.')[0]}"

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${B}/libwebrtc.so ${D}${libdir}/libwebrtc.so.${PV}

    # gn links the library with an unversioned DT_SONAME ("libwebrtc.so"), so a
    # consumer records that as its DT_NEEDED and would then need the -dev package
    # (which owns the unversioned symlink) present at run time. Stamp the
    # versioned soname instead, before anything links against it: this copy is
    # what do_populate_sysroot stages, so consumers pick up
    # libwebrtc.so.${LIBWEBRTC_SOVER} and are satisfied by the runtime package
    # alone.
    patchelf --set-soname libwebrtc.so.${LIBWEBRTC_SOVER} ${D}${libdir}/libwebrtc.so.${PV}

    cd ${D}${libdir}
    ln -sf libwebrtc.so.${PV} libwebrtc.so.${LIBWEBRTC_SOVER}
    ln -sf libwebrtc.so.${LIBWEBRTC_SOVER} libwebrtc.so

    install -d ${D}${includedir}/libwebrtc
    cp -R ${S}/libwebrtc/include/* ${D}${includedir}/libwebrtc

    # ivi-homescreen's LIBWEBRTC_DIR wants a prefix whose include/ holds the flat
    # C ABI header (include/c/lw_c_api.h), so expose that layout too.
    install -d ${D}${datadir}/libwebrtc
    ln -sf ${includedir}/libwebrtc ${D}${datadir}/libwebrtc/include
}

FILES:${PN}-dev += "${datadir}/libwebrtc"

# The debian bbclass renames a package holding a versioned shared library to
# lib<name><soversion>, which publishes this as libwebrtc144. Consumers refer to
# it as libwebrtc, so keep the recipe name in the feed.
DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
