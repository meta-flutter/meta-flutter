#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Engine"
DESCRIPTION = "Google Flutter Engine for use with Flutter applications"
AUTHOR = "Flutter Team"
HOMEPAGE = "https://github.com/flutter/flutter/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://engine/src/LICENSE;md5=537e0b52077bf0a616d0a0c8a79bc9d5"

REQUIRED_DISTRO_FEATURES = "opengl"

DEPENDS += "\
    zip-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'libx11 libxcb', '', d)} \
    "

DEPENDS:aarch64 += "\
    freetype \
    "

TOOLCHAIN = "clang"
TOOLCHAIN_NATIVE = "clang"
TC_CXX_RUNTIME = "llvm"
PREFERRED_PROVIDER_llvm = "clang"
PREFERRED_PROVIDER_llvm-native = "clang-native"
LIBCPLUSPLUS = "-stdlib=libc++"

require conf/include/gn-utils.inc
require conf/include/clang-utils.inc
require conf/include/flutter-version.inc

PV = "${FLUTTER_SDK_VERSION}"

SRC_URI_EXTRA = ""

SRC_URI = "\
    git://github.com/flutter/flutter.git;protocol=https;nobranch=1;name=flutter_sdk \
    gn://github.com/flutter/flutter.git \
    file://BUILD.gn.in \
    file://0003-gn-riscv32-and-riscv64.patch \
    file://0006-fml-fixes-text_input-compiler-warnings.patch \
    file://0001-flutter-third_party-swiftshader-pointer-cast-to-void.patch \
    file://0002-flutter-third_party-swiftshader-llvm-16.0-required-f.patch \
    ${SRC_URI_EXTRA} \
    "
SRCREV_FORMAT .= "_flutter_sdk"
SRCREV_flutter_sdk = "${@get_flutter_hash(d)}"

# UNPACKDIR arrived after scarthgap, where fetches still land directly in
# WORKDIR rather than in a sources/ subdirectory of it. Master's
# "${UNPACKDIR}/gn" expands to nothing here, and do_unpack rejects the result.
S = "${WORKDIR}/gn"

# musl-specific patches.
#
# The libc++ musl patches (0001-libcxx-uglify-support-musl,
# 0002-libcxx-return-type-in-wcstoull_l) were dropped: upstream libc++ selects
# its native musl locale path when told it is on musl, which
# FLUTTER_ENGINE_CXX_LIBC_FLAGS already does via -D_LIBCPP_HAS_MUSL_LIBC. They
# stopped applying at 3.47.1 and are redundant with that define.
SRC_URI:libc-musl += "\
    file://0003-suppres-musl-libc-warning.patch;patchdir=engine/src/flutter/third_party/dart \
    "

inherit gn-fetcher features_check pkgconfig

# gn writes its output inside the sync directory; keep it out of the
# cached tarball, which is also what a mirror would serve.
GN_PACK_EXCLUDES = "./engine/src/out"

# 3.47.1 gives the openjdk cipd entry its own condition and keeps the comment
# the old rewrite targeted, so injecting a second 'condition' key made gclient
# reject DEPS outright ("duplicate key in dictionary: condition"). Fold the gate
# into the upstream expression instead: the JDK stays unfetched when android
# deps are off, and upstream's arm64-host guard is preserved rather than
# replaced. Dropping the rewrite would also parse, but then x86-64 hosts fetch a
# JDK nothing opens and it ships inside the cached source tarball.
# The anchors are deliberately quote-free -- this is a plain bitbake "..."
# variable and bitbake does not unescape \".
GN_DEPS_SED_PATCHES:pn-flutter-engine:aarch64 = "not (host_os|download_android_deps and not (host_os"

# For gn.bbclass
GN_CUSTOM_VARS ?= '\
{\
    "download_android_deps": False, \
    "download_windows_deps": False, \
    "download_linux_deps": False,   \
}'
GN_CUSTOM_VARS:aarch64 ?= '\
{ \
    "download_android_deps": False, \
    "download_windows_deps": False, \
    "download_linux_deps": False,   \
    "download_fuchsia_deps": False, \
}'
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"
COMPATIBLE_MACHINE:riscv32 = "(.*)"
COMPATIBLE_MACHINE:riscv64 = "(.*)"

