SUMMARY = "Flutter App Database benchmark"
DESCRIPTION = "Flutter App benchmarking the performance of ObjectBox against others (e.g. sqflite, Hive)."
AUTHOR = "ObjectBox"
HOMEPAGE = "https://github.com/objectbox/objectbox-dart-performance"
BUGTRACKER = "https://github.com/objectbox/objectbox-dart-performance/issues"
SECTION = "graphics"

LICENSE = "CLOSED"

DEPENDS += "\
    sqlite3 \
"

RDEPENDS:${PN} += "\
    liberation-fonts \
    sqlite3 \
    "

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv6 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"

PUBSPEC_APPNAME = "benchapp"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_PREBUILD_CMD = "flutter pub get; flutter pub run build_runner build --delete-conflicting-outputs"

inherit flutter-app python3native

require conf/include/utils.inc

OB_REV = "0.16.0"
OB_ARCH = "${@get_binary_pkg_arch(d)}"

SRCREV = "f6fa12b7f81692f307347badcc86c17ed0a810c5"
SRC_URI = "git://github.com/meta-flutter/objectbox-dart-performance.git;lfs=0;branch=jw/yocto;protocol=https;destsuffix=git \
           https://github.com/objectbox/objectbox-c/releases/download/v${OB_REV}/objectbox-linux-${OB_ARCH}.tar.gz;downloadfilename=objectbox-linux-${OB_ARCH}-v${OB_REV}.tar.gz;subdir=_deps/${OB_ARCH};name=lib_${OB_ARCH} \
          "

SRC_URI[lib_aarch64.md5_sum] = "66390fae5df1386a8a4f58bceed970dd"
SRC_URI[lib_aarch64.sha256sum] = "6f9cbff3892d28c7192e60f20ef23762c385817fc25a2844f354075b507e6f8b"
SRC_URI[lib_armv6hf.md5_sum] = "9b9f826991eea0b4f467515f331be69f"
SRC_URI[lib_armv6hf.sha256sum] = "6f86aac86f5ed4ff4c39b344f614fcd40ca94bff3ff6daf8e4821c69697cdb4b"
SRC_URI[lib_armv7hf.md5_sum] = "b885f6bdad35782211c14dc4b5873537"
SRC_URI[lib_armv7hf.sha256sum] = "55b9a55ddd78a28e3751a21d1f4be6689df0bdfb4ff2588972ed86433773700c"
SRC_URI[lib_x64.md5_sum] = "e45a201afa8c98a9238b6887ef89d353"
SRC_URI[lib_x64.sha256sum] = "b597c10607bbba71b238c935581ee7bde5ea07211d899bd4581685e495da01f8"

S = "${WORKDIR}/git"

do_install:append() {
    install -d ${D}${FLUTTER_INSTALL_DIR}/lib
    install -m 644 \
        ${WORKDIR}/_deps/${OB_ARCH}/lib/libobjectbox.so \
        ${D}${FLUTTER_INSTALL_DIR}/lib
}
