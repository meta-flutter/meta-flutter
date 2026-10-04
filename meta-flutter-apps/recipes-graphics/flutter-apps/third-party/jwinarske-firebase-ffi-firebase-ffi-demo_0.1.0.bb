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

# The git fetcher unpacks to ${WORKDIR}/git on this release while the default S
# is ${WORKDIR}/${BP}; BB_GIT_DEFAULT_DESTSUFFIX, which lines them up, arrives
# later. Without this LIC_FILES_CHKSUM resolves to nothing and do_populate_lic
# fails before anything is built.
S = "${WORKDIR}/git"

FLUTTER_APPLICATION_PATH = "example"
PUBSPEC_APPNAME = "firebase_ffi_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-ffi-demo"

# The native library is firebase_ffi's own build hook, not an embedder plugin:
# it links the Firebase C++ SDK and is staged into the bundle as a code asset.
DEPENDS += "firebase-cpp-sdk"

inherit flutter-app-native

# The standard library, stated here rather than in the class.
#
# This library links firebase-cpp-sdk's static archives, so the two have to
# agree. On the newer branches flutter-app-native requires this include and
# every hook app gets it; this branch's class sets only TOOLCHAIN = "clang"
# and leaves the runtime at the default, and changing that would flip four
# apps that build today onto libc++ without the DEPENDS to match -- meta-clang
# appends LIBCPLUSPLUS to the flags but does not add libcxx for
# TC_CXX_RUNTIME. So scope it to the recipe that needs it.
require conf/include/flutter-clang-toolchain.inc

# Link with lld.
#
# This branch's binutils cannot read the debug info the clang that built the
# SDK's archives emits, and the link dies on the DWARF rather than on anything
# about the code:
#
#   ld: error: invalid or unhandled FORM value: 0x22
#   error: linker command failed with exit code 1
#
# The newer branches carry -fuse-ld=lld in flutter-app-native for this kind of
# reason; this branch's class does not, though it already DEPENDS on lld-native
# and the clang bbappend beside it makes clang-native the provider. So the flag
# is all that is missing.
#
# Deliberately not -rtlib=compiler-rt with it: the default libgcc is what
# provides _Unwind_Resume here, and asking for compiler-rt would reopen #1119.
CFLAGS += "-fuse-ld=lld"
CXXFLAGS += "-fuse-ld=lld"

# The unwinder.
#
# flutter-app-native asks for -rtlib=compiler-rt -unwindlib=libunwind, but the
# libunwind staged here is nongnu's (libunwind-aarch64.h, libunwind-generic.so),
# not LLVM's, and there is no libunwind.so for -lunwind to find. Checked the
# sysroot: libc++abi.so exports no _Unwind_Resume and libgcc_s.so.1 exports it --
# the one unwinder present is the one that flag excludes.
#
# The other hook-building apps never notice, because a shared library links with
# the symbol unresolved and the embedder supplies it at runtime. This one sets
# -Wl,--no-undefined, deliberately, so it fails at link:
#
#   ld.lld: error: undefined symbol: _Unwind_Resume
#   >>> referenced by lock_guard.h (libc++)
#
# Dropping the request leaves -unwindlib=platform, which is libgcc_s. Mixing it
# with compiler-rt builtins is supported and is what resolves the symbol.
CFLAGS:remove = "-unwindlib=libunwind"
CXXFLAGS:remove = "-unwindlib=libunwind"


# This hook is the most involved in the layer and its cmake output is otherwise
# captured by the native-assets builder, which is how a quiet find_package
# failure hid as an unwinder error. Keep the hook log in the task log.
FLUTTER_NATIVE_VERBOSE = "1"

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

    # Warn, not note: a note lands in the task log, which CI prints only for
    # logs that contain an error. This is the one line that says whether the
    # hook will see with_firebase at all.
    bb.warn('firebase_ffi_demo: wrote into %s:%s'
            % (pubspec, block.replace('\n', ' ')))
}
firebase_ffi_write_user_defines[vardeps] += "\
    FIREBASE_FFI_PRODUCTS \
    FIREBASE_FFI_SDK_PREFIX \
    "
# do_compile, not do_configure: the hook reads the pubspec when flutter build
# runs, and anything between the two tasks that restores project files would
# undo an earlier rewrite. This is the last point before the hook sees it.
do_compile[prefuncs] += "firebase_ffi_write_user_defines"

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

# Assert the library actually has Firebase in it.
#
# find_package(firebase_cpp_sdk CONFIG QUIET) is quiet, so a sysroot missing the
# SDK does not fail the build -- it produces the transport-only library, which
# fails on the target at the first call as "build has no Firebase SDK". That is
# exactly what a missing ${prefix}/src in the SDK recipe's SYSROOT_DIRS did.
#
# Checked on the installed copy, so it covers the install path too, and by
# symbol rather than by size: the archives are static, so the Firebase entry
# points this binds are defined in the library or they are not there at all.
do_install[postfuncs] += "firebase_ffi_assert_linked"
firebase_ffi_assert_linked() {
    found=""
    for lib in $(find ${D} -name 'libfirebase_ffi.so' 2>/dev/null); do
        found="yes"
        if ! ${NM} -D --defined-only "$lib" 2>/dev/null | grep -q '_ZN8firebase'; then
            # Say why, not just that. The hook's cmake output is captured by the
            # native-assets builder and its own task log is not printed by CI, so
            # a bare assertion sends you back for another two-hour build.
            bbwarn "no firebase:: symbols in $lib. What the sysroot has:"
            for probe in \
                ${STAGING_DIR_HOST}${libdir}/cmake/firebase_cpp_sdk/firebase_cpp_sdk-config.cmake \
                ${STAGING_DIR_HOST}${prefix}/src/firebase-cpp-sdk/app/src/include/firebase/app.h \
                ${STAGING_DIR_HOST}${libdir}/firebase-cpp-sdk/app/libfirebase_app.a; do
                if [ -e "$probe" ]; then bbwarn "  present: $probe"; else bbwarn "  MISSING: $probe"; fi
            done
            # The hook's own CMakeCache.txt is the answer: firebase_cpp_sdk_DIR
            # is either the directory find_package resolved or NOTFOUND.
            find ${S}/${FLUTTER_APPLICATION_PATH}/.dart_tool -name CMakeCache.txt 2>/dev/null |
            while read -r cache; do
                bbwarn "  cache: $cache"
                grep -E "^(firebase_cpp_sdk_DIR|CMAKE_PREFIX_PATH|CMAKE_SYSROOT|CMAKE_FIND_ROOT_PATH|FDB_WITH_FIREBASE)" \
                    "$cache" 2>/dev/null | sed 's/^/    /'
            done | while read -r line; do bbwarn "$line"; done
            bbfatal "$lib is the transport-only library: find_package did not \
resolve the SDK. See the probes above -- a missing header path means \
firebase-cpp-sdk did not stage it (SYSROOT_DIRS)."
        fi
        bbnote "$(basename $lib): firebase symbols present"
    done
    if [ -z "$found" ]; then
        bbfatal "no libfirebase_ffi.so was installed; the native-assets hook did not produce one"
    fi
}

# The SDK's secure store and installation id, at runtime as well as link time.
RDEPENDS:${PN} += "libsecret util-linux-libuuid"