PACKAGECONFIG ??= "\
    debug profile release \
    backtrace \
    embedder-for-target \
    fontconfig \
    fstack-protector \
    dart-dynamic-modules \
    dart-secure-socket \
    "


PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[backtrace] = "--backtrace,--no-backtrace"
PACKAGECONFIG[canvaskit] = "--build-canvaskit"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[dart-dynamic-modules] = "--dart-dynamic-modules, --no-dart-dynamic-modules"
PACKAGECONFIG[dart-secure-socket] = ",--no-dart-secure-socket"
PACKAGECONFIG[debug] = "--runtime-mode debug"
PACKAGECONFIG[desktop-embeddings] = ",--disable-desktop-embeddings, glib-2.0 gtk+3"
PACKAGECONFIG[embedder-examples] = "--build-embedder-examples,--no-build-embedder-examples"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig,,fontconfig fontconfig-native"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk,--no-full-dart-sdk"
PACKAGECONFIG[fstack-protector] = "--fstack-protector"
PACKAGECONFIG[glfw-shell] = "--build-glfw-shell,--no-build-glfw-shell, glfw"
PACKAGECONFIG[glfw-swiftshader] = "--use-glfw-swiftshader"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[lto] = "--lto, --no-lto"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[prebuilt-dart-sdk] = "--prebuilt-dart-sdk,--no-prebuilt-dart-sdk"
PACKAGECONFIG[profile] = "--runtime-mode profile"
PACKAGECONFIG[release] = "--runtime-mode release"
PACKAGECONFIG[slimpeller] = "--slimpeller"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer,--no-clang-static-analyzer"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[trace-gn] = "--trace-gn"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[unittests] = "--enable-unittests,--no-enable-unittests, glib-2.0 gtk+3 xinerama"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[verbose] = "--verbose"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"

RDEPENDS:${PN} = "\
    ${@bb.utils.contains('PACKAGECONFIG', 'fontconfig', 'fontconfig', '', d)} \
"

CLANG_BUILD_ARCH = "${@clang_build_arch(d)}"
CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${S}/engine/src/flutter/buildtools/linux-${CLANG_BUILD_ARCH}/clang"

# Use system clang for riscv64; required for linking
CLANG_PATH:riscv64 = "${STAGING_DIR_NATIVE}/usr"

GN_ARGS = "\
    ${PACKAGECONFIG_CONFARGS} \
    --build-engine-artifacts \
    --clang \
    --no-goma --no-rbe \
    --no-stripped \
    --no-default-linux-sysroot \
    --gn-args=dart_include_wasm_opt=false \
    --target-os linux \
    --linux-cpu ${@gn_target_arch_name(d)} \
    --target-sysroot ${STAGING_DIR_TARGET} \
    --target-toolchain ${CLANG_PATH} \
    --target-triple ${@gn_clang_triple_prefix(d)} \
    "

# dart_include_wasm_opt: upstream's flutter/tools/gn already sets this false for
# host builds, with the comment "it doesn't build properly with our gn
# configuration" -- but only in the is_host_build() branch, which a cross build
# with --no-prebuilt-dart-sdk never reaches. Without it, linking the wasm-opt
# host tool fails with duplicate __cxxabiv1/__cxa_* symbols (libc++abi pulled in
# twice). Nothing in the embedder needs wasm-opt; it is dart2wasm tooling.

# Enable ccache when the ccache class is inherited and CCACHE_DISABLE is false
GN_ARGS += "${@'--gn-args=use_ccache=true' if bb.data.inherits_class('ccache', d) and not bb.utils.to_boolean(d.getVar('CCACHE_DISABLE')) else ''}"

GN_ARGS:append:libc-musl = "\
    --no-backtrace \
    "

GN_ARGS:append:armv7 = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " --arm-float-abi ${TARGET_FPU}"

