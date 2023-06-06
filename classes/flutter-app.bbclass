#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#
# Helper class for building Flutter Application.
# Assumes that:
# - Flutter Application does not have a linux folder.  If it does it 
#   will incorrectly link to Linux GTK embedder.  We don't want that.
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

require conf/include/flutter-version.inc

DEPENDS += " \
    ca-certificates-native \
    cmake-native \
    compiler-rt \
    flutter-engine \
    flutter-sdk-native \
    libcxx \
    ninja-native \
    unzip-native \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

FLUTTER_PREBUILD_CMD ??= ""
FLUTTER_APPLICATION_PATH ??= "."
FLUTTER_BUILD_ARGS ??= "bundle -v"
APP_AOT_EXTRA_DART_DEFINES ??= ""
FLUTTER_APPLICATION_INSTALL_PREFIX ??= ""
FLUTTER_INSTALL_DIR = "${datadir}${FLUTTER_APPLICATION_INSTALL_PREFIX}/${PUBSPEC_APPNAME}"
APP_GEN_SNAPSHOT_FLAGS ??= ""

FLUTTER_APP_DISABLE_NATIVE_PLUGINS ??= ""

FLUTTER_PUB_CMD ??= "get"

PUB_CACHE = "${WORKDIR}/pub_cache"
PUB_CACHE_ARCHIVE = "flutter-pub-cache-${PUBSPEC_APPNAME}-${SRCREV}.tar.bz2"

FLUTTER_SDK = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"

FLUTTER_APP_RUNTIME_MODES ?= "release"


python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}

#
# Archive Pub Cache
#

addtask archive_pub_cache before do_patch after do_unpack
do_archive_pub_cache[network] = "1"
do_archive_pub_cache[dirs] = "${WORKDIR} ${DL_DIR}"
do_archive_pub_cache[depends] += " \
    flutter-sdk-native:do_populate_sysroot \
    pbzip2-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    "
python do_archive_pub_cache() {

    import errno
    import multiprocessing
    from   bb.fetch2 import FetchError
    from   bb.fetch2 import runfetchcmd

    localfile = d.getVar("PUB_CACHE_ARCHIVE")
    localpath = os.path.join(d.getVar("DL_DIR"), localfile)
    
    if os.access(localpath, os.R_OK):
        return

    workdir = d.getVar("WORKDIR")
    pub_cache = d.getVar("PUB_CACHE")
    os.makedirs(pub_cache, exist_ok=True)

    flutter_sdk = os.path.join(d.getVar("STAGING_DIR_NATIVE"), 'usr/share/flutter/sdk')
    app_root = os.path.join(d.getVar("S"), d.getVar("FLUTTER_APPLICATION_PATH"))
    pub_cmd = d.getVar("FLUTTER_PUB_CMD")

    pub_cache_cmd = \
        'export PUB_CACHE=%s; ' \
        'export XDG_CONFIG_HOME=%s;' \
        '%s/bin/flutter pub get;' \
        '%s/bin/flutter pub get --offline' % \
        (pub_cache, workdir, flutter_sdk, flutter_sdk)

    bb.note("Running %s in %s" % (pub_cache_cmd, app_root))
    runfetchcmd('%s' % (pub_cache_cmd), d, quiet=False, workdir=app_root)

    cp_cmd = \
        'mkdir -p %s/.project | true; ' \
        'cp -r .dart_tool %s/.project/ | true; ' \
        'cp -r .packages %s/.project/ | true; ' \
        'cp -r .dart_tool %s/.project/ | true; ' \
        'cp -r .flutter-plugins %s/.project/ | true; ' \
        'cp -r .flutter-plugins-dependencies %s/.project/ | true; ' \
        'cp -r .metadata %s/.project/ | true; ' \
        'cp -r .packages %s/.project/ | true; ' \
        % (pub_cache, pub_cache, pub_cache, pub_cache, pub_cache, pub_cache, pub_cache, pub_cache)

    bb.note("Running %s in %s" % (cp_cmd, app_root))

    runfetchcmd('%s' % (cp_cmd), d, quiet=False, workdir=app_root)

    bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
    pack_cmd = "tar -I \"pbzip2 -p%s\" -cf %s ./" % (bb_number_threads, localpath)

    bb.note("Running %s in %s" % (pack_cmd, pub_cache))
    runfetchcmd('%s' % (pack_cmd), d, quiet=False, workdir=pub_cache)

    if not os.path.exists(localpath):
        raise FetchError("The fetch command returned success for pub cache, but %s doesn't exist?!" % (localpath), localpath)

    if os.path.getsize(localpath) == 0:
        os.remove(localpath)
        raise FetchError("The fetch of %s resulted in a zero size file?! Deleting and failing since this isn't right." % (localpath), localpath)
}

