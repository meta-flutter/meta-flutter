#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
# Copyright (c) 2026 Ahmed Wafdy. All rights reserved.
#
SUMMARY = "flathub_catalog"
DESCRIPTION = "Flutter Linux desktop app demonstrating the appstream_dart package."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/appstream_dart"
BUGTRACKER = "https://github.com/meta-flutter/appstream_dart/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2bd45dcb3c7d3bd3ad977ef0ea094f47"

SRCREV = "b132c053826a156fd1b05b9d4a90865c088a1d9a"
SRC_URI = "gitsm://github.com/meta-flutter/appstream_dart.git;branch=main;protocol=https"

DEPENDS += " \
    compiler-rt \
    libcxx \
    lld-native \
    ninja-native \
    sqlite3 \
"

FLUTTER_APPLICATION_PATH = "example/flathub_catalog"
PUBSPEC_APPNAME = "flathub_catalog"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "appstream-dart-example-flathub-catalog"

TOOLCHAIN = "clang"
TOOLCHAIN_NATIVE = "clang"
TC_CXX_RUNTIME = "llvm"
PREFERRED_PROVIDER_llvm = "clang"
PREFERRED_PROVIDER_llvm-native = "clang-native"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

inherit flutter-app cmake pkgconfig

EXTRA_OECMAKE += "\
    -D BUILD_TESTING=OFF \
    -D APPSTREAM_HOOK_BUILD=ON \
"

#
# avoid conflict with flutter-app's do_compile
#

python do_cmake_compile() {
    bb.build.exec_func('cmake_do_compile', d)
}
addtask cmake_compile after do_compile before do_install
do_cmake_compile[dirs] = "${B}"

#
# append to flutter-app's do_install()
#
do_install:append() {
    install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib
    cp ${B}/libappstream.so \
        ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/
}

FILES:${PN} += "\
    ${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libappstream.so \
"

FILES:${PN}-dbg += "\
    ${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/.debug/libappstream.so \
"

INSANE_SKIP:${PN} += " libdir"
INSANE_SKIP:${PN}-dbg += " libdir"
