#
# flutter-app class wrapper to enable building native plugins
# during a Flutter app build.
#
# For apps whose Dart packages carry a native-assets build hook
# (hook/build.dart) that drives CMake. The hook runs inside `flutter build`, so
# it cannot see OE's cross environment; this class puts a cmake wrapper on PATH
# that injects the toolchain file and pkg-config settings, then installs the
# resulting libraries into the app bundle.
#
# Inherit this INSTEAD OF flutter-app.
#

TOOLCHAIN = "clang"
# Required to make dart happy
DEPENDS_append = " lld-native"

# NOTE: conf/include/flutter-app.inc already requires gn-utils.inc and appends
# "--target-platform linux-${@gn_target_arch_name(d)}" to FLUTTER_BUILD_ARGS.
# This class must not repeat that, or --target-platform is passed twice.

# Native-assets hook output.
#
# conf/include/common.inc runs `flutter build` via run_command(), which captures
# combined stdout+stderr and emits it with bb.note() -- so the build's own output
# already lands in ${WORKDIR}/temp/log.do_compile. But Dart's native-assets
# builder captures each hook/build.dart's stdout/stderr itself and only re-emits
# it at higher verbosity, so a hook's compiler output is invisible unless flutter
# is verbose. Set FLUTTER_NATIVE_VERBOSE = "1" (recipe or local.conf) to get it.
#
# The builder also leaves per-hook logs under
# ${S}/${FLUTTER_APPLICATION_PATH}/.dart_tool/native_assets_builder/; those are
# dumped into the task log by the do_dump_hook_logs task below when verbose is on.
FLUTTER_NATIVE_VERBOSE ??= "0"
FLUTTER_BUILD_ARGS_append = "${@' --verbose' if d.getVar('FLUTTER_NATIVE_VERBOSE') == '1' else ''}"

# flutter-app's do_compile is a PYTHON task, so a shell do_compile:append()
# would be parsed as python and fail. Use a separate task instead.
do_dump_hook_logs() {
    if [ "${FLUTTER_NATIVE_VERBOSE}" != "1" ]; then
        return 0
    fi
    nab="${S}/${FLUTTER_APPLICATION_PATH}/.dart_tool/native_assets_builder"
    if [ ! -d "$nab" ]; then
        bbnote "no native_assets_builder log dir at $nab"
        return 0
    fi
    bbnote "==== native-assets hook logs ===="
    find "$nab" -type f \( -name "*.txt" -o -name "*.log" -o -name "stdout*" -o -name "stderr*" \) |
    while read -r f; do
        bbnote "---- $f"
        sed 's/^/    /' "$f" || true
    done
}
addtask dump_hook_logs after do_compile before do_install

# Mask out build path in compiled plugin code
DEBUG_PREFIX_MAP += "-ffile-prefix-map=${PUB_CACHE}/hosted/pub.dev=${TARGET_DBGSRC_DIR}"

# Assume cmake and pkgconfig will be used for non-trivial native plugins
inherit cmake pkgconfig

# Skip cmake do_configure
do_configure[noexec] = "1"
do_compile[prefuncs] += "flutter_native_cmake_setup flutter_native_toolchain_setup flutter_native_toolchain_env flutter_native_path_setup"

# Tell the code-asset hooks which toolchain to use.
#
# A hook built through `flutter build bundle` gets no compiler config: on Linux
# the flutter tool reads one only from the app's CMake cache, and a bundle build
# has no CMake app project. The hook then falls back to whatever
# native_toolchain_c finds on the build host, which cross-compiles to the wrong
# machine entirely -- an x86-64 library inside an aarch64 package. See
# meta-flutter#801.
#
# The patched SDK reads FLUTTER_HOOK_CC / _AR / _LD, but CCompilerConfig carries
# only paths, with nowhere to put --target, --sysroot or the rest of OE's flags.
# So point it at wrappers, the same trick the cmake wrapper above uses.
flutter_native_toolchain_setup() {

    # native_toolchain_c drives both compiling and linking through the compiler;
    # it never execs the linker itself. Link flags on a compile-only invocation
    # make clang warn "linker input unused", which is fatal for a hook built
    # with -Werror -- so only add LDFLAGS when this is not a -c call.
    cat > ${WORKDIR}/flutter-hook-cc <<HOOK_CC_EOF
#!/bin/sh
for arg in "\$@"; do
    if [ "\${arg}" = "-c" ]; then
        exec ${CC} ${CFLAGS} "\$@"
    fi
done
exec ${CC} ${CFLAGS} ${LDFLAGS} "\$@"
HOOK_CC_EOF

    # Invoked directly as `ar rc <lib> <objects>`.
    cat > ${WORKDIR}/flutter-hook-ar <<HOOK_AR_EOF
#!/bin/sh
exec ${AR} "\$@"
HOOK_AR_EOF

    # CCompilerConfig requires a linker even though the C builder links through
    # the compiler driver. Give it the real one rather than a stub.
    cat > ${WORKDIR}/flutter-hook-ld <<HOOK_LD_EOF
#!/bin/sh
exec ${LD} "\$@"
HOOK_LD_EOF

    chmod +x ${WORKDIR}/flutter-hook-cc ${WORKDIR}/flutter-hook-ar ${WORKDIR}/flutter-hook-ld
}

