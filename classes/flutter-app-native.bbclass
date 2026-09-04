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
DEPENDS:append = " lld-native"

# Force lld use and using compiler-rt and libunwind instead of libgcc.
# Ideally these could just be added to LDFLAGS, but they have to go into the
# general flags variables: the resulting CMAKE_<LANG>_LINK_FLAGS in the
# toolchain.cmake that cmake.bbclass writes do not appear to be used, perhaps
# from behaviour changes in cmake 4.3.
DEPENDS:append = " libunwind"
CFLAGS += "-rtlib=compiler-rt -unwindlib=libunwind -fuse-ld=lld"
CXXFLAGS += "-rtlib=compiler-rt -unwindlib=libunwind -fuse-ld=lld"

# Force libc++ instead of the default libstdc++, which is what upstream
# expects. TOOLCHAIN = "clang" alone does not get there: oe-core only selects
# libc++ when TC_CXX_RUNTIME is overridden for the whole toolchain build.
CXXFLAGS += "-stdlib=libc++"

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
FLUTTER_BUILD_ARGS:append = "${@' --verbose' if d.getVar('FLUTTER_NATIVE_VERBOSE') == '1' else ''}"

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
do_compile[prefuncs] += "flutter_native_cmake_setup flutter_native_path_setup"

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

# Native-assets libraries: install them, and put them where dlopen looks.
#
# The flutter tool records a bare file name for a Linux desktop build --
# isolated/native_assets/native_assets.dart writes
# KernelAssetAbsolutePath(Uri(path: fileName)), so the manifest entry is
# ["absolute", "libfoo.so"] with no directory. The engine hands that straight to
# dlopen (runtime/dart_isolate.cc, path_type == kAbsolute ->
# NativeAssets::DlopenAbsolute), and a name with no slash is resolved by the
# dynamic linker: DT_RUNPATH, LD_LIBRARY_PATH, ld.so.cache, default directories.
# flutter_assets/ is never consulted -- NativeAssetsManager only parses the
# manifest, it does not resolve against the asset store.
#
# So bundling the library is not enough; the directory holding it has to be on
# the linker search path. Hence the ld.so.conf.d fragment below.
#
# The libraries go in their own subdirectory rather than beside libapp.so, so
# that adding it to the search path does not also make libapp.so and the
# libflutter_engine.so symlink globally resolvable by name.
#
# Only release and profile get them, matching the AOT install in
# conf/include/flutter-app.inc. FLUTTER_RUNTIME_MODE cannot be used here: it is
# a shell loop variable in that file's do_install, and this append runs after
# the loop has exited, so it needs its own loop.
#
# One limitation worth knowing: conf/include/common.inc reuses a single build/
# directory across runtime modes and runs `flutter clean` between them, so at
# install time the hook output belongs to whichever mode was built last. With
# more than one mode enabled, each mode therefore gets that same binary.
do_install:append() {
    nadir="${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/native_assets/linux"

    if [ ! -d "$nadir" ] || [ -z "$(ls -A "$nadir" 2>/dev/null)" ]; then
        bbnote "no native-assets output at $nadir; nothing to install"
        return 0
    fi

    installed=""
    for mode in $(ls ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION}); do
        if ! echo "${FLUTTER_APP_RUNTIME_MODES}" | grep -qw "$mode"; then
            continue
        fi
        if [ "$mode" != "release" ] && [ "$mode" != "profile" ]; then
            continue
        fi
        dest="${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/$mode/lib/native_assets"
        install -d ${D}$dest
        # cp -r, not cp -a: -a preserves the source ownership, and the hook
        # output is owned by the build user, so the package ends up with files
        # owned by a uid that does not exist on target. do_package then fails
        # in the output hash with "getpwuid(): uid not found". Everything else
        # in this layer copies with cp -r for the same reason.
        cp -r "$nadir"/* ${D}$dest/
        bbnote "[$mode] native assets installed into $dest:" \
               "$(ls ${D}$dest | tr '\n' ' ')"
        installed="$installed $dest"
    done

    if [ -z "$installed" ]; then
        bbwarn "this app has native assets in $nadir but none were installed:" \
               "FLUTTER_APP_RUNTIME_MODES is '${FLUTTER_APP_RUNTIME_MODES}' and" \
               "they are only installed for release and profile"
        return 0
    fi

    # dlopen resolves these by bare name, so the directory has to be searched.
    # ldconfig runs at rootfs assembly, which is what turns this into cache
    # entries; an image built without it will not find the libraries.
    install -d ${D}${sysconfdir}/ld.so.conf.d
    conf="${D}${sysconfdir}/ld.so.conf.d/flutter-${PN}.conf"
    : > "$conf"
    for dest in $installed; do
        echo "$dest" >> "$conf"
    done
}

# :append rather than +=: conf/include/flutter-app.inc assigns FILES:${PN}
# outright, and it is inherited at the bottom of this file, so a += here is
# parsed first and then thrown away. Same trap as PUB_CACHE in common.inc.
FILES:${PN}:append = " ${sysconfdir}/ld.so.conf.d/flutter-${PN}.conf"

# Ensure do_compile has a clean slate when it runs.
#
# Narrowed to the hook output. Other tasks put things under .dart_tool that
# do_compile needs: pub-cache.bbclass resolves offline before do_compile and
# leaves package_config.json there, and wiping the directory would throw that
# resolution away and send pub back to the network to redo it.
#
# The path carries FLUTTER_APPLICATION_PATH because this layer supports an app
# in a subdirectory of its repository, where ${S}/.dart_tool is not the app's.
do_compile[cleandirs] += "${S}/${FLUTTER_APPLICATION_PATH}/.dart_tool/hooks_runner"

# Quiet QA warnings about debug libraries under /usr/share/flutter/.../lib/.debug
INSANE_SKIP:${PN}-dbg += " libdir"

inherit flutter-app