GN_TUNE_ARGS = ""
GN_TUNE_ARGS:append:aarch64 = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7 = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7a = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7ve = "arm_tune = \"${@gn_get_tune_features(d)}\""

TMP_OUT_DIR = "${@get_gn_tmp_out_dir_relative(d)}"

GN_ARGS_LESS_RUNTIME_MODES = "${@get_gn_args_less_runtime(d)}"

FLUTTER_ENGINE_INSTALL_PREFIX ??= "${datadir}/flutter/${FLUTTER_SDK_VERSION}"

FLUTTER_ENGINE_DEBUG_PREFIX_MAP ?= " \
    -fmacro-prefix-map=${S}/engine/src=${TARGET_DBGSRC_DIR} \
    -fdebug-prefix-map=${S}/engine/src=${TARGET_DBGSRC_DIR} \
    -fmacro-prefix-map=${B}=${TARGET_DBGSRC_DIR} \
    -fdebug-prefix-map=${B}=${TARGET_DBGSRC_DIR} \
    -fdebug-prefix-map=${STAGING_DIR_HOST}= \
    -fmacro-prefix-map=${STAGING_DIR_HOST}= \
    -fdebug-prefix-map=${STAGING_DIR_NATIVE}= \
    "
FLUTTER_ENGINE_DEBUG_FLAGS ?= "-g -feliminate-unused-debug-types ${FLUTTER_ENGINE_DEBUG_PREFIX_MAP}"
FLUTTER_ENGINE_CXX_LIBC_FLAGS ?= ""
# flatbuffers keys its locale-independent path on _XOPEN_VERSION >= 700, which
# musl advertises without providing strtoll_l/strtoull_l; force it off.
FLUTTER_ENGINE_CXX_LIBC_FLAGS:append:libc-musl = "-D_LIBCPP_HAS_MUSL_LIBC -DFLATBUFFERS_LOCALE_INDEPENDENT=0"

WAYLAND_IS_PRESENT = "${@bb.utils.filter('DISTRO_FEATURES', 'wayland', d)}"
X11_IS_PRESENT = "${@bb.utils.filter('DISTRO_FEATURES', 'x11', d)}"


