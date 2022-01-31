SUMMARY = "OpenGL tracing tool"
DESCRIPTION = "trace OpenGL, OpenGL ES, Direct3D, and DirectDraw APIs calls to a file."
AUTHOR = "apitrace team"
HOMEPAGE = "http://apitrace.github.io"
BUGTRACKER = "https://github.com/apitrace/apitrace/issues"
SECTION = "graphics"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=49fada46694956cdf2fc0292d72d888c"

DEPENDS += "\
    compiler-rt \
    libcxx \
    libdwarf \
    libpng \
    procps \
    virtual/egl \
    virtual/libgles2 \
    zlib \
   "

REQUIRED_DISTRO_FEATURES = "opengl"

SRC_URI = "git://github.com/apitrace/apitrace.git;protocol=https;branch=master"
SRCREV = "03e4e9d2cab4f1a61d3f589785641a73dff027c5"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

EXTRA_OECMAKE = "-D ENABLE_GUI=FALSE"

BBCLASSEXTEND = ""
