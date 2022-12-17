SUMMARY = "Dart SDK"
DESCRIPTION = "The Dart SDK, including the VM, dart2js, core libraries, and more."
AUTHOR = "Dart Team"
HOMEPAGE = "https://github.com/dart-lang/sdk"
BUGTRACKER = "https://github.com/dart-lang/sdk/issues"
SECTION = "devtools"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=29b4ad63b1f1509efea6629404336393"


DEPENDS += "\
    compiler-rt \
    curl-native \
    libcxx \
    zip-native \
    "

SRCREV = "f16b62ea92cc0f04cfd9166992f93419e425c809"
SRC_URI = "gn://github.com/dart-lang/sdk.git;name=sdk"

S = "${WORKDIR}/sdk"

inherit gn-for-flutter python3native pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass

EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

# For do_configure, do_compile
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"

GN_ARGS = "--no-goma --mode=release create_sdk"

GN_ARGS:append:armv7 = " --arch arm"
GN_ARGS:append:armv7a = " --arch arm"
GN_ARGS:append:armv7ve = " --arch arm"
GN_ARGS:append:aarch64 = " --arch arm64"
GN_ARGS:append:x86-64 = " --arch x64"

do_compile() {
    cd ${S}

    ./tools/build.py ${GN_ARGS}
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    install -d ${D}${datadir}/dart-sdk
    cp -r ${S}/out/Release*/dart-sdk/* ${D}${datadir}/dart-sdk/
}

FILES:${PN} += "${datadir}"

INSANE_SKIP:${PN} = "already-stripped ldflags"

BBCLASSEXTEND = "native nativesdk"