do_configure() {

    cd ${S}/engine/src

    #
    # disable default sysroot
    #
    # Historically done by editing build/config/sysroot.gni, but that no longer
    # works: flutter/tools/gn does parser.set_defaults(default_linux_sysroot=True)
    # and passes use_default_linux_sysroot=true into gn_args, overriding whatever
    # the .gni declares (which itself now defaults to false). Without the
    # --no-default-linux-sysroot flag in GN_ARGS, gn asserts on the missing
    # bundled debian_bullseye sysroot instead of using --target-sysroot.
    # The sed is kept for older engines where the .gni default was true.
    sed -i "s|use_default_linux_sysroot = true|use_default_linux_sysroot = false|g" build/config/sysroot.gni

    #
    # host-toolchain fontconfig
    #
    # //third_party:fontconfig is only libs = [ "fontconfig" ] -- it adds no
    # include or library path, so it relies on the header being in the
    # compiler's default search path. Target compiles get it from
    # --target-sysroot, but the host toolchain (clang_x64/) has no sysroot and
    # falls through to the build host's /usr/include. That is only present if
    # the host distro happens to ship fontconfig headers, which is not
    # something the build may assume, and it is why this only ever failed in a
    # clean container.
    #
    # The host consumer is impellerc, the Impeller shader compiler: it links
    # host skia, whose fontmgr_fontconfig port has
    # public_deps = [ "//third_party:fontconfig" ]. impellerc ships in
    # engine_sdk.zip under sdk/clang_${CLANG_BUILD_ARCH}/ and runs on the build
    # host, so it needs fontconfig-native, not the target's fontconfig.
    #
    # Note the label: there is no //third_party/BUILD.gn at the engine root, so
    # .gn's secondary_source sends it to flutter/build/secondary/. skia has its
    # own config("system_fontconfig"), but nothing in this graph reaches it.
    # The source_set has no sources of its own, so the paths have to ride out
    # on a public_config to land on dependents' compile lines. Scoped to
    # host_toolchain so native headers can never preempt the target sysroot.
    if ${@bb.utils.contains('PACKAGECONFIG', 'fontconfig', 'true', 'false', d)}; then

        _fc_gn="flutter/build/secondary/third_party/BUILD.gn"

        # idempotent: do_configure can be forced without a fresh unpack, and a
        # second append would be a duplicate gn definition.
        if ! grep -q "yocto_native_fontconfig" "$_fc_gn"; then

        sed -i -e "s|^\([[:space:]]*\)libs = \[ \"fontconfig\" \]|\1libs = [ \"fontconfig\" ]\n\1if (current_toolchain == host_toolchain) {\n\1  public_configs = [ \":yocto_native_fontconfig\" ]\n\1} else {\n\1  public_configs = [ \":yocto_target_fontconfig\" ]\n\1}|" "$_fc_gn"

        # written with echo rather than a heredoc: bitbake ends a shell
        # function at a column-0 "}", which a gn block would otherwise contain.
        {
            echo ""
            echo "config(\"yocto_native_fontconfig\") {"
            echo "  include_dirs = [ \"${STAGING_INCDIR_NATIVE}\" ]"
            echo "  lib_dirs = [ \"${STAGING_LIBDIR_NATIVE}\" ]"
            # -rpath-link only: the linker has to open libfontconfig.so and
            # resolve its transitive deps, but -Wl,--as-needed and
            # --gc-sections drop the library from DT_NEEDED (impellerc
            # references no Fc* symbols), so a runtime -rpath would bake a
            # workdir path in for a library that is never loaded.
            echo "  ldflags = [ \"-Wl,-rpath-link,${STAGING_LIBDIR_NATIVE}\" ]"
            echo "}"
            # The target link carries the same unconditional -lfontconfig. On
            # riscv64 the target toolchain is the native clang (see CLANG_PATH),
            # whose built-in search path covers ${STAGING_LIBDIR_NATIVE} -- so
            # once fontconfig-native is staged there, -lfontconfig resolves to
            # the host library and lld rejects it as incompatible with
            # elf64-littleriscv. Name the target sysroot explicitly so it wins.
            echo ""
            echo "config(\"yocto_target_fontconfig\") {"
            echo "  lib_dirs = [ \"${STAGING_LIBDIR}\" ]"
            echo "}"
        } >> "$_fc_gn"

        fi

        grep -q "public_configs = \[ \":yocto_native_fontconfig\" \]" "$_fc_gn" || \
            bbfatal "failed to patch source_set(\"fontconfig\") in $_fc_gn"
    fi

    #
    # vulkan_headers override: enables DRM case
    #

    # Match the assignment however it is spelled: 3.47.1 writes
    # "vulkan_use_x11 = !is_minimal_linux" (and hardcodes wayland false), so the
    # old "= true" patterns silently matched nothing and vulkan.h then pulled in
    # xcb/xcb.h on a distro without x11.
    test -z "$WAYLAND_IS_PRESENT" && sed -i -E "s|^([[:space:]]*)vulkan_use_wayland = .*|\1vulkan_use_wayland = false|" build_overrides/vulkan_headers.gni
    test -z "$X11_IS_PRESENT"     && sed -i -E "s|^([[:space:]]*)vulkan_use_x11 = .*|\1vulkan_use_x11 = false|" build_overrides/vulkan_headers.gni

    #
    # remove x11 package check if x11 is not available
    #
    test -z $X11_IS_PRESENT && sed -i '/^pkg_config("x11") {/,/^}$/d' flutter/shell/platform/linux/config/BUILD.gn

    #
    # fix build without wayland
    #
    test -z $WAYLAND_IS_PRESENT && sed -i "s|ozone_platform_wayland = true|ozone_platform_wayland = false|g" build/config/BUILDCONFIG.gn
    test -z $X11_IS_PRESENT && sed -i "s|ozone_platform_x11 = true|ozone_platform_x11 = false|g" build/config/BUILDCONFIG.gn 

    #
    # fix build with musl libc
    #
    # swiftshader vendors an LLVM whose Linux config.h assumes glibc. Which copy
    # ships varies by engine version (llvm-subzero and/or llvm-<ver>), so cover
    # both, and all the glibc-only symbols musl lacks -- not just HAVE_MALLINFO.
    if [ "${TCLIBC}" = "musl" ]; then
        for scfg in \
            flutter/third_party/swiftshader/third_party/llvm-subzero/build/Linux/include/llvm/Config/config.h \
            flutter/third_party/swiftshader/third_party/llvm-*/configs/linux/include/llvm/Config/config.h; do
            [ -f "$scfg" ] && sed -i -E \
                's@^#define (HAVE_MALLINFO2?|HAVE_BACKTRACE|HAVE_EXECINFO_H) 1$@/* #undef \1 */@' \
                "$scfg"
        done
    fi

    #
    # Custom Build config
    #
    cp ${WORKDIR}/BUILD.gn.in build/toolchain/custom/BUILD.gn
    sed -i "s|@DEBUG_FLAGS@|${FLUTTER_ENGINE_DEBUG_FLAGS}|g" build/toolchain/custom/BUILD.gn
    sed -i "s|@CXX_LIBC_FLAGS@|${FLUTTER_ENGINE_CXX_LIBC_FLAGS}|g" build/toolchain/custom/BUILD.gn

    #
    # Configure each mode defined in PACKAGECONFIG
    #

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"
    bbnote "CLANG_BUILD_ARCH=${CLANG_BUILD_ARCH}"

    for MODE in $FLUTTER_RUNTIME_MODES; do

        # make it easy to parse
        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"
        ARGS_FILE="${BUILD_DIR}/args.gn"

        # remove in case this is a rebuild and you're not using rm_work.bbclass
        rm -rf ${BUILD_DIR} | true

        flutter/tools/gn ${GN_ARGS_LESS_RUNTIME_MODES} --runtime-mode ${MODE}

        echo ${GN_TUNE_ARGS} >> "${ARGS_FILE}"

        bbnote `cat ${ARGS_FILE}`
    done

    # external clang toolchain
    cd ${STAGING_DIR_TARGET}${libdir}

    test -e crtbeginS.o && rm crtbeginS.o
    test -e crtendS.o && rm crtendS.o
    test -e libgcc.a && rm libgcc.a

    ln -s "$(find -iname crtbeginS.o)" crtbeginS.o
    ln -s "$(find -iname crtendS.o)" crtendS.o
    ln -s "$(find -iname libgcc.a)" libgcc.a
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile() {

    cd ${S}/engine/src

    # required for dart: https://github.com/dart-lang/sdk/issues/41560
    export HOME=${WORKDIR}

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"

    for MODE in $FLUTTER_RUNTIME_MODES; do
        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"
        ninja -C "${BUILD_DIR}" $PARALLEL_MAKE
    done
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"


    for MODE in $FLUTTER_RUNTIME_MODES; do

        cd ${S}/engine/src

        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"

        #
        # Install directories
        #
        install -d ${D}${includedir}
        install -d ${D}${includedir}/flutter_linux
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/bin
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/data

        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_patched_sdk

        #
        # Headers
        #
        install -m 0644 ${BUILD_DIR}/flutter_embedder.h ${D}${includedir}/flutter_embedder.h

        #
        # Shared modules
        #
        cwd=$(pwd)

        cd ${BUILD_DIR}/so.unstripped
        for so_file in *; do

            # Copy the .so file to lib directory
            cp "$so_file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib/

            # Copy the .TOC file to SDK lib directory
            cp "../${so_file}.TOC" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib/
        done

        #
        # ICD files
        #
        cd ..
        for pat in *_icd.json; do
            for file in ${pat}; do
                # Skip if glob didn't match anything
                [ -e "$file" ] || continue
                # Move unstripped executable into bin
                cp  "$file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib/
            done
        done

        cd $cwd

        #
        # Data
        #
        install -m 0644 ${BUILD_DIR}/icudtl.dat ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/data/icudtl.dat

        #
        # Shader libraries
        #
        test -e ${BUILD_DIR}/shader_lib && \
            cp -r ${BUILD_DIR}/shader_lib \
                  ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib/

        #
        # flutter_linux headers
        #
        test -e ${BUILD_DIR}/flutter_linux && \
            cp -r ${BUILD_DIR}/flutter_linux ${D}${includedir}/flutter_linux

        #
        # Executables
        #
        test -e ${BUILD_DIR}/exe.unstripped && \
            cd ${BUILD_DIR}/exe.unstripped && \
            # Conditionally install only selected test executables when present
            for pat in *_benchmarks *_unittests *_rendertests *_example_gl *_example_vk *_testrunner; do
                for file in ${pat}; do
                    # Skip if glob didn't match anything
                    [ -e "$file" ] || continue
                    # Move unstripped executable into bin
                    cp "$file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/bin/
                done
            done
        cd $cwd

        # cross canadian artifacts
        cd ${BUILD_DIR}/clang_${CLANG_BUILD_ARCH}/exe.unstripped
        for file in *; do
            # copy the unstripped variant one up
             cp "../$file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}/
        done
        cd $cwd

        # include patched sdk for local-engine scenarios
        test -e ${BUILD_DIR}/flutter_patched_sdk && \
            cp -r ${BUILD_DIR}/flutter_patched_sdk ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/

        echo "${SRCREV}"                   > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_ENGINE_REPO_URL}" >> ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_SDK_VERSION}"      > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_sdk.version
        echo "${MODE}"                     > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter.runtime

        cp "${BUILD_DIR}/args.gn" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/args.gn

        cwd=$(pwd)
        cd ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/
        zip -r engine_sdk.zip sdk
        rm -rf sdk
        cd $cwd

    done
}
do_install[depends] += "zip-native:do_populate_sysroot"

