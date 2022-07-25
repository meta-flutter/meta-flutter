SUMMARY = "Embedded Linux embedding for Flutter"
AUTHOR = "Sony Group Corporation"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    compiler-rt \
    libcxx \
    libinput \
    libxkbcommon \
    virtual/egl \
    "

RDEPENDS:${PN} += "xkeyboard-config"

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master \
           file://0001-path-updates.patch"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

require conf/include/flutter-runtime.inc

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

EXTRA_OECMAKE += "\
    -D USER_PROJECT_PATH=${S}/examples/${@gn_get_pn_without_runtime_suffix(d)} \
    "

do_configure:prepend() {
   mkdir -p ${S}/build | true
   ln -sf ${STAGING_LIBDIR}/libflutter_engine.so ${S}/build/libflutter_engine.so
}

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"

DEPENDS += "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
RDEPENDS:${PN} += "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
