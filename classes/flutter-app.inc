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
require conf/include/app-utils.inc
require conf/include/common.inc


DEPENDS += " \
    ca-certificates-native \
    flutter-engine \
    flutter-sdk-native \
    unzip-native \
    "

FLUTTER_PREBUILD_CMD ??= ""
FLUTTER_APPLICATION_PATH ??= "."
FLUTTER_BUILD_ARGS ??= "bundle"
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
        '%s/bin/flutter config --no-analytics;' \
        '%s/bin/flutter pub get;' \
        '%s/bin/flutter pub get --offline' % \
        (pub_cache, workdir, flutter_sdk, flutter_sdk, flutter_sdk)

    bb.note("Running [%s] in %s" % (pub_cache_cmd, app_root))
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

    bb.note("Running [%s] in %s" % (cp_cmd, app_root))

    runfetchcmd('%s' % (cp_cmd), d, quiet=False, workdir=app_root)

    bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
    pack_cmd = "tar -I \"pbzip2 -p%s\" -cf %s ./" % (bb_number_threads, localpath)

    bb.note("Running [%s] in %s" % (pack_cmd, pub_cache))
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
    bb.note("Running [%s] in %s" % (cmd, unpackdir))
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
    bb.note("Running [%s] in %s" % (cmd, unpackdir))
    ret = subprocess.call(cmd, preexec_fn=subprocess_setup, shell=True, cwd=unpackdir)

    if ret != 0:
        raise UnpackError("Restore .dart_tool command %s failed with return value %s" % (cmd, ret), localpath)
}


