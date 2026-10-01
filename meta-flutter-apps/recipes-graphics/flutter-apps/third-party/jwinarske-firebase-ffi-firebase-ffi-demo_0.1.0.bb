#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
SUMMARY = "firebase_ffi_demo"
DESCRIPTION = "Firebase workbench for embedded Linux: sign in, browse and edit \
the Realtime Database, move objects in and out of Storage, call a function, \
read Remote Config and mint an App Check token, through the ordinary \
FlutterFire plugins over firebase_ffi's Linux implementations."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/jwinarske/firebase_ffi"
BUGTRACKER = "https://github.com/jwinarske/firebase_ffi/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=46382638866a5bb9f04134b101d78fb2"

SRCREV = "c205ef08bb48728ebf4f3665abbee966a9647ab4"
SRC_URI = "git://github.com/jwinarske/firebase_ffi.git;branch=main;protocol=https"

FLUTTER_APPLICATION_PATH = "example"
PUBSPEC_APPNAME = "firebase_ffi_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-ffi-demo"

# The native library is firebase_ffi's own build hook, not an embedder plugin:
# it links the Firebase C++ SDK and is staged into the bundle as a code asset.
DEPENDS += "firebase-cpp-sdk"

inherit flutter-app-native

#
# What the build hook is told.
#
# The hook reads these from the app's pubspec.yaml, not from the environment --
# the hook runner does not forward it -- so the recipe writes them there. The
# checked-in values are for a host build with no SDK installed
# (with_firebase: false, which produces the transport-only library), which is
# not what we are doing.
#
# FIREBASE_FFI_PRODUCTS has to be a subset of what firebase-cpp-sdk built: the
# hook turns each name into -DFDB_WITH_<PRODUCT>=ON and the link then expects
# that archive to exist. Adding firestore here means adding it there too:
#
#   PACKAGECONFIG:append:pn-firebase-cpp-sdk = " firestore"
#
FIREBASE_FFI_PRODUCTS ?= "auth database storage functions remote_config app_check"
FIREBASE_FFI_SDK_PREFIX ?= "${STAGING_DIR_HOST}${prefix}"

python firebase_ffi_write_user_defines() {
    import os

    pubspec = os.path.join(d.getVar('S'),
                           d.getVar('FLUTTER_APPLICATION_PATH'),
                           'pubspec.yaml')
    with open(pubspec) as handle:
        text = handle.read()

    # Replace the block wholesale rather than editing lines in it: the keys we
    # set are not all present to begin with (firebase_sdk is not), and a sed
    # that only rewrote with_firebase would leave the SDK unfound and the
    # library transport-only -- which fails at runtime as "build has no Firebase
    # SDK", not at build time.
    marker = '\nhooks:\n'
    if marker not in text:
        bb.fatal('firebase_ffi_demo: no hooks: block in %s; upstream moved the '
                 'user_defines and the build hook is no longer being configured'
                 % pubspec)

    products = (d.getVar('FIREBASE_FFI_PRODUCTS') or '').split()
    if not products:
        bb.fatal('firebase_ffi_demo: FIREBASE_FFI_PRODUCTS is empty')

    block = (
        '\nhooks:\n'
        '  user_defines:\n'
        '    firebase_ffi:\n'
        '      products: [%s]\n'
        '      with_firebase: true\n'
        '      firebase_sdk: %s\n'
        % (', '.join(products), d.getVar('FIREBASE_FFI_SDK_PREFIX'))
    )
    with open(pubspec, 'w') as handle:
        handle.write(text[:text.index(marker)] + block)

    bb.note('firebase_ffi_demo: products %s, SDK at %s'
            % (' '.join(products), d.getVar('FIREBASE_FFI_SDK_PREFIX')))
}
firebase_ffi_write_user_defines[vardeps] += "\
    FIREBASE_FFI_PRODUCTS \
    FIREBASE_FFI_SDK_PREFIX \
    "
do_configure[prefuncs] += "firebase_ffi_write_user_defines"

# google-services.json
#
# GoogleServicesConfig.resolvePath() (firebase_ffi's lib/google_services.dart,
# not the C++ SDK) takes the first of:
#
#   1. a path passed in -- the app's Project page has a field for it
#   2. $GOOGLE_SERVICES_JSON
#   3. google-services.json in the working directory
#
# 3 is not the bundle root unless the launcher cd'd there; under a systemd unit
# it is /. So the environment variable is the one to use:
#
#   Environment=GOOGLE_SERVICES_JSON=${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/release/google-services.json
#
# The file is a project's own config, so nothing ships unless one is named:
#
#   FIREBASE_GOOGLE_SERVICES_JSON = "/path/to/google-services.json"
#
# do_install logs where it put it. Without one the app starts and says it has no
# project.
FIREBASE_GOOGLE_SERVICES_JSON ?= ""

do_install:append() {
    [ -n "${FIREBASE_GOOGLE_SERVICES_JSON}" ] || return 0
    if [ ! -f "${FIREBASE_GOOGLE_SERVICES_JSON}" ]; then
        bbfatal "FIREBASE_GOOGLE_SERVICES_JSON names ${FIREBASE_GOOGLE_SERVICES_JSON}, which does not exist"
    fi
    for mode in ${FLUTTER_APP_RUNTIME_MODES}; do
        dest=${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/$mode
        [ -d "${D}$dest" ] || continue
        install -m 0600 "${FIREBASE_GOOGLE_SERVICES_JSON}" "${D}$dest/google-services.json"
        bbnote "google-services.json installed at $dest/google-services.json"
    done
}

# The SDK's secure store and installation id, at runtime as well as link time.
RDEPENDS:${PN} += "libsecret util-linux-libuuid"
