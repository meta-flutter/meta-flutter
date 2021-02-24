SUMMARY = "A multi-platform library for OpenGL, OpenGL ES, Vulkan, window and input"
DESCRIPTION = "GLFW is an Open Source, multi-platform library for OpenGL, \
               OpenGL ES and Vulkan application development. It provides a simple, \
               platform-independent API for creating windows, contexts and surfaces, reading \
               input, handling events, etc."
AUTHOR = "glfw team"
HOMEPAGE = "https://www.glfw.org/"
BUGTRACKER = "https://github.com/glfw/glfw/issues"
SECTION = "graphics"
LICENSE = "Zlib"
CVE_PRODUCT = "libglfw.so"

LIC_FILES_CHKSUM = "file://LICENSE.md;md5=98d93d1ddc537f9b9ea6def64e046b5f"

DEPENDS += "libxkbcommon wayland wayland-native"

SRC_URI = "git://github.com/glfw/glfw.git;protocol=https;branch=master"
SRCREV = "2c7f3ce91b4f22773855c43d6480824ed4ac9907"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

EXTRA_OECMAKE += "-D BUILD_SHARED_LIBS=ON \
                  -D GLFW_BUILD_DOCS=OFF \
                  -D GLFW_USE_WAYLAND=ON \
                  -D CMAKE_POSITION_INDEPENDENT_CODE=ON \
                 "

REQUIRED_DISTRO_FEATURES = "wayland"

COMPATIBLE_HOST_libc-musl = "null"

BBCLASSEXTEND = ""
