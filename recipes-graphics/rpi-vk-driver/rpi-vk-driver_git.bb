LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0e9db807e4f1ed14373059c8499d5f82"

SRC_URI = "git://github.com/jwinarske/rpi-vk-driver.git;protocol=https;branch=cmake_for_clang"
SRCREV = "617b3f900e121a65f6522b14177f805decfdf17d"

DEPENDS_append = " libdrm vulkan-headers expat zlib"

S = "${WORKDIR}/git"

REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake distro_features_check

EXTRA_OECMAKE_append = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DBUILD_TEST=OFF \
    "

FILES_${PN} = " \
    ${libdir}/librpi-vk-driver.so \
    "

FILES_${PN}-dev  = " \
    ${bindir}/QPUassemblerExe \
    "

