# Copyright (c) 2020-2022 Woven Alpha, Inc
# Copyright (c) 2023-2023 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

python () {
    import gn
    bb.fetch2.methods.append(gn.GN())
}

DEPENDS += " \
    ca-certificates-native \
    curl-native \
    depot-tools-native \
    pbzip2-native \
    tar-native \
"

CURL_CA_BUNDLE ??= "${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt"
DEPOT_TOOLS ??= "${STAGING_DIR_NATIVE}/usr/share/depot_tools"
VPYTHON_VIRTUALENV_ROOT ??= "${WORKDIR}/.vpython-root"
DEPOT_TOOLS_XDG_CONFIG_HOME ??= "${WORKDIR}"
PYTHON3_PATH ??= ".cipd_bin/3.11/bin"
EXTRA_GN_SYNC ??= ""
GN_CUSTOM_VARS ??= "{}"
GN_DEPS_FILE ??= "DEPS"
GN_CUSTOM_DEPS ??= "{}"
do_fetch[depends] += " \
    ca-certificates-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    depot-tools-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    pbzip2-native:do_populate_sysroot \
"

do_configure[network] = "1"
do_configure:prepend() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export NO_PROXY=localhost,127.0.0.1,::1
    export PATH=${DEPOT_TOOLS}/${PYTHON3_PATH}:${DEPOT_TOOLS}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export VPYTHON_VIRTUALENV_ROOT=${VPYTHON_VIRTUALENV_ROOT}
}

do_compile[network] = "1"
do_compile:prepend() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export NO_PROXY=localhost,127.0.0.1,::1
    export PATH=${DEPOT_TOOLS}/${PYTHON3_PATH}:${DEPOT_TOOLS}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export VPYTHON_VIRTUALENV_ROOT=${VPYTHON_VIRTUALENV_ROOT}
}
