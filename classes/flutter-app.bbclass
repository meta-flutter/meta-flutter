# Helper class for building Flutter Application.
# Assumes that:
# - Flutter Application does not have a linux folder.  If it does it 
#   will incorrectly link to Linux GTK embedder.  We don't want that.
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

DEPENDS += " \
    ca-certificates-native \
    flutter-sdk-native \
    unzip-native \
    "

FLUTTER_RUNTIME ??= "release"

FLUTTER_APPLICATION_PATH ??= "."

FLUTTER_EXTRA_BUILD_ARGS ??= ""

PUB_CACHE = "${WORKDIR}/pub_cache"
PUB_CACHE_ARCHIVE = "flutter-pub-cache-${PUBSPEC_APPNAME}-${SRCREV}.tar.bz2"

#
# Archive Pub Cache
#

addtask archive_pub_cache before do_patch after do_unpack
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
    
    pub_cache_cmd = \
        'export PUB_CACHE=%s; ' \
        '%s/bin/dart pub get --directory=%s --no-offline && ' \
        '%s/bin/dart pub get --directory=%s --offline' % \
        (pub_cache, flutter_sdk, app_root, flutter_sdk, app_root)

    bb.note("Running %s in %s" % (pub_cache_cmd, workdir))
    runfetchcmd('%s' % (pub_cache_cmd), d, quiet=False, workdir=workdir)

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
        raise UnpackError("Unapck command %s failed with return value %s" % (cmd, ret), localpath)
}

#
# Build flutter_assets folder and AOT (libapp.so)
#

do_compile() {

    FLUTTER_SDK=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk

    export PATH=${FLUTTER_SDK}/bin:$PATH

    cd ${S}/${FLUTTER_APPLICATION_PATH}

    flutter build bundle ${FLUTTER_EXTRA_BUILD_ARGS}

    if ${@bb.utils.contains('FLUTTER_RUNTIME', 'release', 'true', 'false', d)} || \
       ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)}; then

        PROFILE_ENABLE=false
        if ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)}; then
            PROFILE_ENABLE=true
        fi

        ${FLUTTER_SDK}/bin/cache/dart-sdk/bin/dart \
            --verbose \
            --disable-analytics \
            --disable-dart-dev ${FLUTTER_SDK}/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
            --sdk-root ${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/ \
            --target=flutter \
            --no-print-incremental-dependencies \
            -Ddart.vm.profile=${PROFILE_ENABLE} \
            -Ddart.vm.product=true \
            --aot --tfa \
            --packages .dart_tool/package_config.json \
            --output-dill .dart_tool/flutter_build/*/app.dill \
            --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
            package:${PUBSPEC_APPNAME}/main.dart

        #
        # Extract Engine SDK
        #
        rm -rf ${S}/engine_sdk
        unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk

        #
        # Create libapp.so
        #
        ${S}/engine_sdk/sdk/clang_x64/gen_snapshot \
            --snapshot_kind=app-aot-elf \
            --elf=libapp.so \
            --strip \
            .dart_tool/flutter_build/*/app.dill
    fi
}

INSANE_SKIP:${PN} += " ldflags libdir"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
    install -d ${D}${datadir}/${PUBSPEC_APPNAME}
    if ${@bb.utils.contains('FLUTTER_RUNTIME', 'release', 'true', 'false', d)} || \
       ${@bb.utils.contains('FLUTTER_RUNTIME', 'profile', 'true', 'false', d)}; then
        cp ${S}/${FLUTTER_APPLICATION_PATH}/libapp.so ${D}${datadir}/${PUBSPEC_APPNAME}/
    fi
    cp -r ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/* ${D}${datadir}/${PUBSPEC_APPNAME}/
}

FILES:${PN} = "${datadir}"
FILES:${PN}-dev = ""