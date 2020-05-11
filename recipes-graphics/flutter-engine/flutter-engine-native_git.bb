DESCRIPTION = "Flutter Engine"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "9e5072f0ce81206b99db3598da687a19ce57a863"

S = "${WORKDIR}/git/src"

inherit python3native native

DEPENDS =+ " flutter-engine ninja-native depot-tools-native"

require gn-utils.inc

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

GN_ARGS = ""

do_patch() {

    export CURL_CA_BUNDLE=${STAGING_BINDIR_NATIVE}/depot_tools/ca-certificates.crt
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}
    export SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
    export SSH_AGENT_PID=${SSH_AGENT_PID}

    cd ${S}/..
    gclient.py config --spec 'solutions = [
        {
            "managed" : False,
            "name" : "src/flutter",
            "url" : "git@github.com:flutter/engine.git",
            "custom_vars" : {
                "download_android_deps" : False,
                "download_windows_deps" : False,
            }
        }
    ]'

    cd ${S}
    gclient.py sync --no-history --revision ${SRCREV} ${PARALLEL_MAKE} -v
}
do_patch[depends] =+ " \
    depot-tools-native:do_populate_sysroot \
    "

do_configure() {

    ./flutter/tools/gn --unoptimized
}

do_compile() {

	ninja -C out/host_debug_unopt -v
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    cd ${S}/out/host_debug_unopt

    install -d ${D}${bindir}
    install -m 755 dart ${D}${bindir}

    install -d ${D}${datadir}/flutter/engine
    install -m 644 frontend_server.dart.snapshot ${D}${datadir}/flutter/engine
}

FILES_${PN} = " \
    ${bindir}/dart \
    ${datadir}/flutter/engine/frontend_server.dart.snapshot \
    "

SYSROOT_DIRS_NATIVE =+ " \
    ${datadir}/flutter/engine \
    "

INSANE_SKIP_${PN} += "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
