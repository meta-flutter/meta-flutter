# Copyright (c) 2021-2022 Woven Alpha, Inc

python () {
    import gn
    bb.fetch2.methods.append(gn.GN())
}

DEPENDS += " \
    curl-native \
    ca-certificates-native \
    ninja-native \
    depot-tools-native \
    tar-native \
    xz-native \
"

inherit python3native

CURL_CA_BUNDLE ??= "${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt"
DEPOT_TOOLS ??= "${STAGING_DIR_NATIVE}/usr/share/depot_tools"
PYTHON2_PATH ??= "bootstrap-2@3.8.9.chromium.14_bin/python/bin"
EXTRA_GN_SYNC ??= ""
GN_CUSTOM_VARS ??= ""
do_fetch[depends] += " \
    curl-native:do_populate_sysroot \
    ca-certificates-native:do_populate_sysroot \
    depot-tools-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    xz-native:do_populate_sysroot \
    fontconfig:do_populate_sysroot \
"

do_configure_prepend() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export GCLIENT_PY3=0
}

do_compile_prepend() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export GCLIENT_PY3=0
}
