SUMMARY = "VK driver for the Raspberry Pi (Broadcom Videocore IV)"
DESCRIPTION = "RPi-VK-Driver is a low level GPU driver for the \
               Broadcom Videocore IV GPU that implements a subset \
               of the Vulkan (registered trademark of The Khronos \
               Group) standard."
AUTHOR = "Marton Tamas"
HOMEPAGE = "https://github.com/Yours3lf/rpi-vk-driver"
BUGTRACKER = "https://github.com/Yours3lf/rpi-vk-driver/issues"
SECTION = "graphics"
CVE_PRODUCT = "librpi-vk-driver.so"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0e9db807e4f1ed14373059c8499d5f82"

DEPENDS += "expat glibc libdrm vulkan-headers zlib"

SRC_URI = "git://github.com/Yours3lf/rpi-vk-driver.git;protocol=https;branch=master \
           file://0001-autogen-problem.patch"
SRCREV = "39bb5f20e39185bf41a636a238437d55dc6ded5a"
S = "${WORKDIR}/git"

REQUIRED_DISTRO_FEATURES = "vulkan"
ANY_OF_DISTRO_FEATURES = "x11 wayland"

inherit cmake features_check

TOOLCHAIN = "clang"

OECMAKE_FIND_ROOT_PATH_MODE_PROGRAM = "BOTH"

# todo add package flags
EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=Release \
                  -DCMAKE_VERBOSE_MAKEFILE=TRUE \
                  -DCMAKE_SKIP_INSTALL_RPATH=ON \
                  -DBUILD_TESTING=OFF \
                 "

# must choose x11 or wayland or both
PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11', d)}"

PACKAGECONFIG[x11] = "-DBUILD_WSI_XLIB_SUPPORT=ON -DBUILD_WSI_XCB_SUPPORT=ON, -DBUILD_WSI_XLIB_SUPPORT=OFF -DBUILD_WSI_XCB_SUPPORT=OFF, libxcb libx11 libxrandr"
PACKAGECONFIG[wayland] = "-DBUILD_WSI_WAYLAND_SUPPORT=ON, -DBUILD_WSI_WAYLAND_SUPPORT=OFF, wayland"

FILES_${PN} = "${libdir}/librpi-vk-driver.so \
               ${datadir}/vulkan/icd.d/rpi-vk-driver.json \
              "
FILES_${PN}-dev = "${bindir}/QPUassemblerExe"

BBCLASSEXTEND = ""
