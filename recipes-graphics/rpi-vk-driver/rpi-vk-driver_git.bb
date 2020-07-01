LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0e9db807e4f1ed14373059c8499d5f82"

SRC_URI = "git://github.com/jwinarske/rpi-vk-driver.git;protocol=https;branch=cmake_for_clang"
SRCREV = "cff21b40cc7a5257c7c4a60b9ccd9de267f926b8"

DEPENDS_append = " libdrm vulkan-headers expat zlib"

S = "${WORKDIR}/git"

REQUIRED_DISTRO_FEATURES = "vulkan"

inherit cmake distro_features_check
ANY_OF_DISTRO_FEATURES = "x11 wayland"

OECMAKE_FIND_ROOT_PATH_MODE_PROGRAM = "BOTH"

# todo add package flags
EXTRA_OECMAKE_append = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DCMAKE_SKIP_INSTALL_RPATH=ON \
    -DBUILD_TESTING=OFF \
    "

FILES_${PN} = " \
    ${libdir}/librpi-vk-driver.so \
    ${datadir}/vulkan/icd.d/rpi-vk-driver.json \
    "

FILES_${PN}-dev  = " \
    ${bindir}/QPUassemblerExe \
    "

# must choose x11 or wayland or both
PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11', d)}"

PACKAGECONFIG[x11] = "-DBUILD_WSI_XLIB_SUPPORT=ON -DBUILD_WSI_XCB_SUPPORT=ON, -DBUILD_WSI_XLIB_SUPPORT=OFF -DBUILD_WSI_XCB_SUPPORT=OFF, libxcb libx11 libxrandr"
PACKAGECONFIG[wayland] = "-DBUILD_WSI_WAYLAND_SUPPORT=ON, -DBUILD_WSI_WAYLAND_SUPPORT=OFF, wayland"
