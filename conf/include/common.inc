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

PUB_CACHE_EXTRA_ARCHIVE_CMD ??= ""
PUB_CACHE_EXTRA_ARCHIVE_PATH ??= ""

APP_AOT_EXTRA ??= ""
APP_AOT_ENTRY_FILE ??= "main.dart"
APP_GEN_SNAPSHOT_FLAGS ??= ""

FLUTTER_APP_RUNTIME_MODES ?= "release"

FLUTTER_APPLICATION_INSTALL_PREFIX ??= "${datadir}/flutter"
FLUTTER_APPLICATION_INSTALL_SUFFIX ??= "${PUBSPEC_APPNAME}"
FLUTTER_INSTALL_DIR = "${FLUTTER_APPLICATION_INSTALL_PREFIX}/${FLUTTER_APPLICATION_INSTALL_SUFFIX}"

PUB_CACHE = "${WORKDIR}/pub_cache"
PUB_CACHE_ARCHIVE = "flutter-pub-cache-${PN}-${SRCPV}-${FLUTTER_SDK_TAG}.tar.bz2"

PUBSPEC_IGNORE_LOCKFILE ??= "0"
PUBSPEC_ENFORCE_LOCKFILE ??= "0"

FLUTTER_SDK = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"

require conf/include/clang-utils.inc

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
        formatted_output = '\n  ' + output.replace('\n','\n  ')
        bb.note(f'Output:{formatted_output}')
        bb.fatal(f'{cmd} failed: {retval}')

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
    import shutil
    from   bb.fetch2 import FetchError

    localfile = d.getVar("PUB_CACHE_ARCHIVE")
    localpath = os.path.join(d.getVar("DL_DIR"), localfile)

    if os.access(localpath, os.R_OK):
        return

    env = os.environ

    #
    # clean existing pub-cache, populate from the flutter-sdk-native pub-cache
    #
    pub_cache = d.getVar("PUB_CACHE")
    env['PUB_CACHE'] = pub_cache

    shutil.rmtree(f'{pub_cache}', ignore_errors=True)
    flutter_sdk_pub_cache = os.path.join(d.getVar("STAGING_DATADIR_NATIVE"), 'flutter/sdk/.pub-cache')
    shutil.copytree(flutter_sdk_pub_cache, pub_cache)

    workdir = d.getVar("WORKDIR")
    env['XDG_CONFIG_HOME'] = workdir

    flutter_sdk = os.path.join(d.getVar("STAGING_DATADIR_NATIVE"), 'flutter/sdk')
    env['PATH'] = f'{env["PATH"]}:{flutter_sdk}/bin'

    app_root = os.path.join(d.getVar("S"), d.getVar("FLUTTER_APPLICATION_PATH"))

    if d.getVar("PUBSPEC_IGNORE_LOCKFILE") == "1":
        #
        # Remove pubspec.lock file if present
        #
        pubspec_lock = os.path.join(app_root, 'pubspec.lock')
        if os.path.exists(pubspec_lock):
            run_command(d, 'rm -rf pubspec.lock', app_root, env)

    #
    # App hook to include something in archive 
    #
    pub_cache_extra_archive_cmd = d.getVar("PUB_CACHE_EXTRA_ARCHIVE_CMD")
    pub_cache_extra_archive_path = d.getVar("PUB_CACHE_EXTRA_ARCHIVE_PATH")
    if pub_cache_extra_archive_cmd != None:
        path = d.getVar("PATH")
        cmd = \
            'export PUB_CACHE=%s; ' \
            'export XDG_CONFIG_HOME=%s;' \
            'export PATH=%s/bin:%s:%s;' \
            '%s' % \
            (pub_cache, workdir, flutter_sdk, path, pub_cache_extra_archive_path, pub_cache_extra_archive_cmd)

        bb.note("Running [%s] in %s" % (cmd, app_root))
        run_command(d, cmd, app_root, env)

    #
    # go online to fetch, then mark offline
    #
    run_command(d, 'flutter config --no-analytics', app_root, env)
    run_command(d, 'flutter config --no-cli-animations', app_root, env)
    run_command(d, 'flutter clean', app_root, env)
    if d.getVar("PUBSPEC_ENFORCE_LOCKFILE") == "1":
        run_command(d, 'flutter pub get --enforce-lockfile', app_root, env)
        run_command(d, 'flutter pub get --offline --enforce-lockfile', app_root, env)
    else:
        run_command(d, 'flutter pub get', app_root, env)
        run_command(d, 'flutter pub get --offline', app_root, env)

    cp_cmd = \
        'mkdir -p %s/.project | true; ' \
        'cp -r .* %s/.project/ | true; ' \
        % (pub_cache, pub_cache)

    bb.note("Running [%s] in %s" % (cp_cmd, app_root))
    run_command(d, cp_cmd, app_root, env)

    bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
    pack_cmd = "tar -I \"pbzip2 -p%s\" -cf %s ./" % (bb_number_threads, localpath)

    bb.note("Running [%s] in %s" % (pack_cmd, pub_cache))
    run_command(d, pack_cmd, pub_cache, env)

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
    localfile = d.getVar("PUB_CACHE_ARCHIVE")
    localpath = os.path.join(d.getVar("DL_DIR"), localfile)

    bb.note("Removing [%s]" % (localpath))

    try:
        os.remove(localpath)
    except OSError:
        pass
}

