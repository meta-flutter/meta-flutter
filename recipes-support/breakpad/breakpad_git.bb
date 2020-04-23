DESCRIPTION = "A set of client and server components which implement a crash-reporting system."

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=56c24a43c81c3af6fcf590851931489e"

SRCREV = "a2d3e8b2d5f8f3de06eefec50566c9a54d7cf0a6"

S = "${WORKDIR}/git/src"

inherit python3native autotools pkgconfig

DEPENDS =+ " depot-tools-native"

do_patch() {

    export CURL_CA_BUNDLE=${STAGING_BINDIR_NATIVE}/depot_tools/ca-certificates.crt
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}
    export SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
    export SSH_AGENT_PID=${SSH_AGENT_PID}

    cd ${S}/..
    gclient.py config --spec 'solutions = [
        {
           "url": "https://chromium.googlesource.com/breakpad/breakpad.git",
           "managed": False,
           "name": "src",
           "custom_deps": {},
        }
    ]'

    cd ${S}
    gclient.py sync --revision ${SRCREV} ${PARALLEL_MAKE} -v
}
do_patch[depends] =+ " \
    depot-tools-native:do_populate_sysroot \
    "

do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"