PACKAGES =+ "\
    ${PN}-desktop-embeddings \
    ${PN}-impeller \
    ${PN}-sdk-dev \
    ${PN}-test \
    "

INSANE_SKIP:${PN} += " libdir"
INSANE_SKIP:${PN}-dbg += "libdir"
INSANE_SKIP:${PN}-desktop-embeddings += "libdir"
INSANE_SKIP:${PN}-impeller += " libdir"
INSANE_SKIP:${PN}-test += " buildpaths libdir"

#
# Per-runtime-mode packaging
#
# The engine installs debug/, profile/ and release/ side by side under
# ${datadir}/flutter/<ver>/. Shipping them in one package forces an image that
# only runs release to carry the debug and profile engines too (the debug one is
# by far the largest). Split them so a consumer can depend on exactly the mode it
# runs -- flutter-engine-release, etc.
#
# These go in PACKAGE_BEFORE_PN, not PACKAGES =+, so they are matched AFTER the
# specific packages (-dbg, -dev, -impeller, -desktop-embeddings, -sdk-dev, -test)
# and do not swallow the files those claim, while still winning over ${PN}.
PACKAGE_BEFORE_PN += "\
    ${PN}-debug \
    ${PN}-profile \
    ${PN}-release \
    ${PN}-jit-release \
    "

