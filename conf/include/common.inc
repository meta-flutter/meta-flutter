# Copyright (C) 2023 Joel Winarske. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

DEPENDS += " \
    ca-certificates-native \
    flutter-sdk-native \
    unzip-native \
    "

FLUTTER_APPLICATION_PATH ??= ""
FLUTTER_PREBUILD_CMD ??= ""

APP_AOT_EXTRA ??= ""
APP_AOT_ENTRY_FILE ??= "main.dart"
APP_GEN_SNAPSHOT_FLAGS ??= "--obfuscate"

FLUTTER_APP_RUNTIME_MODES ?= "release"

FLUTTER_APPLICATION_INSTALL_PREFIX ??= ""
FLUTTER_INSTALL_DIR = "${datadir}${FLUTTER_APPLICATION_INSTALL_PREFIX}/${PUBSPEC_APPNAME}"

FLUTTER_PUB_CMD ??= "get"

PUB_CACHE = "${WORKDIR}/pub_cache"
PUB_CACHE_ARCHIVE = "flutter-pub-cache-${PUBSPEC_APPNAME}-${SRCREV}.tar.bz2"

FLUTTER_SDK = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"


python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}

def getstatusoutput(cmd, cwd, env):
    from subprocess import check_output, CalledProcessError, STDOUT

    try:
        data = check_output(cmd, shell=True, universal_newlines=True, stderr=STDOUT, cwd=cwd, env=env)
        status = 0
    except CalledProcessError as ex:
        data = ex.output
        status = ex.returncode
    if data[-1:] == '\n':
        data = data[:-1]
    return status, data

def run_command(d, cmd, cwd, env):
    import subprocess
    import re

    # replace all consecutive whitespace characters (tabs, newlines etc.) with a single space
    cmd = re.sub('\s{2,}', ' ', cmd)

    bb.note('Running [%s] in %s' % (cmd, cwd))
    (retval, output) = getstatusoutput(cmd, cwd, env)
    if retval:
        bb.error("failed %s (cmd was %s)%s" % (retval, cmd, ":\n%s" % output if output else ""))
        return

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


#
# clean pub cache archive
#
python do_cleanall:append() {
    import shutil
    pub_cache_archive = d.getVar("PUB_CACHE_ARCHIVE")
    bb.note("Removing [%s]" % (pub_cache_archive))
    shutil.rmtree(pub_cache_archive, ignore_errors=True)
}


do_compile[network] = "1"
python do_compile() {

    pubspec_yaml_appname = get_pubspec_yaml_appname(d)
    pubspec_appname = d.getVar("PUBSPEC_APPNAME")
    if pubspec_appname != pubspec_yaml_appname:
        bb.fatal("Set PUBSPEC_APPNAME to match name value in pubspec.yaml")

    flutter_sdk = d.getVar('FLUTTER_SDK')

    env = os.environ
    env['PATH'] = f'{flutter_sdk}/bin:{d.getVar("PATH")}'
    env['PUB_CACHE'] = d.getVar('PUB_CACHE')

    staging_dir_target = d.getVar('STAGING_DIR_TARGET')
    env['PKG_CONFIG_PATH'] = f'{staging_dir_target}/usr/lib/pkgconfig:{staging_dir_target}/usr/share/pkgconfig:{d.getVar("PKG_CONFIG_PATH")}'

    bb.note(f'{env}')

    source_dir = d.getVar('S')
    flutter_application_path = d.getVar('FLUTTER_APPLICATION_PATH')
    source_root = os.path.join(source_dir, flutter_application_path)
    cmd = d.getVar('FLUTTER_PREBUILD_CMD')
    if cmd != '':
        run_command(d, cmd, source_root, env)

    build_type = d.getVar('BUILD_TYPE')

    if build_type == 'web':
        build_web(d, source_root, env)

    elif build_type == 'app':
        build_app(d, source_dir, flutter_sdk, pubspec_appname, flutter_application_path, staging_dir_target, source_root, env)
}


def build_web(d, source_root, env):

    run_command(d, 'flutter clean', source_root, env)
    run_command(d, 'flutter pub get --offline', source_root, env)

    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')

    cmd = f'flutter build web {flutter_build_args}'

    run_command(d, cmd, source_root, env)

    bb.note(f'Flutter build web {flutter_build_args}: Completed')