do_compile[network] = "1"
python do_compile() {
    import shutil

    flutter_sdk = d.getVar('FLUTTER_SDK')

    env = os.environ
    env['PATH'] = '%s/bin:%s' % (flutter_sdk, d.getVar('PATH'))
    env['PUB_CACHE'] = '%s' % d.getVar('PUB_CACHE')

    staging_dir_target = d.getVar('STAGING_DIR_TARGET')
    env['PKG_CONFIG_PATH'] = '%s/usr/lib/pkgconfig:%s/usr/share/pkgconfig:%s' % \
        (staging_dir_target, staging_dir_target, d.getVar("PKG_CONFIG_PATH"))
    
    env = os.environ
    bb.note('%s' % env)

    source_dir = d.getVar('S')
    flutter_application_path = d.getVar('FLUTTER_APPLICATION_PATH')
    source_root = os.path.join(source_dir, flutter_application_path)
    cmd = d.getVar('FLUTTER_PREBUILD_CMD')
    if cmd is not None:
        run_command(d, cmd, source_root, env)

    datadir = d.getVar('datadir')
    flutter_sdk_version = d.getVar('FLUTTER_SDK_VERSION')
    
    # determine build type based on what flutter-engine installed.
    flutter_runtime_modes = os.listdir('%s%s/flutter/%s' % (staging_dir_target, datadir, flutter_sdk_version))

    flutter_app_runtime_modes = d.getVar('FLUTTER_APP_RUNTIME_MODES')
    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')

    for runtime_mode in flutter_runtime_modes:
        if runtime_mode not in flutter_app_runtime_modes:
            bb.note('Skipping build for: %s' % runtime_mode)
            continue

        bb.note('[%s] flutter build %s: Starting' % (runtime_mode, flutter_build_args))

        run_command(d, 'flutter clean', source_root, env)

        if runtime_mode is 'jit_release':
            bb.note('flutter build %s --local-engine' % flutter_build_args)
            cmd = ('flutter build %s --local-engine' % flutter_build_args)
            run_command(cmd, source_root, env)
        else:
            cmd = 'flutter build %s' % flutter_build_args
            run_command(d, cmd, source_root, env)

        bb.note('[%s] flutter build %s: Completed\n' % (runtime_mode, flutter_build_args))

        if runtime_mode is 'release' or 'profile':

            bb.note('kernel_snapshot_%s: Starting' % runtime_mode)

            flutter_app_sdk_root = '%s/bin/cache/artifacts/engine/common/flutter_patched_sdk/' % flutter_sdk
            flutter_app_vm_product = "false"
            if runtime_mode is 'release':
                flutter_app_sdk_root = '%s/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/' % flutter_sdk
                flutter_app_vm_product = "true"

            flutter_app_profile_flags = ''
            flutter_app_vm_profile = 'false'
            if runtime_mode is 'profile':
                flutter_app_profile_flags = '--track-widget-creation' 
                flutter_app_vm_profile = 'true'

            flutter_app_debug_flags = ''
            flutter_app_app_dill = '--output-dill .dart_tool/flutter_build/*/app.dill'
            if runtime_mode is 'debug':
                flutter_app_debug_flags = '--enable-asserts'
                flutter_app_app_dill = '.dart_tool/flutter_build/*/app.dill'

            flutter_source_file = ''
            flutter_source_package = ''
            flutter_source_defines = ''
            dart_plugin_registrant_file = '%s/%s/.dart_tool/flutter_build/dart_plugin_registrant.dart' % (source_dir, flutter_application_path)
            if os.path.isfile(dart_plugin_registrant_file):
                d.setVar("DART_PLUGIN_REGISTRANT_FILE", dart_plugin_registrant_file)
                filter_plugin_registrant(d)
                flutter_source_file = '--source file://%s' % dart_plugin_registrant_file
                flutter_source_package = '--source package:flutter/src/dart_plugin_registrant.dart'
                flutter_source_defines = '-Dflutter.dart_plugin_registrant=file://%s' % dart_plugin_registrant_file

            flutter_native_assets = ''
            if os.path.isfile('.dart_tool/flutter_build/*/native_assets.yaml'):
                flutter_native_assets = '--native-assets %s/%s/.dart_tool/flutter_build/*/native_assets.yaml' % (source_dir, flutter_application_path)

            cmd = '%s/bin/cache/dart-sdk/bin/dart \
                --verbose \
                --disable-analytics \
                --disable-dart-dev %s/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
                --sdk-root %s \
                --target=flutter \
                --no-print-incremental-dependencies \
                -Ddart.vm.profile=%s \
                -Ddart.vm.product=%s \
                %s %s %s --aot --tfa \
                --target-os linux \
                --packages .dart_tool/package_config.json \
                %s \
                --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
                %s %s %s %s \
                package:%s/main.dart' % (
                    flutter_sdk, flutter_sdk,
                    flutter_app_sdk_root,
                    flutter_app_vm_profile, flutter_app_vm_product,
                    d.getVar("APP_AOT_EXTRA_DART_DEFINES"),
                    flutter_app_debug_flags, flutter_app_profile_flags, flutter_app_app_dill,
                    flutter_source_file, flutter_source_package, flutter_source_defines,
                    flutter_native_assets,
                    d.getVar("PUBSPEC_APPNAME")
                )

            run_command(d, cmd, source_root, env)

            bb.note('kernel_snapshot_%s: Complete' % runtime_mode)

            # remove kernel_blob.bin to save space
            try:
                os.remove('%s/%s/build/flutter_assets/kernel_blob.bin' % (source_dir, flutter_application_path))
            except OSError:
                pass

            # create empty file for apps that check for kernel_blob.bin
            cmd = 'touch %s/%s/build/flutter_assets/kernel_blob.bin' % (source_dir, flutter_application_path)
            run_command(d, cmd, source_root, env)

            bb.note('aot_elf_%s: Started' % runtime_mode)

            #
            # Extract Engine SDK
            #
            shutil.rmtree('%s/engine_sdk' % source_dir, ignore_errors=True)

            cmd = 'unzip %s/flutter/%s/%s/engine_sdk.zip -d %s/engine_sdk' % (
                d.getVar('STAGING_DATADIR'),
                flutter_sdk_version,
                runtime_mode,
                source_dir)

            run_command(d, cmd, source_root, env)

            #
            # Create libapp.so
            #
            cmd = '%s/engine_sdk/sdk/clang_x64/gen_snapshot \
                --deterministic %s \
                --snapshot_kind=app-aot-elf \
                --elf=libapp.so \
                --strip \
                .dart_tool/flutter_build/*/app.dill' % (
                    source_dir,
                    d.getVar("APP_GEN_SNAPSHOT_FLAGS")
                )

            run_command(d, cmd, source_root, env)
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
