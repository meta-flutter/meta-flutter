SUMMARY = "Map Renderer"
DESCRIPTION = "2D and 3D map renderer using OpenGL ES"
AUTHOR = "Mapzen"
HOMEPAGE = "https://github.com/tangrams/tangram-es"
BUGTRACKER = "https://github.com/tangrams/tangram-es/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9f31a7b567ba0cb3f3ad9edc53383a9b"

DEPENDS = "\
    compiler-rt \
    curl \
    fontconfig \
    freetype \
    harfbuzz \
    icu \
    libcxx \
    sqlite \
    virtual/libgl \
    "

SRC_URI = "\
    git://github.com/meta-flutter/tangram-es.git;protocol=https;branch=jw/wayland \
    git://github.com/tangrams/isect2d.git;protocol=https;nobranch=1;name=isect2d;destsuffix=git/core/deps/isect2d \
    git://github.com/tangrams/css-color-parser-cpp;protocol=https;nobranch=1;name=css_parser;destsuffix=git/core/deps/css-color-parser-cpp \
    git://github.com/tangrams/variant;protocol=https;nobranch=1;name=variant;destsuffix=git/core/deps/variant \
    git://github.com/tangrams/earcut.hpp;protocol=https;nobranch=1;name=earcut;destsuffix=git/core/deps/earcut \
    git://github.com/tangrams/geojson-vt-cpp.git;protocol=https;nobranch=1;name=geojson;destsuffix=git/core/deps/geojson-vt-cpp \
    git://github.com/tangrams/yaml-cpp;protocol=https;nobranch=1;name=yaml;destsuffix=git/core/deps/yaml-cpp \
    git://github.com/glfw/glfw;protocol=https;nobranch=1;name=glfw;destsuffix=git/platforms/common/glfw \
    git://github.com/google/benchmark;protocol=https;nobranch=1;name=benchmark;destsuffix=git/bench/benchmark \
    git://github.com/hjanetzek/alfons;protocol=https;nobranch=1;name=alfons;destsuffix=git/core/deps/alfons \
    git://github.com/tangrams/harfbuzz-icu-freetype.git;protocol=https;nobranch=1;name=harfbuzz_icu_freetype;destsuffix=git/core/deps/harfbuzz-icu-freetype \
    git://github.com/SRombauts/SQLiteCpp.git;protocol=https;nobranch=1;name=SQLiteCpp;destsuffix=git/core/deps/SQLiteCpp \
"

PV = "1.0+git${SRCPV}"
SRCREV = "f41051a045bbe9f7db750a4c5299826ef4487187"

SRCREV_FORMAT .= "_isect2d"
SRCREV_isect2d = "2e1a75cee09d9949900e926c61c86505b09205b2"
SRCREV_FORMAT .= "_css_parser"
SRCREV_css_parser = "1771826bc3a868e5e23e3ba6d2a024872218812d"
SRCREV_FORMAT .= "_variant"
SRCREV_variant = "e26e56c3e7d5b819702ff71ee57d82be5b097f65"
SRCREV_FORMAT .= "_earcut"
SRCREV_earcut = "195b2fdae9a354c0f86592f320df4988c896f822"
SRCREV_FORMAT .= "_geojson"
SRCREV_geojson = "52c96b66467e9cf154509860a1b5ff936022f957"
SRCREV_FORMAT .= "_yaml"
SRCREV_yaml = "49e11f16c1691a61f89367d8eb7dfa10d9153700"
SRCREV_FORMAT .= "_glfw"
SRCREV_glfw = "dd8a678a66f1967372e5a5e3deac41ebf65ee127"
SRCREV_FORMAT .= "_benchmark"
SRCREV_benchmark = "e776aa0275e293707b6a0901e0e8d8a8a3679508"
SRCREV_FORMAT .= "_alfons"
SRCREV_alfons = "26a4dc693ad35609d4909141f763f1bad32e681f"
SRCREV_FORMAT .= "_harfbuzz_icu_freetype"
SRCREV_harfbuzz_icu_freetype = "67b579e42f6afb39646909af33f156a2cfe9d5e4"
SRCREV_FORMAT .= "_SQLiteCpp"
SRCREV_SQLiteCpp = "4e3d36af2d4a612d548f5959532a7b97c9724e6b"

S = "${WORKDIR}/git"

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

PACKAGECONFIG ??= "${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)}"

PACKAGECONFIG[wayland] = "\
    -DTANGRAM_USE_WAYLAND=ON  -DTANGRAM_USE_X11=OFF, \
    -DTANGRAM_USE_WAYLAND=OFF -DTANGRAM_USE_X11=ON, \
    wayland wayland-native wayland-protocols libxkbcommon"

inherit cmake pkgconfig

EXTRA_OECMAKE = "\
    -DTANGRAM_PLATFORM=linux \
    -DTANGRAM_USE_SYSTEM_FONT_LIBS=ON \
    -DTANGRAM_USE_SYSTEM_GLFW_LIBS=OFF \
    -DTANGRAM_USE_SYSTEM_SQLITE_LIBS=ON \
    "

cmake_do_install() {
    install -d ${D}${datadir}/tangram/res
    cp ${B}/tangram ${D}${datadir}/tangram/
    cp -r ${B}/res/* ${D}${datadir}/tangram/res/
}

FILES:${PN} += "${datadir}"

BBCLASSEXTEND = ""