FILES:${PN}-debug       = "${datadir}/flutter/${FLUTTER_SDK_TAG}/debug"
FILES:${PN}-profile     = "${datadir}/flutter/${FLUTTER_SDK_TAG}/profile"
FILES:${PN}-release     = "${datadir}/flutter/${FLUTTER_SDK_TAG}/release"
FILES:${PN}-jit-release = "${datadir}/flutter/${FLUTTER_SDK_TAG}/jit_release"

# The engine libraries live under ${datadir}/flutter/<ver>/<mode>/lib/, not
# ${libdir}, so every package carrying them needs the libdir QA check skipped --
# the same exemption ${PN} already had before the split.
INSANE_SKIP:${PN}-debug       += " libdir"
INSANE_SKIP:${PN}-profile     += " libdir"
INSANE_SKIP:${PN}-release     += " libdir"
INSANE_SKIP:${PN}-jit-release += " libdir"

SUMMARY:${PN}-debug       = "Flutter engine - debug runtime mode"
SUMMARY:${PN}-profile     = "Flutter engine - profile runtime mode"
SUMMARY:${PN}-release     = "Flutter engine - release runtime mode"
SUMMARY:${PN}-jit-release = "Flutter engine - jit_release runtime mode"

# ${PN} becomes a pure metapackage: the per-mode packages above claim every
# directory under ${datadir}/flutter, so nothing is left for it to own. It must
# still be PRODUCED -- every consumer RDEPENDs on "flutter-engine" by name, and
# an empty package is skipped by default, which makes those dependencies
# unresolvable at do_rootfs ("nothing provides flutter-engine"). ALLOW_EMPTY
# keeps it, and its RDEPENDS pull in whichever modes were built, so existing
# consumers behave exactly as they did before the split.
FILES:${PN} = "\
    ${datadir}/flutter \
    "

