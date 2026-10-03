#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Firebase C++ SDK, desktop build"
DESCRIPTION = "The desktop (Linux) build of the Firebase C++ SDK, staged as \
static archives plus the CMake package config a consumer resolves with \
find_package(firebase_cpp_sdk CONFIG). firebase_ffi's native-assets build hook \
is what consumes it."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/firebase/firebase-cpp-sdk"
BUGTRACKER = "https://github.com/firebase/firebase-cpp-sdk/issues"
SECTION = "devel"
CVE_PRODUCT = "google:firebase_cpp_sdk"

# Where the supplied dependency sources land. file:// and a git destsuffix are
# both relative to UNPACKDIR from styhead on.
EXTERNALS = "${UNPACKDIR}/externals"

# Where the SDK's own download step would have put them. Setting <NAME>_SOURCE_DIR
# is enough to stop the download, but not enough on its own: the post-download
# fixups and the boringssl sub-build name ${PROJECT_BINARY_DIR}/external/src/<name>
# directly rather than going through the variable, so that is the one path both
# sides agree on. See do_configure:prepend().
EXTERNAL_SRC = "${B}/external/src"

# The SDK is Apache-2.0. The rest is what it compiles in: the pinned
# dependencies below are built as part of this recipe and end up inside its
# archives, so their licenses are this package's licenses too. The SDK and
# flatbuffers share the Apache-2.0 text and so the same md5; boringssl is
# OpenSSL with ISC on its newer files; leveldb is BSD-3-Clause; libuv MIT;
# uWebSockets and zlib are Zlib, and zlib states its terms in README, which is
# why that is the file named.
LICENSE = "Apache-2.0 & OpenSSL & ISC & curl & BSD-3-Clause & MIT & Zlib"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
    file://${EXTERNALS}/boringssl/LICENSE;md5=9b082148f9953258347788edb83e401b \
    file://${EXTERNALS}/curl/COPYING;md5=2e9fb35867314fe31c6a4977ef7dd531 \
    file://${EXTERNALS}/flatbuffers/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
    file://${EXTERNALS}/leveldb/LICENSE;md5=92d1b128950b11ba8495b64938fc164d \
    file://${EXTERNALS}/libuv/LICENSE;md5=a68902a430e32200263d182d44924d47 \
    file://${EXTERNALS}/uWebSockets/LICENSE;md5=4017f4be9779f7947c8f95e5d0334ab7 \
    file://${EXTERNALS}/zlib/README;md5=0ff45db88393c3152e458a047bba0ff1 \
    "

#
# The SDK and the eight patches emb's augment names.
#
# They are applied in filename order, which is the order they were cut in: a
# later patch's hunks assume the earlier ones are in. 0006 is macOS-only and is
# not here. 0010 and 0011 are ours.
#
# The boringssl- one is apply=no: it belongs to a staged dependency rather
# than to ${S}, so patch_externals applies it and do_patch does not.
#
SRC_URI = "\
    git://github.com/firebase/firebase-cpp-sdk.git;protocol=https;nobranch=1;name=sdk;destsuffix=firebase-cpp-sdk \
    file://0001-Disable-unknown-compiler-options.patch \
    file://0002-leveldb-do-not-inherit-another-dependency-s-patch-fi.patch \
    file://0003-Build-this-SDK-and-its-dependencies-with-a-current-t.patch \
    file://0004-Add-install-rules-for-the-desktop-SDK.patch \
    file://0005-Recognize-arm-on-desktop-Linux.patch \
    file://0007-Drop-git-gc-from-the-boringssl-patch-step.patch \
    file://0008-Expose-Firestore-sum-and-average-on-desktop.patch \
    file://0009-Do-not-abandon-Repo-setup-over-a-persistence-cache.patch \
    file://0010-external-let-leveldb-use-a-local-source-tree.patch \
    file://0011-Recognize-riscv-on-desktop-Linux.patch \
    file://boringssl-0001-generate-err_data-without-go-module-mode.patch;apply=no \
    "