def build_app(d, source_dir, flutter_sdk, pubspec_appname, flutter_application_path, staging_dir_target, source_root, env):
    import shutil

    # determine build type based on what flutter-engine installed.
    datadir = d.getVar('datadir')
    flutter_sdk_version = d.getVar('FLUTTER_SDK_VERSION')
    flutter_runtime_modes = os.listdir(f'{staging_dir_target}{datadir}/flutter/{flutter_sdk_version}')

    flutter_app_runtime_modes = d.getVar('FLUTTER_APP_RUNTIME_MODES')
    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')

    for runtime_mode in flutter_runtime_modes:
        if runtime_mode not in flutter_app_runtime_modes:
            bb.note(f'Skipping build for: {runtime_mode}')
            continue

        bb.note(f'[{runtime_mode}] flutter build {flutter_build_args}: Starting')

        run_command(d, 'flutter clean', source_root, env)
        run_command(d, 'flutter pub get --offline', source_root, env)

        if runtime_mode == 'jit_release':
            cmd = (f'flutter build {flutter_build_args} --local-engine')
            run_command(cmd, source_root, env)
        else:
            cmd = f'flutter build {flutter_build_args}'
            run_command(d, cmd, source_root, env)

        bb.note(f'[{runtime_mode}] flutter build {flutter_build_args}: Completed')

        if runtime_mode == 'release' or 'profile':

            bb.note(f'kernel_snapshot_{runtime_mode}: Starting')

            flutter_app_sdk_root = f'{flutter_sdk}/bin/cache/artifacts/engine/common/flutter_patched_sdk/'
            flutter_app_vm_product = "false"
            if runtime_mode == 'release':
                flutter_app_sdk_root = f'{flutter_sdk}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/'
                flutter_app_vm_product = "true"

            flutter_app_profile_flags = ''
            flutter_app_vm_profile = 'false'
            if runtime_mode == 'profile':
                flutter_app_profile_flags = '--track-widget-creation' 
                flutter_app_vm_profile = 'true'

            flutter_app_debug_flags = ''
            flutter_app_app_dill = '--output-dill .dart_tool/flutter_build/*/app.dill'
            if runtime_mode == 'debug':
                flutter_app_debug_flags = '--enable-asserts'
                flutter_app_app_dill = '.dart_tool/flutter_build/*/app.dill'

            flutter_source_file = ''
            flutter_source_package = ''
            flutter_source_defines = ''
            dart_plugin_registrant_file = f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/dart_plugin_registrant.dart'
            if os.path.isfile(dart_plugin_registrant_file):
                filter_plugin_registrant(dart_plugin_registrant_file)
                flutter_source_file = f'--source file://{dart_plugin_registrant_file}'
                flutter_source_package = '--source package:flutter/src/dart_plugin_registrant.dart'
                flutter_source_defines = f'-Dflutter.dart_plugin_registrant=file://{dart_plugin_registrant_file}'

            flutter_native_assets = ''
            if os.path.isfile('.dart_tool/flutter_build/*/native_assets.yaml'):
                flutter_native_assets = f'--native-assets {source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/native_assets.yaml'

            app_aot_extra = d.getVar("APP_AOT_EXTRA")
            app_aot_entry_file = d.getVar("APP_AOT_ENTRY_FILE")
            cmd = f'{flutter_sdk}/bin/cache/dart-sdk/bin/dart \
                --disable-analytics \
                --disable-dart-dev {flutter_sdk}/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
                --sdk-root {flutter_app_sdk_root} \
                --target=flutter \
                --no-print-incremental-dependencies \
                -Ddart.vm.profile={flutter_app_vm_profile} \
                -Ddart.vm.product={flutter_app_vm_product} \
                {app_aot_extra} \
                {flutter_app_debug_flags} \
                {flutter_app_profile_flags} \
                --aot \
                --tfa \
                --target-os linux \
                --packages .dart_tool/package_config.json \
                {flutter_app_app_dill} \
                --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
                {flutter_source_file} \
                {flutter_source_package} \
                {flutter_source_defines} \
                {flutter_native_assets} \
                --verbosity=error \
                package:{pubspec_appname}/{app_aot_entry_file}'

            run_command(d, cmd, source_root, env)

            bb.note(f'kernel_snapshot_{runtime_mode}: Complete')

            # remove kernel_blob.bin to save space
            try:
                os.remove(f'{source_dir}/{flutter_application_path}/build/flutter_assets/kernel_blob.bin')
            except OSError:
                pass

            # create empty file for apps that check for kernel_blob.bin
            run_command(d, 'touch build/flutter_assets/kernel_blob.bin', source_root, env)

            bb.note(f'aot_elf_{runtime_mode}: Started')

            #
            # Extract Engine SDK
            #
            shutil.rmtree(f'{source_dir}/engine_sdk', ignore_errors=True)

            staging_datadir = d.getVar('STAGING_DATADIR')
            cmd = f'unzip {staging_datadir}/flutter/{flutter_sdk_version}/{runtime_mode}/engine_sdk.zip \
                -d {source_dir}/engine_sdk'
            run_command(d, cmd, source_root, env)

            #
            # Create libapp.so
            #
            app_gen_snapshot_flags = d.getVar("APP_GEN_SNAPSHOT_FLAGS")
            cmd = f'{source_dir}/engine_sdk/sdk/clang_x64/gen_snapshot \
                --deterministic \
                --snapshot_kind=app-aot-elf \
                --elf=libapp.so \
                --strip \
                {app_gen_snapshot_flags} \
                .dart_tool/flutter_build/*/app.dill'
            run_command(d, cmd, source_root, env)