ALLOW_EMPTY:${PN} = "1"
RDEPENDS:${PN} += "${@' '.join('${PN}-' + m.replace('_', '-') for m in bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d).split())}"

FILES:${PN}-dbg = "\
    ${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/.debug \
    "

FILES:${PN}-desktop-embeddings = "\
    ${@bb.utils.contains('PACKAGECONFIG', 'desktop-embeddings', '${datadir}/flutter/${FLUTTER_SDK_TAG}/*/lib/libflutter_linux*.so', '', d)} \
    "

FILES:${PN}-dev = "\
    ${includedir} \
    "

FILES:${PN}-impeller = "\
    ${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/libimpeller.so \
    ${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/libpath_ops.so \
    ${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/libtessellator.so \
    "

FILES:${PN}-sdk-dev = "\
    ${datadir}/flutter/${FLUTTER_SDK_TAG}/*/engine_sdk.zip \
    "

FILES:${PN}-test = "\
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/bin/*_benchmarks', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/bin/*_unittests', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/bin/*_rendertests', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/bin/*_example_*', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/bin/*_testrunner', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/*_icd.so', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/*_icd.json', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/*_swiftshader.so', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'unittests', '${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/libvulkan.so.1', '', d)} \
    "

python () {
    d.setVar('SRCREV', gn_get_engine_commit(d))

    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}

#
# libflutter_engine.so ABI interop gate
#
# The engine is built with clang and an in-tree libc++, but it is dropped into
# userlands built with GCC (OE-musl and Alpine both default to GCC). That only
# works because the engine exposes a pure C embedder ABI over the target libc --
# nothing else is a shared contract. These rules prove that self-containment
# instead of assuming it. Ported from emb_cli's engine_abi_gate.dart; fail-closed.
#
#   2  no C++ runtime in DT_NEEDED  -- libstdc++/libc++ must not be dynamic
#   3  DT_NEEDED libc matches TCLIBC -- catches a glibc engine on a musl image
#   4  no dynamic libgcc_s          -- unwinder must be static
#   5  no exported _Z* symbols      -- the internal libc++ must not leak
#   6  no undefined C++-runtime syms -- would pull libstdc++/libc++abi at runtime
#
# Rule 1 (ELF machine) is omitted: OE's own arch QA already covers it.
#
# Symbols versioned @GLIBC_ are provided by libc.so.6, an allowed NEEDED, so
# they are excluded from rule 6. That matters for the __cxa_atexit / __cxa_finalize
# / __cxa_thread_atexit_impl family, which shares the __cxa_ prefix but is C
# runtime, not C++ ABI -- musl carries no symbol version, so it is excluded by
# name rather than by version.
#
FLUTTER_ENGINE_ABI_GATE ??= "1"