#
# Compile
#
python do_compile() {

    pubspec_yaml_appname = get_pubspec_yaml_appname(d)
    pubspec_appname = d.getVar("PUBSPEC_APPNAME")
    if pubspec_appname != pubspec_yaml_appname:
        bb.fatal("Set PUBSPEC_APPNAME to match name value in pubspec.yaml")

    if d.getVar("PUBSPEC_IGNORE_LOCKFILE") == "1" and d.getVar("PUBSPEC_ENFORCE_LOCKFILE") == "1":
        bb.fatal("Both PUBSPEC_IGNORE_LOCKFILE and PUBSPEC_ENFORCE_LOCKFILE are set which is invalid")

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

    build_folder = os.path.join(source_root, 'build')
    if os.path.exists(build_folder):
        run_command(d, 'flutter clean', source_root, env)

    if d.getVar("PUBSPEC_IGNORE_LOCKFILE") == "1":
        #
        # Remove pubspec.lock file if present
        #
        pubspec_lock = os.path.join(app_root, 'pubspec.lock')
        if os.path.exists(pubspec_lock):
            run_command(d, 'rm -rf pubspec.lock', app_root, env)

    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')
    run_command(d, f'flutter build web {flutter_build_args}', source_root, env)

    bb.note(f'Flutter build web {flutter_build_args}: Completed')


def build_app(d, source_dir, flutter_sdk, pubspec_appname, flutter_application_path, staging_dir_target, source_root, env):
    import glob
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

        build_folder = os.path.join(source_root, 'build')
        if os.path.exists(build_folder):
            run_command(d, 'flutter clean -v', source_root, env)

        if d.getVar("PUBSPEC_IGNORE_LOCKFILE") == "1":
            #
            # Remove pubspec.lock file if present
            #
            pubspec_lock = os.path.join(app_root, 'pubspec.lock')
            if os.path.exists(pubspec_lock):
                run_command(d, 'rm -rf pubspec.lock', app_root, env)

        run_command(d, 'flutter config --no-cli-animations', source_root, env)
        if d.getVar("PUBSPEC_ENFORCE_LOCKFILE") == "1":
            run_command(d, 'flutter pub get --offline --enforce-lockfile -v', source_root, env)
        else:
            run_command(d, 'flutter pub get --offline -v', source_root, env)
        
        if runtime_mode == 'jit_release':
            cmd = (f'flutter build {flutter_build_args} --local-engine -v')
            run_command(cmd, source_root, env)
        else:
            cmd = f'flutter build {flutter_build_args} -v'
            run_command(d, cmd, source_root, env)

        bb.note(f'[{runtime_mode}] flutter build {flutter_build_args}: Completed')

        if runtime_mode == 'release' or runtime_mode == 'profile':

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
            dill_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/app.dill')
            flutter_app_output_dill = dill_path[0]
            if runtime_mode == 'debug':
                flutter_app_debug_flags = '--enable-asserts'

            flutter_source_file = ''
            flutter_source_package = ''
            flutter_source_defines = ''
            dart_plugin_registrant_file = f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/dart_plugin_registrant.dart'
            if os.path.isfile(dart_plugin_registrant_file):
                flutter_source_file = f'--source file://{dart_plugin_registrant_file}'
                flutter_source_package = '--source package:flutter/src/dart_plugin_registrant.dart'
                flutter_source_defines = f'-Dflutter.dart_plugin_registrant=file://{dart_plugin_registrant_file}'

            flutter_native_assets = ''
            yaml_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/native_assets.yaml')
            if os.path.isfile(yaml_path[0]):
                flutter_native_assets = f'--native-assets {yaml_path[0]}'

            app_aot_extra = d.getVar("APP_AOT_EXTRA")
            app_aot_entry_file = d.getVar("APP_AOT_ENTRY_FILE")

            package_config_json = f'{source_dir}/{flutter_application_path}/.dart_tool/package_config.json'

            dep_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/kernel_snapshot.d')
            dep_file = dep_path[0]

            cmd = f'{flutter_sdk}/bin/cache/dart-sdk/bin/dart \
                --disable-analytics \
                --disable-dart-dev \
                {flutter_sdk}/bin/cache/artifacts/engine/linux-{clang_build_arch(d)}/frontend_server.dart.snapshot \
                --sdk-root {flutter_app_sdk_root} \
                --target=flutter \
                -Ddart.vm.profile={flutter_app_vm_profile} \
                -Ddart.vm.product={flutter_app_vm_product} \
                {app_aot_extra} \
                {flutter_app_debug_flags} \
                {flutter_app_profile_flags} \
                --aot \
                --tfa \
                --target-os linux \
                --packages {package_config_json} \
                --output-dill {flutter_app_output_dill} \
                --depfile {dep_file} \
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
            cmd = f'{source_dir}/engine_sdk/sdk/clang_{clang_build_arch(d)}/gen_snapshot \
                --deterministic \
                --snapshot_kind=app-aot-elf \
                --elf=libapp.so \
                --strip \
                {app_gen_snapshot_flags} \
                {flutter_app_output_dill}'
            run_command(d, cmd, source_root, env)
