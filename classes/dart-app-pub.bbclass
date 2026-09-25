#
# dart-app for an app with pub dependencies.
#
# Inherit this INSTEAD OF dart-app.
#
# The dependencies come from a pubvendor.py fragment the recipe requires, the
# same mechanism the Flutter SDK apps use: every package arrives through
# SRC_URI with a checksum bitbake verifies, and pub resolves against the
# staged cache with the network off.
#
# What differs from the Flutter path, and why pub-cache.bbclass alone is not
# enough here:
#
#   * no flutter_tools packages to seed. pub-cache copies them out of the
#     SDK's own .pub-cache because `flutter pub get` resolves flutter_tools
#     first. `dart pub get` resolves the app and nothing else.
#
#   * dart, not flutter. There is no flutter binary in a Dart app's
#     dependencies, and reaching for one would pull the whole SDK in for a
#     resolve that does not need it.
#

inherit pub-cache dart-app

# Nothing to seed: see above.
do_seed_pub_cache[noexec] = "1"

# gen_kernel resolves imports through the package config pub writes.
DART_APP_KERNEL_ARGS = "--packages ${PUBSPEC_APP_DIR}/.dart_tool/package_config.json"

do_pub_get_offline() {
    # HOME and XDG_CONFIG_HOME for the same reason common.inc sets them: dart
    # writes into both and must not touch the builder's real home.
    export PATH="${DART_SDK_NATIVE}/bin:$PATH"
    export HOME="${WORKDIR}"
    export XDG_CONFIG_HOME="${WORKDIR}"

    cd ${PUBSPEC_APP_DIR}

    # --offline so a missing package fails here rather than being fetched
    # behind the vendoring, and --enforce-lockfile so a lockfile that no
    # longer matches pubspec.yaml is an error instead of a silent re-resolve.
    dart pub get --offline --enforce-lockfile
}
