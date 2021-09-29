SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with Wayland compositor"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "connectivity"
CVE_PRODUCT = "homescreen"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

DEPENDS += "\
    flutter-engine \
    glib-2.0 \
    virtual/egl \
    wayland \
    wayland-native \
    wayland-protocols \
    libcxx \
    "

PV .= "+${SRCPV}"

SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit cmake

TOOLCHAIN = "clang"

RUNTIME = "llvm"

EXTRA_OECMAKE += "\ 
    -D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr \
    -D CMAKE_THREAD_LIBS_INIT=-lpthread \
    -D CMAKE_HAVE_THREADS_LIBRARY=1 \
    -D CMAKE_USE_WIN32_THREADS_INIT=0 \
    -D CMAKE_USE_PTHREADS_INIT=1 \
    -D THREADS_PREFER_PTHREAD_FLAG=ON \
    "

BBCLASSEXTEND = ""
