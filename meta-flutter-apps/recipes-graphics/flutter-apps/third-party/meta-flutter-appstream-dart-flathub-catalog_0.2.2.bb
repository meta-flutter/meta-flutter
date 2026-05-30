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
LIC_FILES_CHKSUM = "file://LICENSE;md5=641bdc36389b26ea9787acb6844e4b22"

SRCREV = "c53d5c8a9ba048cdca572ba41fb517a830a3b9aa"
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

# Order matters: cmake must be inherited before flutter-app so that
# flutter-app's do_compile (flutter build) and do_install (bundle install)
# win over cmake's EXPORT_FUNCTIONS versions. cmake still provides
# do_configure (its sole definer) and cmake_do_compile, used by the
# do_cmake_compile task below to build libappstream.so for the target.
inherit cmake flutter-app pkgconfig

# APPSTREAM_HOOK_BUILD=ON keeps libappstream.so in the CMake build dir (${B}),
# where do_install picks it up, instead of staging it into the source tree.
# BUILD_TESTING=OFF avoids building the C++ test suite for the target.
EXTRA_OECMAKE += "\
    -D BUILD_TESTING=OFF \
    -D APPSTREAM_HOOK_BUILD=ON \
"

# The appstream_dart build hook runs its own CMake build during `flutter
# build`. SKIP_NATIVE_BUILD makes it stand down so do_cmake_compile
# (cmake.bbclass, with the OE cross toolchain) is the single producer of
# libappstream.so for the target.
export SKIP_NATIVE_BUILD = "1"

# The sqlite3 Dart package (pulled in via drift) defaults to downloading a
# prebuilt libsqlite3 from GitHub during `flutter build`, which fails in the
# network-isolated do_compile. Tell its build hook to resolve sqlite3 from the
# system library instead (DynamicLoadingSystem -> dlopen libsqlite3.so at
# runtime). user_defines are read from the app being built and must live in its
# pubspec.yaml -- pubspec_overrides.yaml only accepts dependency_overrides /
# resolution / workspace, so append the hooks section to pubspec.yaml itself.
do_configure:prepend() {
    if ! grep -q '^hooks:' ${S}/${FLUTTER_APPLICATION_PATH}/pubspec.yaml; then
        cat >> ${S}/${FLUTTER_APPLICATION_PATH}/pubspec.yaml <<'EOF'

hooks:
  user_defines:
    sqlite3:
      source: system
EOF
    fi
}

# libsqlite3.so is resolved from the system at runtime (sqlite3 source: system
# above, and libappstream.so links it), so it must be present on the image.
RDEPENDS:${PN} += "libsqlite3"

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