#
# The dependencies the SDK would otherwise fetch itself.
#
# cmake/external_rules.cmake runs a nested CMake build whose only job is to
# download these, and check_use_local_directory() turns each download off when
# the matching <NAME>_SOURCE_DIR exists. Supplying them through SRC_URI is what
# lets this build with BB_NO_NETWORK, and the revisions are exactly the ones
# cmake/external/*.cmake pins -- so the result is the dependency set upstream
# tests, not a newer one.
#
# They are compiled into this recipe's archives rather than installed, which is
# what the SDK's desktop build is; see LICENSE above.
#
# git rather than the release archives upstream's own script downloads: a GitHub
# archive is generated on demand and its bytes are not guaranteed stable, which
# OE's src-uri-bad check rejects. These are the same tags and commits, resolved.
#
SRC_URI += "\
    git://github.com/google/boringssl.git;protocol=https;nobranch=1;name=boringssl;destsuffix=externals/boringssl \
    git://github.com/curl/curl.git;protocol=https;nobranch=1;name=curl;destsuffix=externals/curl \
    git://github.com/google/flatbuffers.git;protocol=https;nobranch=1;name=flatbuffers;destsuffix=externals/flatbuffers \
    git://github.com/google/leveldb.git;protocol=https;nobranch=1;name=leveldb;destsuffix=externals/leveldb \
    git://github.com/libuv/libuv.git;protocol=https;nobranch=1;name=libuv;destsuffix=externals/libuv \
    git://github.com/uNetworking/uWebSockets.git;protocol=https;nobranch=1;name=uwebsockets;destsuffix=externals/uWebSockets \
    git://github.com/madler/zlib.git;protocol=https;nobranch=1;name=zlib;destsuffix=externals/zlib \
    "

# v13.12.0
SRCREV_sdk = "28a173820827041dab298c819e654ab21da2dcab"
# fips-20220613
SRCREV_boringssl = "df031c2b1322905d1e91b87488b90302a59e1886"
# curl-7_73_0
SRCREV_curl = "db9629e4c539831ec950aca92ddc229e8c2c6b69"
# the v25.2.10 release commit
SRCREV_flatbuffers = "1c514626e83c20fffa8557e75641848e1e15cd5e"
# 1.23
SRCREV_leveldb = "99b3c03b3284f5886f9ef9a4ef703d57373e61be"
# v1.33.1
SRCREV_libuv = "9ccad6fe997e812fdfae75f430b58a435e9f4e29"
SRCREV_uwebsockets = "4d94401b9c98346f9afd838556fdc7dce30561eb"
# v1.2.11
SRCREV_zlib = "7085a61bce3ed39d5e56ca4d01d80f4338c8a4a6"
SRCREV_FORMAT = "sdk_boringssl_curl_flatbuffers_leveldb_libuv_uwebsockets_zlib"

S = "${UNPACKDIR}/firebase-cpp-sdk"

# libsecret backs the Linux secure store and libuuid the installation id. Both
# are link-time, not runtime only, and patch 0004 publishes them through the
# package config as firebase_cpp_sdk_SYSTEM_LIBS so a consumer picks them up.
# go-native and perl: boringssl generates sources with both and its CMake
# hard-errors "Could not find Go" without them. perl is a host tool already.
DEPENDS = "libsecret util-linux python3-native python3-absl-native go-native"
DEPENDS += "compiler-rt libcxx"

# Built with the same standard library its consumers link against: these are
# static archives, so the runtime is part of their interface, not an
# implementation detail. flutter-app-native builds hook libraries the same way.
require conf/include/flutter-clang-toolchain.inc

inherit cmake pkgconfig python3native

# The pinned dependencies predate the compiler. boringssl's crypto/fipsmodule
# trips -Werror=discarded-qualifiers on gcc 15, and libstdc++ has tightened its
# transitive includes since these revisions, which -w alone cannot fix.
#
# Patch 0003 appends the same flags for the SDK's own build and exports them to
# the sub-builds as CFLAGS/CXXFLAGS, but that does not reach a sub-build
# configured with OE's toolchain.cmake: the toolchain file puts
# CMAKE_<LANG>_FLAGS in the cache, and a cache entry beats the environment. Here
# they go through the toolchain file itself, which every sub-build inherits.
CFLAGS += "-w"
CXXFLAGS += "-w -include cstdint -include cstring -include algorithm"
# ctime and cerrno are libc++'s: the SDK's own date_provider.cc reaches
# std::time_t and std::gmtime, and filesystem_desktop_linux.cc reaches EINTR
# and errno, neither of which libc++ pulls in transitively.
CXXFLAGS += "-include ctime -include cerrno"