#
# Restore Pub Cache
#

addtask restore_pub_cache before do_patch after do_archive_pub_cache
do_restore_pub_cache[dirs] = "${WORKDIR} ${DL_DIR}"
do_restore_pub_cache[depends] += " \
    pbzip2-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    "
python do_restore_pub_cache() {

    import multiprocessing
    import shutil
    import subprocess
    from   bb.fetch2 import subprocess_setup
    from   bb.fetch2 import UnpackError
    
    localfile = d.getVar("PUB_CACHE_ARCHIVE")
    localpath = os.path.join(d.getVar("DL_DIR"), localfile)

    bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
    cmd = 'pbzip2 -dc -p%s %s | tar x --no-same-owner -f -' % (bb_number_threads, localpath)
    unpackdir = d.getVar("PUB_CACHE")
    shutil.rmtree(unpackdir, ignore_errors=True)
    bb.utils.mkdirhier(unpackdir)
    path = d.getVar('PATH')
    if path: cmd = 'PATH=\"%s\" %s' % (path, cmd)
    bb.note("Running %s in %s" % (cmd, unpackdir))
    ret = subprocess.call(cmd, preexec_fn=subprocess_setup, shell=True, cwd=unpackdir)

    if ret != 0:
        raise UnpackError("Unpack command %s failed with return value %s" % (cmd, ret), localpath)

    # restore flutter pub get artifacts
    app_root = os.path.join(d.getVar("S"), d.getVar("FLUTTER_APPLICATION_PATH"))
    cmd = \
        'mv .project/.dart_tool %s/ | true; ' \
        'mv .project/.packages %s/ | true; ' \
        'mv .project/.dart_tool %s/ | true; ' \
        'mv .project/.flutter-plugins %s/ | true; ' \
        'mv .project/.flutter-plugins-dependencies %s/ | true; ' \
        'mv .project/.metadata %s/ | true; ' \
        'mv .project/.packages %s/ | true; ' \
        'rm -rf .project' % (app_root, app_root, app_root, app_root, app_root, app_root, app_root)
    bb.note("Running %s in %s" % (cmd, unpackdir))
    ret = subprocess.call(cmd, preexec_fn=subprocess_setup, shell=True, cwd=unpackdir)

    if ret != 0:
        raise UnpackError("Restore .dart_tool command %s failed with return value %s" % (cmd, ret), localpath)
}

