#
# Build a standalone Dart executable for the target.
#
# Two steps, because nothing in the SDK cross-compiles in one:
#
#   1. the AOT kernel, on the build machine. gen_kernel_aot with --aot --tfa,
#      not `dart compile kernel` -- that produces a non-AOT kernel and
#      gen_snapshot rejects it outright:
#
#        Missing table selector metadata!
#        Probably gen_kernel was run in non-AOT mode or without TFA.
#
#   2. the target ELF, from that kernel, with the cross gen_snapshot staged by
#      dart-sdk-tools-native: a build-machine binary that emits target code.
#
# The result is a shared object run by the target's dartaotruntime, which the
# dart-sdk package provides. See #428.
#
# Pub dependencies are not handled yet. An app with a pubspec that resolves
# anything from pub.dev needs the offline vendoring pub-cache.bbclass does for
# Flutter apps; this handles apps that depend only on the SDK.
#

DEPENDS += " \
    dart-sdk-native \
    dart-sdk-tools-native \
    "

RDEPENDS:${PN} += "dart-sdk"

# The app's entry point, relative to ${S}.
DART_APP_ENTRY ??= "bin/main.dart"

# Installed name of the executable, and where its snapshot lives.
DART_APP_NAME ??= "${BPN}"
DART_APP_LIBDIR ??= "${libdir}/${DART_APP_NAME}"

# Extra arguments for the kernel step. dart-app-pub sets this to point
# gen_kernel at the package config pub writes, which an app with no pub
# dependencies does not have.
DART_APP_KERNEL_ARGS ??= ""

DART_SDK_NATIVE = "${STAGING_DATADIR_NATIVE}/dart-sdk"
DART_CROSS_GEN_SNAPSHOT = "${STAGING_BINDIR_NATIVE}/dart-sdk-tools/gen_snapshot"

# Target runtime, as installed by dart-sdk.
DART_AOT_RUNTIME ??= "${datadir}/dart-sdk/bin/dartaotruntime"

# The entry point is named through a filesystem root rather than by path. The
# frontend records the source URI in the AOT image and canonicalizes a relative
# path back to an absolute one, so only a scheme with no absolute form keeps
# TMPDIR out of the result. Same mechanism flutter-app uses for the plugin
# registrant -- see common.inc and #819.
DART_APP_SCHEME ??= "org-dartlang-root"

do_compile() {
    cd ${S}

    bbnote "kernel: ${DART_APP_ENTRY} -> ${B}/${DART_APP_NAME}.dill"
    ${DART_SDK_NATIVE}/bin/dartaotruntime \
        ${DART_SDK_NATIVE}/bin/snapshots/gen_kernel_aot.dart.snapshot \
        --platform ${DART_SDK_NATIVE}/lib/_internal/vm_platform_strong.dill \
        --aot --tfa \
        --filesystem-root ${S} \
        --filesystem-scheme ${DART_APP_SCHEME} \
        ${DART_APP_KERNEL_ARGS} \
        -o ${B}/${DART_APP_NAME}.dill \
        ${DART_APP_SCHEME}:///${DART_APP_ENTRY}

    bbnote "aot elf: ${B}/${DART_APP_NAME}.so"
    # Not --strip: bitbake strips what it packages and keeps the debug
    # information, the same reason the engine's host tools are taken
    # unstripped (#1009). Stripping here is what trips already-stripped.
    ${DART_CROSS_GEN_SNAPSHOT} \
        --snapshot_kind=app-aot-elf \
        --elf=${B}/${DART_APP_NAME}.so \
        ${B}/${DART_APP_NAME}.dill
}

do_install() {
    install -d ${D}${DART_APP_LIBDIR}
    install -m 0644 ${B}/${DART_APP_NAME}.so ${D}${DART_APP_LIBDIR}/

    # dartaotruntime takes the snapshot as its first argument, so the app needs
    # a wrapper to be a command rather than a thing you remember how to launch.
    install -d ${D}${bindir}
    cat > ${D}${bindir}/${DART_APP_NAME} <<WRAPPER
#!/bin/sh
exec ${DART_AOT_RUNTIME} ${DART_APP_LIBDIR}/${DART_APP_NAME}.so "\$@"
WRAPPER
    chmod 0755 ${D}${bindir}/${DART_APP_NAME}
}

FILES:${PN} += " \
    ${DART_APP_LIBDIR} \
    ${bindir}/${DART_APP_NAME} \
    "

# ldflags: the snapshot is emitted by gen_snapshot, not linked by our
# toolchain, so it carries no GNU_HASH and never will. Same reason
# flutter-app.inc skips it for the app image.
#
# buildpaths is deliberately not skipped. The entry point is named through a
# filesystem root above, so the image has no build path in it, and the check
# stays a regression guard.
INSANE_SKIP:${PN} += "ldflags"