# app/CMakeLists.txt's version_header.py and cmake/binary_to_array.cmake's
# binary_to_array.py both import absl. Without it the failure is a
# ModuleNotFoundError from inside a CMake custom command, naming nothing that
# suggests a missing Python package.
EXTRA_OECMAKE = "\
    -D FIREBASE_PYTHON_EXECUTABLE=${PYTHON} \
    -D FIREBASE_CPP_INSTALL=ON \
    -D FIREBASE_USE_BORINGSSL=ON \
    -D FIREBASE_CPP_BUILD_TESTS=OFF \
    -D FIREBASE_CPP_BUILD_STUB_TESTS=OFF \
    -D CMAKE_POSITION_INDEPENDENT_CODE=ON \
    -D CMAKE_POLICY_VERSION_MINIMUM=3.5 \
    -D FIREBASE_INCLUDE_LIBRARY_DEFAULT=OFF \
    -D BORINGSSL_SOURCE_DIR=${EXTERNAL_SRC}/boringssl \
    -D CURL_SOURCE_DIR=${EXTERNAL_SRC}/curl \
    -D FLATBUFFERS_SOURCE_DIR=${EXTERNAL_SRC}/flatbuffers \
    -D LEVELDB_SOURCE_DIR=${EXTERNAL_SRC}/leveldb \
    -D LIBUV_SOURCE_DIR=${EXTERNAL_SRC}/libuv \
    -D UWEBSOCKETS_SOURCE_DIR=${EXTERNAL_SRC}/uWebSockets \
    -D ZLIB_SOURCE_DIR=${EXTERNAL_SRC}/zlib \
    "

# libstdc++ has two incompatible std::string ABIs and the SDK still defaults to
# the legacy one, which would mangle every std::string-taking entry point
# differently from the calls a consumer makes. The option the SDK documents is
# FIREBASE_USE_LINUX_CXX11_ABI, but the if() that acts on it reads
# FIREBASE_LINUX_USE_CXX11_ABI -- declared and tested names differ, so both are
# set: the tested one is what takes effect, the declared one is what keeps
# working if upstream reconciles them.
EXTRA_OECMAKE += "\
    -D FIREBASE_LINUX_USE_CXX11_ABI=ON \
    -D FIREBASE_USE_LINUX_CXX11_ABI=ON \
    "

# The products firebase_ffi binds, and no more. With
# FIREBASE_INCLUDE_LIBRARY_DEFAULT off, anything not named here is not built;
# App comes along with any of them.
#
# Firestore is the expensive one and is off by default: it drags in gRPC,
# protobuf and abseil for roughly 23 MB of extra archive, and nothing else here
# needs them. Storage is on -- it is a REST client over the transport the others
# already build, so it costs almost nothing to have available.
PACKAGECONFIG ??= "auth database storage functions remote_config app_check"

PACKAGECONFIG[auth] = "-DFIREBASE_INCLUDE_AUTH=ON,-DFIREBASE_INCLUDE_AUTH=OFF"
PACKAGECONFIG[database] = "-DFIREBASE_INCLUDE_DATABASE=ON,-DFIREBASE_INCLUDE_DATABASE=OFF"
PACKAGECONFIG[storage] = "-DFIREBASE_INCLUDE_STORAGE=ON,-DFIREBASE_INCLUDE_STORAGE=OFF"
PACKAGECONFIG[functions] = "-DFIREBASE_INCLUDE_FUNCTIONS=ON,-DFIREBASE_INCLUDE_FUNCTIONS=OFF"
PACKAGECONFIG[remote_config] = "-DFIREBASE_INCLUDE_REMOTE_CONFIG=ON,-DFIREBASE_INCLUDE_REMOTE_CONFIG=OFF"
PACKAGECONFIG[app_check] = "-DFIREBASE_INCLUDE_APP_CHECK=ON,-DFIREBASE_INCLUDE_APP_CHECK=OFF"
PACKAGECONFIG[firestore] = "-DFIREBASE_INCLUDE_FIRESTORE=ON,-DFIREBASE_INCLUDE_FIRESTORE=OFF"