#
# Build flutter_assets folder and AOT (libapp.so)
#
do_compile[network] = "1"
do_compile() {

    export PATH=${FLUTTER_SDK}/bin:$PATH
    export PUB_CACHE=${PUB_CACHE}
    export PKG_CONFIG_PATH=${STAGING_DIR_TARGET}/usr/lib/pkgconfig:${STAGING_DIR_TARGET}/usr/share/pkgconfig:${PKG_CONFIG_PATH}

    bbnote `env`

    cd ${S}/${FLUTTER_APPLICATION_PATH}

    ${FLUTTER_PREBUILD_CMD}

    # determine build type based on what flutter-engine installed.
    FLUTTER_RUNTIME_MODES="$(ls ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION})"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do

        if ! echo "${FLUTTER_APP_RUNTIME_MODES}" | grep -qw "$FLUTTER_RUNTIME_MODE"; then
            bbnote "Skipping build for: ${FLUTTER_RUNTIME_MODE}"
            continue
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] flutter build ${FLUTTER_BUILD_ARGS}: Starting"

        rm -rf build || true

        if [ "${FLUTTER_RUNTIME_MODE}" = "jit_release"]; then
            bbnote "flutter build ${FLUTTER_BUILD_ARGS} --local-engine"
            flutter build ${FLUTTER_BUILD_ARGS} --local-engine
        else
            bbnote "flutter build ${FLUTTER_BUILD_ARGS}"
            flutter build ${FLUTTER_BUILD_ARGS}
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] flutter build ${FLUTTER_BUILD_ARGS}: Completed\n"

        if [ "${FLUTTER_RUNTIME_MODE}" = "release" ] || [ "${FLUTTER_RUNTIME_MODE}" = "profile" ]; then

            bbnote "kernel_snapshot_${FLUTTER_RUNTIME_MODE}: Starting"

            FLUTTER_APP_SDK_ROOT="${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk/"
            FLUTTER_APP_VM_PRODUCT="false"
            if [ "${FLUTTER_RUNTIME_MODE}" = "release" ]; then
                FLUTTER_APP_SDK_ROOT="${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/"
                FLUTTER_APP_VM_PRODUCT="true"
            fi

            FLUTTER_APP_PROFILE_FLAGS=""
            FLUTTER_APP_VM_PROFILE="false"
            if [ "${FLUTTER_RUNTIME_MODE}" = "profile" ]; then
                FLUTTER_APP_PROFILE_FLAGS="--track-widget-creation"
                FLUTTER_APP_VM_PROFILE="true"
            fi

            FLUTTER_APP_DEBUG_FLAGS=""
            FLUTTER_APP_APP_DILL="--output-dill .dart_tool/flutter_build/*/app.dill"
            if [ "${FLUTTER_RUNTIME_MODE}" = "debug" ]; then
                FLUTTER_APP_DEBUG_FLAGS="--enable-asserts"
                FLUTTER_APP_APP_DILL=".dart_tool/flutter_build/*/app.dill"
            fi

            FLUTTER_APP_AOT_CMD="${FLUTTER_SDK}/bin/cache/dart-sdk/bin/dart \
                --verbose \
                --disable-analytics \
                --disable-dart-dev ${FLUTTER_SDK}/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
                --sdk-root ${FLUTTER_APP_SDK_ROOT} \
                --target=flutter \
                --no-print-incremental-dependencies \
                -Ddart.vm.profile=${FLUTTER_APP_VM_PROFILE} \
                -Ddart.vm.product=${FLUTTER_APP_VM_PRODUCT} \
                ${APP_AOT_EXTRA_DART_DEFINES} \
                ${FLUTTER_APP_DEBUG_FLAGS} ${FLUTTER_APP_PROFILE_FLAGS} --aot --tfa \
                --packages .dart_tool/package_config.json \
                ${FLUTTER_APP_APP_DILL} \
                --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
                package:${PUBSPEC_APPNAME}/main.dart"

            bbnote "${FLUTTER_APP_AOT_CMD}"

            $FLUTTER_APP_AOT_CMD

            bbnote "kernel_snapshot_${FLUTTER_RUNTIME_MODE}: Complete"

            # remove kernel_blob.bin to save space
            rm ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/kernel_blob.bin

            # create empty file for apps that check for kernel_blob.bin
            touch ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/kernel_blob.bin

            bbnote "aot_elf_${FLUTTER_RUNTIME_MODE}: Started"

            #
            # Extract Engine SDK
            #
            rm -rf ${S}/engine_sdk
            unzip ${STAGING_DATADIR}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/engine_sdk.zip -d ${S}/engine_sdk

            #
            # Create libapp.so
            #
            ${S}/engine_sdk/sdk/clang_x64/gen_snapshot \
                --deterministic \
                ${APP_GEN_SNAPSHOT_FLAGS} \
                --snapshot_kind=app-aot-elf \
                --elf=libapp.so \
                --strip \
                .dart_tool/flutter_build/*/app.dill

            bbnote "aot_elf_${FLUTTER_RUNTIME_MODE}: Complete"
        else
            bbnote "Not creating AOT: ${FLUTTER_RUNTIME_MODE}"
        fi
    done
}


do_install() {

    # determine build type based on what flutter-engine installed
    FLUTTER_RUNTIME_MODES="$(ls ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION})"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do

        if ! echo "${FLUTTER_APP_RUNTIME_MODES}" | grep -qw "$FLUTTER_RUNTIME_MODE"; then
            bbnote "Skipping install for: ${FLUTTER_RUNTIME_MODE}"
            continue
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter Bundle Assets: Installing"

        # App artifacts
        install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/flutter_assets
        install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib

        cp -r ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/* \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/flutter_assets/

        if [ "${FLUTTER_RUNTIME_MODE}" = "release" ] || [ "${FLUTTER_RUNTIME_MODE}" = "profile" ]; then

            bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter AOT: Installing ${FLUTTER_RUNTIME_MODE}"

            install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib
            cp ${S}/${FLUTTER_APPLICATION_PATH}/libapp.so \
                ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter Bundle Symlink: Installing"

        # Engine artifact symlinks
        ln -sfr ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/icudtl.dat \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/icudtl.dat

        ln -sfr ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_engine.so \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_engine.so
    done
}

INSANE_SKIP:${PN} += " ldflags libdir dev-so"

FILES:${PN} = "\
    ${bindir} \
    ${libdir} \
    ${FLUTTER_INSTALL_DIR} \
    "

RDEPENDS:${PN} = "flutter-engine"
