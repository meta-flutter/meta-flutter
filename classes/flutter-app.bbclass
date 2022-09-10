# Helper class for building Flutter Application.
# Assumes that:
# - Flutter Application does not have a linux folder.  If it does it 
#   will incorrectly link to Linux GTK embedder.  We don't want that.
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

require conf/include/flutter-runtime.inc

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"

DEPENDS += " \
    ca-certificates-native \
    cmake-native \
    compiler-rt \
    flutter-engine-${@gn_get_flutter_runtime_name(d)} \
    flutter-sdk-native \
    libcxx \
    ninja-native \
    unzip-native \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

FLUTTER_PREBUILD_CMD ??= ""
FLUTTER_APPLICATION_PATH ??= "."
FLUTTER_BUILD_ARGS ??= "bundle -v"
FLUTTER_APPLICATION_INSTALL_PREFIX ??= ""
FLUTTER_INSTALL_DIR = "${datadir}${FLUTTER_APPLICATION_INSTALL_PREFIX}/${PUBSPEC_APPNAME}"

FLUTTER_APP_DISABLE_NATIVE_PLUGINS ??= ""

FLUTTER_PUB_CMD ??= "get"

PUB_CACHE = "${WORKDIR}/pub_cache"
PUB_CACHE_ARCHIVE = "flutter-pub-cache-${PUBSPEC_APPNAME}-${SRCREV}.tar.bz2"

FLUTTER_SDK = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"

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
        '%s/bin/flutter pub get;' \
        '%s/bin/flutter pub get --offline' % \
        (pub_cache, flutter_sdk, flutter_sdk)

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

    bbnote "flutter build ${FLUTTER_BUILD_ARGS}: Starting"

    flutter build ${FLUTTER_BUILD_ARGS}

    bbnote "flutter build ${FLUTTER_BUILD_ARGS}: Completed"

    if ${@bb.utils.contains('FLUTTER_RUNTIME', 'release', 'true', 'false', d)} || \
       ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)}; then

        bbnote "kernel_snapshot_${FLUTTER_RUNTIME}: Starting"

        ${FLUTTER_SDK}/bin/cache/dart-sdk/bin/dart \
            --verbose \
            --disable-analytics \
            --disable-dart-dev ${FLUTTER_SDK}/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
            --sdk-root ${@bb.utils.contains('FLUTTER_RUNTIME', 'release', '${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/', '${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk/', d)} \
            --target=flutter \
            --no-print-incremental-dependencies \
            -Ddart.vm.profile=${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)} \
            -Ddart.vm.product=${@bb.utils.contains('FLUTTER_RUNTIME', 'release', 'true', 'false', d)} \
            ${@bb.utils.contains('FLUTTER_RUNTIME', 'debug', '--enable-asserts', '', d)} \
            ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', '--track-widget-creation', '', d)} \
            --aot --tfa \
            --packages .dart_tool/package_config.json \
            ${@bb.utils.contains('FLUTTER_RUNTIME', 'debug', '.dart_tool/flutter_build/*/app.dill', '--output-dill .dart_tool/flutter_build/*/app.dill', d)} \
            --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
            package:${PUBSPEC_APPNAME}/main.dart

        bbnote "kernel_snapshot_${FLUTTER_RUNTIME}: Complete"

        # remove kernel_blob.bin to save space
        rm ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/kernel_blob.bin

        # create empty file for apps that check for kernel_blob.bin
        touch ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/kernel_blob.bin

        bbnote "aot_elf_${FLUTTER_RUNTIME}: Started"

        #
        # Extract Engine SDK
        #
        rm -rf ${S}/engine_sdk
        unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk

        #
        # Create libapp.so
        #
        ${S}/engine_sdk/sdk/clang_x64/gen_snapshot \
            --deterministic \
            --snapshot_kind=app-aot-elf \
            --elf=libapp.so \
            --strip \
            .dart_tool/flutter_build/*/app.dill

        bbnote "aot_elf_${FLUTTER_RUNTIME}: Complete"
    fi
}

INSANE_SKIP:${PN} += " ldflags libdir"
SOLIBS = ".so"
FILES:SOLIBSDEV = ""

do_install() {

    install -d ${D}${FLUTTER_INSTALL_DIR}/data/flutter_assets
    cp -r ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/* ${D}${FLUTTER_INSTALL_DIR}/data/flutter_assets/

    if ${@bb.utils.contains('FLUTTER_RUNTIME', 'release', 'true', 'false', d)} || \
        ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)}; then

        bbnote "Flutter Application: Installing ${FLUTTER_RUNTIME}"

        install -d ${D}${FLUTTER_INSTALL_DIR}/lib
        cp ${S}/${FLUTTER_APPLICATION_PATH}/libapp.so ${D}${FLUTTER_INSTALL_DIR}/lib/
    fi

       
    if [[ "${FLUTTER_BUILD_ARGS}" =~ .*"linux".* ]]; then

        bbnote "Flutter Application: Installing GTK bundle"

        if [ -n "${FLUTTER_REMOVE_LINUX_BUILD_ARTIFACTS}" ]; then
            rm ${D}/usr/${PUBSPEC_APPNAME} | true
            rm -rf ${D}${libdir}/*.so | true
        else
            # expecting default "release" build
            mv ${D}/usr/${PUBSPEC_APPNAME} ${D}${bindir}/ | true
            rm -rf ${D}${bindir}/${PUBSPEC_APPNAME} | true
            rm -rf ${D}${libdir}/*.so | true
        fi
    fi
}

FILES:${PN} = "\
    ${bindir} \
    ${libdir} \
    ${FLUTTER_INSTALL_DIR} \
    "

FILES:${PN}-dev = ""
