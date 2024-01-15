#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Firebase C++ SDK"
DESCRIPTION = "Google Firebase C++ SDK"
AUTHOR = "Google Firebase Team"
HOMEPAGE = "https://github.com/firebase/firebase-cpp-sdk"
BUGTRACKER = "https://github.com/firebase/firebase-cpp-sdk/issues"
SECTION = "devtools"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS += "\
    curl \
    flatbuffers \
    flatbuffers-native \
    libsecret \
    libunwind \
    libuv \
    openssl \
    zlib \
    "

SRC_URI = "gitsm://github.com/firebase/firebase-cpp-sdk.git;protocol=https;branch=main;destsuffix=git \
           gitsm://github.com/uNetworking/uWebSockets.git;protocol=https;nobranch=1;name=uwebsockets;destsuffix=git/third_party/uWebSockets \
           git://github.com/firebase/firebase-ios-sdk.git;protocol=https;nobranch=1;name=firestore;destsuffix=git/third_party/firestore \
           git://github.com/nanopb/nanopb.git;protocol=https;nobranch=1;name=nanopb;destsuffix=git/third_party/nanopb \
           file://0001-enable-system-libraries.patch \
           file://BuildFlatBuffers.cmake \
           "

SRCREV = "b5b7e05b6a6e3c7ca4baadb7f15ef6fc6f36829c"

SRCREV_FORMAT .= "_uwebsockets"
SRCREV_uwebsockets = "4d94401b9c98346f9afd838556fdc7dce30561eb"
SRCREV_FORMAT .= "_firestore"
SRCREV_firestore = "8a8ec57a272e0d31480fb0893dda0cf4f769b57e"
SRCREV_FORMAT .= "_nanopb"
SRCREV_nanopb = "7ee9ef9f627d85cbe1b8c4f49a3ed26eed216c77"


S = "${WORKDIR}/git"

inherit cmake pkgconfig

PACKAGECONFIG ??= "\
    analytics \
    app_check \
    auth \
    database \
    dynamic_links \
    fake_secure_storage \
    firestore \
    functions \
    gma \
    installations \
    messaging \
    remote_config \
    storage \
    "

PACKAGECONFIG[analytics] = "-DFIREBASE_INCLUDE_ANALYTICS=ON, -DFIREBASE_INCLUDE_ANALYTICS=OFF"
PACKAGECONFIG[app_check] = "-DFIREBASE_INCLUDE_APP_CHECK=ON, -DFIREBASE_INCLUDE_APP_CHECK=OFF"
PACKAGECONFIG[auth] = "-DFIREBASE_INCLUDE_AUTH=ON, -DFIREBASE_INCLUDE_AUTH=OFF"
PACKAGECONFIG[database] = "-DFIREBASE_INCLUDE_DATABASE=ON, -DFIREBASE_INCLUDE_DATABASE=OFF,leveldb"
PACKAGECONFIG[dynamic_links] = "-DFIREBASE_INCLUDE_DYNAMIC_LINKS=ON, -DFIREBASE_INCLUDE_DYNAMIC_LINKS=OFF"
PACKAGECONFIG[fake_secure_storage] = "-DFIREBASE_FORCE_FAKE_SECURE_STORAGE=ON, -DFIREBASE_FORCE_FAKE_SECURE_STORAGE=OFF"
PACKAGECONFIG[firestore] = "-DFIREBASE_INCLUDE_FIRESTORE=ON, -DFIREBASE_INCLUDE_FIRESTORE=OFF, abseil-cpp grpc"
PACKAGECONFIG[functions] = "-DFIREBASE_INCLUDE_FUNCTIONS=ON, -DFIREBASE_INCLUDE_FUNCTIONS=OFF"
PACKAGECONFIG[gma] = "-DFIREBASE_INCLUDE_GMA=ON, -DFIREBASE_INCLUDE_GMA=OFF"
PACKAGECONFIG[installations] = "-DFIREBASE_INCLUDE_INSTALLATIONS=ON, -DFIREBASE_INCLUDE_INSTALLATIONS=OFF"
PACKAGECONFIG[messaging] = "-DFIREBASE_INCLUDE_MESSAGING=ON, -DFIREBASE_INCLUDE_MESSAGING=OFF"
PACKAGECONFIG[remote_config] = "-DFIREBASE_INCLUDE_REMOTE_CONFIG=ON, -DFIREBASE_INCLUDE_REMOTE_CONFIG=OFF"
PACKAGECONFIG[storage] = "-DFIREBASE_INCLUDE_STORAGE=ON, -DFIREBASE_INCLUDE_STORAGE=OFF"


EXTRA_OECMAKE += "\
    -D FIREBASE_INCLUDE_LIBRARY_DEFAULT=OFF \
    -D FIREBASE_LINUX_USE_CXX11_ABI=ON \
    -D FIREBASE_CPP_BUILD_PACKAGE=ON \
    -D FIREBASE_USE_BORINGSSL=OFF \
    \
    -D FIREBASE_USE_SYSTEM_LIBS=ON \
    -D BUILD_FLAT_BUFFERS_PATH=${WORKDIR}/BuildFlatBuffers.cmake \
    -D UWEBSOCKETS_SOURCE_DIR=${S}/third_party/uWebSockets \
    -D FIRESTORE_SOURCE_DIR=${S}/third_party/firestore \
"

do_install:append () {
    install -d ${D}${libdir}/firebase
    mv ${D}${libdir}s/linux/x86_64/* ${D}${libdir}/firebase

    rm ${D}/usr/CMakeLists.txt
    rm ${D}/usr/NOTICES
    rm ${D}/usr/readme.md

    rm -rf ${D}/usr/Android
    rm -rf ${D}/usr/libs
}

FILES:${PN}-staticdev = "\
    ${libdir} \
    "