# Put the supplied sources where the SDK expects to have downloaded them.
#
# Copied rather than symlinked: download_external_sources() appends
# OPENSSL_VERSION_NUMBER and an #include to two boringssl headers in place, and
# a symlink would send those writes back into the unpacked source. The -build
# directories are created here too -- build_external_dependencies() configures
# boringssl with external/src/boringssl-build as its working directory and fails
# with "no such file or directory" if it is not there.
# do_configure:prepend rather than a prefunc: do_configure[cleandirs] is ${B},
# and a prefunc runs before the clean, so anything it staged there is deleted
# again before cmake runs. The symptom is subtle -- CMake's file(APPEND) on the
# boringssl header fixups recreates external/src/boringssl/include/, so the
# directory is there and only its sources are missing.
do_configure:prepend() {
    # Wholesale, not "copy if absent": a failed configure leaves a half-made
    # tree behind -- file(APPEND) on the boringssl header fixups creates
    # external/src/boringssl/include/ even when the source was never there --
    # and a presence test then skips the copy and fails again on a directory
    # with no CMakeLists.txt. Everything under external/ is generated.
    rm -rf ${B}/external
    install -d ${EXTERNAL_SRC}
    for dep in boringssl curl flatbuffers leveldb libuv uWebSockets zlib; do
        cp -a ${EXTERNALS}/$dep ${EXTERNAL_SRC}/$dep
        install -d ${EXTERNAL_SRC}/$dep-build
        # uWebSockets has no CMakeLists.txt of its own -- the SDK compiles its
        # src/*.cpp directly -- so the check is that something arrived, not
        # that it is a CMake project.
        test -n "$(ls -A ${EXTERNAL_SRC}/$dep)" \
            || bbfatal "$dep did not stage: ${EXTERNAL_SRC}/$dep is empty"
    done
}

# The dependency rules that are skipped because their source is supplied also
# skip their PATCH_COMMAND, and three of them carry one the SDK needs. Applied
# here with patch(1) rather than `git apply`: these trees are checked out
# without their .git.
#
# Ours for a dependency go in the same pass, named <dep>-*.patch in SRC_URI.
do_patch[postfuncs] += "patch_externals"
patch_externals() {
    for dep in boringssl flatbuffers uWebSockets; do
        for p in ${S}/scripts/git/patches/$dep/*.patch ${UNPACKDIR}/$dep-*.patch; do
            [ -e "$p" ] || continue
            # Stamped: do_patch re-runs whenever the task signature changes, and
            # these trees are not cleaned in between, so a second pass would
            # fail on patches that are already in.
            stamp=${EXTERNALS}/$dep/.applied-$(basename $p)
            [ -e "$stamp" ] && continue
            bbnote "applying $(basename $p) to $dep"
            patch -p1 --no-backup-if-mismatch -d ${EXTERNALS}/$dep -i "$p" || \
                bbfatal "$dep: $(basename $p) did not apply; the pinned revision moved"
            touch "$stamp"
        done
    done
}

# The install rules copy the build tree's layout wholesale, which brings along
# CMake's own scratch, the SDK's download cache and the iOS CocoaPods staging --
# none of which a consumer links. Pruned here rather than excluded from FILES,
# so they are not "installed but not shipped" either. 'generated' stays: it
# holds generated headers the package config points at.
do_install:append() {
    rm -rf ${D}${libdir}/firebase-cpp-sdk/CMakeFiles \
           ${D}${libdir}/firebase-cpp-sdk/downloads \
           ${D}${libdir}/firebase-cpp-sdk/ios_pod
}

# Nothing here is a runtime artifact: a consumer links the archives and reads
# the package config at build time. An empty ${PN} keeps the packaging honest
# rather than inventing a runtime package for it.
ALLOW_EMPTY:${PN} = "1"

# The install rules keep upstream's own layout: headers under
# ${prefix}/src/firebase-cpp-sdk/<product>/src/include, which is what the
# generated package config points a consumer at, and the archives in a tree of
# their own under ${libdir}/firebase-cpp-sdk -- including the dependency
# archives at external/src/*, which the SDK's own targets link.
FILES:${PN}-dev += "\
    ${libdir}/cmake/firebase_cpp_sdk \
    ${prefix}/src/firebase-cpp-sdk \
    "
FILES:${PN}-staticdev += "${libdir}/firebase-cpp-sdk"

# The headers live under ${prefix}/src, which SYSROOT_DIRS does not cover:
# /usr/include /usr/lib /usr/share /sysroot-only. Without this a consumer's
# sysroot gets the CMake package config and none of the headers, and
# find_package(firebase_cpp_sdk CONFIG QUIET) quietly comes up empty -- which
# builds firebase_ffi's transport-only library instead of failing, and that is
# the thing upstream's --no-undefined exists to catch.
SYSROOT_DIRS += "${prefix}/src/firebase-cpp-sdk"

# The package config is in -dev and names archives in -staticdev, so a consumer
# that has only one of them gets a config that resolves to nothing.
RDEPENDS:${PN}-dev += "${PN}-staticdev"

# Static archives only, and the ABI they were built with has to match the
# consumer's: there is no shared object to check.
INSANE_SKIP:${PN}-staticdev += "staticdev"