python do_flutter_engine_abi_gate() {
    import glob, re, subprocess

    if d.getVar('FLUTTER_ENGINE_ABI_GATE') != '1':
        bb.note('flutter-engine ABI gate disabled')
        return

    dest = d.getVar('D')
    # The engine installs per runtime mode under ${datadir}/flutter/<ver>/<mode>/lib/,
    # not ${libdir} -- search the whole image so the gate cannot pass vacuously.
    sos = glob.glob(f'{dest}/**/libflutter_engine.so', recursive=True)
    if not sos:
        bb.fatal('ABI gate: no libflutter_engine.so found under ${D} -- '
                 'the gate would otherwise pass without checking anything')

    readelf = d.getVar('READELF') or (d.getVar('TARGET_PREFIX') + 'readelf')
    nm = d.getVar('NM') or (d.getVar('TARGET_PREFIX') + 'nm')
    tclibc = d.getVar('TCLIBC')

    cxx_runtime = re.compile(r'libstdc\+\+|libc\+\+')
    musl_libc = re.compile(r'ld-musl-|libc\.musl-')
    glibc_libc = re.compile(r'libc\.so\.6')
    exported_cxx = re.compile(r'^_Z')
    extern_cxx = re.compile(
        r'^(?:__cxa_(?!atexit|finalize|thread_atexit)'
        r'|__cxxabiv1|__gxx_personality|_ZSt|_ZNSt|_Zna|_Znw|_ZdlPv)')

    def run(cmd):
        try:
            return subprocess.run(cmd, capture_output=True, text=True,
                                  check=False).stdout
        except FileNotFoundError:
            bb.fatal(f'ABI gate: {cmd[0]} not found')

    violations = []
    for so in sos:
        dyn = run([readelf, '-d', so])
        needed = re.findall(r'NEEDED.*\[(.+?)\]', dyn)

        cxx = [n for n in needed if cxx_runtime.search(n)]
        if cxx:
            violations.append(f'[rule 2] {so}: links a C++ runtime: {", ".join(cxx)}')

        has_musl = any(musl_libc.search(n) for n in needed)
        has_glibc = any(glibc_libc.search(n) for n in needed)
        if tclibc == 'musl' and not has_musl:
            violations.append(f'[rule 3] {so}: expected musl libc, NEEDED={needed}')
        elif tclibc == 'glibc' and not has_glibc:
            violations.append(f'[rule 3] {so}: expected glibc libc, NEEDED={needed}')

        # Rule 4 asserts the compiler-rt contract that TC_CXX_RUNTIME=llvm and
        # PREFERRED_PROVIDER_libgcc=compiler-rt are meant to establish: the
        # unwinder is compiler-rt's, linked statically, so libgcc_s never
        # appears. That only holds where the engine links with its own bundled
        # clang. riscv64 overrides CLANG_PATH to the native clang because the
        # bundled one cannot link there, and the native clang defaults to
        # --rtlib=libgcc. Applying the rule to that configuration would report a
        # deviation the recipe deliberately chose, so it is scoped out by the
        # same condition that causes it, rather than dropped.
        native_clang = (d.getVar('CLANG_PATH') or '').startswith(
            d.getVar('STAGING_DIR_NATIVE') or '\0')
        if any('libgcc_s' in n for n in needed):
            if native_clang:
                bb.note(f'ABI gate: {so} links libgcc_s; expected, this target '
                        f'builds with the native clang (CLANG_PATH is under '
                        f'STAGING_DIR_NATIVE), which defaults to --rtlib=libgcc')
            else:
                violations.append(f'[rule 4] {so}: dynamic libgcc_s -- unwinder not static')

        defined = [l.split()[-1] for l in run([nm, '-D', '--defined-only', so]).splitlines() if l.split()]
        leaked = [x for x in defined if exported_cxx.match(x)][:5]
        if leaked:
            violations.append(f'[rule 5] {so}: exported C++ symbols leak libc++: {", ".join(leaked)}')

        undef = [l.split()[-1] for l in run([nm, '-D', '-u', so]).splitlines() if l.split()]
        ext = [x for x in undef if '@GLIBC_' not in x and extern_cxx.match(x)][:5]
        if ext:
            violations.append(f'[rule 6] {so}: undefined C++-runtime deps require '
                              f'libstdc++/libc++abi: {", ".join(ext)}')

    if violations:
        bb.fatal('flutter-engine ABI interop gate failed:\n  ' + '\n  '.join(violations))
    bb.note(f'flutter-engine ABI interop gate passed for {len(sos)} library(ies)')
}
addtask flutter_engine_abi_gate after do_install before do_package_qa