FLUTTER_HOOK_CC = "${WORKDIR}/flutter-hook-cc"
FLUTTER_HOOK_AR = "${WORKDIR}/flutter-hook-ar"
FLUTTER_HOOK_LD = "${WORKDIR}/flutter-hook-ld"

# Put them where the flutter invocation will actually see them.
#
# common.inc's do_compile is a python task that seeds its subprocess
# environment from os.environ. bitbake's `export` only populates the
# environment of *shell* tasks, so exporting these would set them for nothing
# and the hook would go on resolving a host compiler. Prefuncs run in the same
# interpreter as the python task body, so mutating os.environ here reaches it.
python flutter_native_toolchain_env() {
    import os
    for var in ('FLUTTER_HOOK_CC', 'FLUTTER_HOOK_AR', 'FLUTTER_HOOK_LD'):
        value = d.getVar(var)
        if value:
            os.environ[var] = value
}

# oe-core grew OECMAKE_ARGS in scarthgap. On this release cmake_do_configure
# still writes the same arguments out inline, so there is no variable for the
# wrapper below to reuse and ${OECMAKE_ARGS} expands to nothing. cmake then
# configures with no toolchain file and picks a compiler off PATH -- bitbake's
# hosttools gcc -- so a hook that drives cmake itself compiles for the build
# host and never sees the target sysroot:
#
#   <command-line>: fatal error: systemd/sd-bus.h: No such file or directory
#
# Weak default, so a release that does define OECMAKE_ARGS still wins. The list
# mirrors this release's own cmake_do_configure.
OECMAKE_ARGS ??= "\
    -DCMAKE_INSTALL_PREFIX:PATH=${prefix} \
    -DCMAKE_INSTALL_BINDIR:PATH=${@os.path.relpath(d.getVar('bindir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_SBINDIR:PATH=${@os.path.relpath(d.getVar('sbindir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_LIBEXECDIR:PATH=${@os.path.relpath(d.getVar('libexecdir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_SYSCONFDIR:PATH=${sysconfdir} \
    -DCMAKE_INSTALL_SHAREDSTATEDIR:PATH=${@os.path.relpath(d.getVar('sharedstatedir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_LOCALSTATEDIR:PATH=${localstatedir} \
    -DCMAKE_INSTALL_LIBDIR:PATH=${@os.path.relpath(d.getVar('libdir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_INCLUDEDIR:PATH=${@os.path.relpath(d.getVar('includedir'), d.getVar('prefix') + '/')} \
    -DCMAKE_INSTALL_DATAROOTDIR:PATH=${@os.path.relpath(d.getVar('datadir'), d.getVar('prefix') + '/')} \
    -DPYTHON_EXECUTABLE:PATH=${PYTHON} \
    -DPython_EXECUTABLE:PATH=${PYTHON} \
    -DPython3_EXECUTABLE:PATH=${PYTHON} \
    -DLIB_SUFFIX=${@d.getVar('baselib').replace('lib', '')} \
    -DCMAKE_INSTALL_SO_NO_EXE=0 \
    -DCMAKE_TOOLCHAIN_FILE:FILEPATH=${WORKDIR}/toolchain.cmake \
    -DCMAKE_NO_SYSTEM_FROM_IMPORTED=1 \
    -DCMAKE_EXPORT_NO_PACKAGE_REGISTRY=ON \
    -DFETCHCONTENT_FULLY_DISCONNECTED=ON \
"

flutter_native_cmake_setup() {
    # Create cmake wrapper to insert OE environment options
    cat > ${WORKDIR}/cmake <<CMAKE_WRAPPER_EOF
#!/bin/sh
export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}"
export PKG_CONFIG_LIBDIR="${PKG_CONFIG_LIBDIR}"
export PKG_CONFIG_SYSROOT_DIR="${PKG_CONFIG_SYSROOT_DIR}"
export PKG_CONFIG_DISABLE_UNINSTALLED="${PKG_CONFIG_DISABLE_UNINSTALLED}"
export PKG_CONFIG_SYSTEM_LIBRARY_PATH="${PKG_CONFIG_SYSTEM_LIBRARY_PATH}"
export PKG_CONFIG_SYSTEM_INCLUDE_PATH="${PKG_CONFIG_SYSTEM_INCLUDE_PATH}"

configuring="true"
for arg in "\$@"; do
    if [ "\${arg}" = "--build" ]; then
        configuring="false"
    fi
done
CMAKE_ARGS=""
if [ "\${configuring}" = "true" ]; then
    CMAKE_ARGS="${OECMAKE_ARGS}"
fi
exec ${RECIPE_SYSROOT_NATIVE}${bindir}/cmake \${CMAKE_ARGS} "\$@"
CMAKE_WRAPPER_EOF
    chmod +x ${WORKDIR}/cmake
}

python flutter_native_path_setup() {
    # Ensure cmake wrapper is found
    path = d.getVar('PATH')
    workdir = d.getVar('WORKDIR')
    d.setVar('PATH', workdir + ':' + path)
}

do_install_append() {
    if [ -d ${S}/${FLUTTER_APPLICATION_PATH}/build/native_assets/linux ]; then
        cp -r ${S}/${FLUTTER_APPLICATION_PATH}/build/native_assets/linux/* \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/
    fi
}

# Ensure do_compile has a clean slate when it runs
do_compile[cleandirs] = "${S}/.dart_tool"

# Quiet QA warnings about debug libraries under /usr/share/flutter/.../lib/.debug
INSANE_SKIP_${PN}-dbg += " libdir"

inherit flutter-app
