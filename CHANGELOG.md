# Changelog

May 20, 2024
1. Roll v2 - cleanup plugin_common

May 18, 2024
1. Roll v2 - Separates plugin_common to plugin_common_curl and plugin_common_glib

May 17, 2024
1. v2 enable disabling optional plugins

May 15, 2024
1. ivi-homescreen/flutter-auto v2

May 13, 2024
1. Update Pubspec Lockfile Handling
2. resolve PUB_CACHE_ARCHIVE fails for local sources
3. Version ivi-homescreen to v1.0
4. Version flutter-auto to v1.0
5. Update flutter-auto repo and commit to match ivi-homescreen v1.6. flutter-auto recipes now uses EXE_OUTPUT_NAME=flutter-auto
7. Move archive pubspec after patch, before configure


Apr 22, 2024
1. Add runtime recommendation for liberation-fonts to flutter-pi
2. common.inc -> change sys.exit occurence to bb.fatal

Apr 13, 2024
1. Set `VPYTHON_VIRTUALENV_ROOT` to ${WORKDIR}/vpython

Apr 2, 2024
1. rive-text LTO build option.  Resolves 32-bit build issue.
2. pdfium. Remove skia from default PACKAGECONFIG.
3. dart-sdk. Resolves 32-bit build issue.

March 29, 2024
1. improve flutter app build error handling

Mar 27, 2024
1. sentry-native RISC-V
2. add compatibility for deprecated layer names: meta-flutter and meta-flutter-apps

Mar 26, 2024
1. flutter-app build use glob to resolve paths with asterisk

Mar 22, 2024
1. flutter-engine: Resolve tmp path warning
2. pdfium: Resolve tmp path warning
3. dart-sdk: Resolve tmp path warning

Mar 21, 2024
1. Add PV value to flutter-engine and flutter-sdk
2. Delete pubspec.lock file

Mar 20, 2024
1. Rename logical layer names to xxx-layer
2. Add LAYERRECOMMENDS to both meta-flutter and meta-flutter-apps

Mar 18, 2024
1. flutter-engine: default desktop-embedding off
2. flutter-engine: remove gtk dep if unit-tests disabled
3. flutter-engine: enable RISC-V building

Mar 12, 2024
1. Rive 0.8.4
2. packagegroup-flutter-sdk-deps
3. Updated README for steps to use flutter SDK on target

Mar 11, 2024
1. Flutter 3.19.3
2. Move native recipes out of meta-flutter-apps
3. Update comp-surf-pbr
4. make revenue cat recipe name lowercase

Mar 7, 2024
1. Strip x86_64 executables in flutter-engine-sdk package

Mar 4, 2024
1. Add lib prefix to rive_text.so

Feb 28, 2024
1. add Rive native recipes
2. create meta-flutter-apps layer and moved app related recipes here
3. flutter 3.19.2

Feb 22, 2024
1. add gemini flutter app example
2. super_dash + filament app are now auto-generated with roll_meta_flutter.py.
3. Document auto roll process

Feb 21, 2024
1. flutter 3.19.1
2. roll flutter apps
   updates flutter apps using latest roll_meta_flutter.py (workspace_automation).
3. fixes `BSD3-Clause` issue.
4. Add additional RDEPENDS to flutter-sdk.

Feb 15, 2024
1. flutter 3.19.0
2. dart-sdk 3.3.0
3. flutter-engine-sdk - copy everything from exe.unstripped

Feb 10, 2024
1. roll sentry-native to 0.7.0.  Resolves QA step break

Feb 9, 2024
1. flutter-engine arm64 host build
2. roll ivi-homescreen
  - DLT log fix
  - CMP0148 policy
3. pdf demo RDEPENDS
4. default EGL backend for ivi-homescreen + flutter-auto.
  Works around BSP issues incorrectly adding vulkan to DISTRO_FEATURES.

Feb 7, 2024
1. PDFium support all archs, default skia backend on (faster)
2. Move flutter package file selector example to dynamic-layer due to RDEPENDS

Feb 6, 2024
1. dart-sdk 3.2.6
2. dart-sdk gcc recipe
3. Update gn fetcher "name".  Pass `gn_name` parameter instead of `name`.
   Prevents any conflicts with `name` and it's use.
4. Split flutter-engine, and dart-sdk between do_configure and do_compile.   
5. Correct Feb 4 CHANGELOG.md entry
6. pdfium 123.0.6281.0
7. roll flutter-apps
8. add flutter-app dart_pdf recipes

Feb 5, 2024
1. Unable to find `curl` fix.
   `occasional` flutter-sdk-native build failures on clean source tree.
   Initial issue may be related to host machine HW thread count.  Not reproducible
   on 32+ HW threads.

Feb 4, 2024
1. Improved recipe names

Feb 3, 2024
1. 1st manual autoroll using workspace automation `roll_meta_flutter.py`.  Total 108 flutter apps
2. remove filtering of dart_plugin_registrant.dart.  Fixes build break
3. FLUTTER_APPLICATION_INSTALL_SUFFIX.  No change in install path behavior.  Allows overriding install path suffix.
4. Flutter Community plus_plugins

Feb 1, 2024
1. Defaults FLUTTER_APPLICATION_INSTALL_PREFIX to "${datadir}/flutter". Remove reference from all app recipes.
2. Allow overriding FLUTTER_APPLICATION_INSTALL_PREFIX.  In support of user home path install.
3. Introduce FLUTTER_ENGINE_INSTALL_PREFIX.  This allows overriding the flutter engine install prefix.

Jan 31, 2024
1. Remove bbappend for ghost recipe.  Not released yet.
2. Flutter 3.16.9

Jan 24, 2024
1. Add http(s)_proxy export to depot_tools

Jan 22, 2024
1. Fix debug builds for apps

Jan 19, 2024
1. Add `FLUTTER_SDK_TAG` to pub-cache archive filename.  When updating SDK version this will update pub-cache correctly now network is disabled for compile.
2. Move flutter-packages-example-file-selector_git.bb to dynamic-layers/gnome-layer/recipes-graphics/flutter-apps/ since it has a runtime dependency on meta-gnome.

Jan 17, 2024

1. Update packagegroups
2. Update CI
3. baseflow-geolocator runtime dep.  Note this needs DBUS access configuration.
4. Note this layer has optional dependencies on https://github.com/jwinarske/meta-vulkan
   - filament-vk
   - swiftshader
5. Fltuter 3.16.7

Jan 16, 2024

1. Offline build support. If you have already ran do_archive_pub_cache on a flutter recipe, you can now build it without a network connection.

  Note: if you have pub-cache archive files populated in the DL_DIR it will skip the network fetch.

2. remove all ${AUTOREV} references.
3. remove do_compile[network] = "1" from flutter-app template.
* requires app recipes to add as needed.
4. set pub cache offline after a flutter clean.
5. flutter-sdk-native
  - remove deletion of ${S}/bin/cache/pkg/sky_engine/ and ${S}/bin/cache/artifacts/*
    most likely will require update for SDK
  - update to append to do_unpack
6. flutter-app template do_cleanall will remove pub cache archive from DL_DIR
7. create the app pub-cache from the flutter-sdk pub-cache
8. remove unused var FLUTTER_PUB_CMD
9. update pub cache archive name to include ${PN}
10. include the desktop embedder (GTK) library in flutter-engine by default

Jan 15, 2024
1. firebase-cpp-sdk
2. patches to enable firebase on various apps

Jan 4, 2024
1. Support building Flutter Web apps
2. Remove Rust workaround for macro_proc2 (prevents build error)
3. disable engine unit tests (improves build time)
4. Move --obfuscate to a variable that can be overriden
5. 3.16.5

Nov 9, 2023
1. Replace pyyaml use with re to avoid mixing host and Yocto -native
   Python bits.
2. Changed a couple of pubspec.yaml name errors to bb.fatal to stop
   build immediately, as do_compile will fail.

Nov 8, 2023
1. Update engine_sdk.zip contents to enable impeller 3d aot generation from host
2. Support unpotimized builds with IsCreationCurrentThreadCurrent patch
  https://github.com/flutter/flutter/issues/129533
  https://github.com/flutter/flutter/issues/135345

Nov 6, 2023
1. Use pyyaml from Yocto build not from system

Nov 4, 2023
1. Dart AOT plugin registration is working in all tested cases.
  This is confirmed on 3.13.9 using the --source flag during
  kernel snapshot creation.  The scenario where some apps were 
  correctly calling _PluginRegistrant.register and not others,
  was resolved by setting the --obfuscate flag for the gen_snapshot
  invocation.  This is now enabled by default for release/profile builds.

Nov 2, 2023
1. Includes dart_plugin_registrant.dart file in AOT
  https://github.com/meta-flutter/meta-flutter/issues/115
2. filter dart_plugin_registrant.dart against unused platforms.
3. path_provider is dependent on xdg-user-dirs if you want official xdg directories,
  otherwise methods return home path.  For apps that are dependent on xdg-user-dirs
  add xdg-user-dirs to your RDEPENDS, and run `xdg-user-dirs-update`
  on birthday boot from user that runs homescreen/flutter-auto.
4. ivi-homescreen/flutter-auto known working apps included in meta-flutter:
  * flutter_markdown_example
  * google_maps_flutter_example
    runs - not plumbed into proprietary module
  * path_provider_example
    run xdg-user-dirs-update same user as the homescreen/flutter-auto runs as
  * animated_background_example
  * gallery
  * go_router_examples
  * google_sign_in_example
  * extension_google_sign_in_example
  * shared_preferences_example
  * file_selector_example
    runs, and zenity starts
    works on desktop homescreen/flutter-auto
    For AGL app activation code needed
  * url_launcher_example
    url launch works, webview launch/close not wired in
    For AGL app activation code needed
  * camera_example
    runs, does not talk to flutter-auto
  * wonders
    does not display anything
    shared_preference plugin issue - needs investigation
    appears to be pigeon related
  * in_app_purchase_example
    does not display anything
    appears to be pigeon related 
  * video_player_example
    runs, does not talk to homescreen/flutter-auto.  Needs update to work with pigeon
  

March 17, 2023
* APP_GEN_SNAPSHOT_FLAGS - allows setting gen_snapshot flags like `--no-use-integer-division`
* FLUTTER_APP_RUNTIME_MODES - allows setting runtime mode per app.  Deprecates use of FLUTTER_APP_SKIP_DEBUG_INSTALL.
* APP_AOT_EXTRA_DART_DEFINES - allows setting Dart defines for AOT builds
* Local version patch override. allows overriding patch set applied.  Could also be in bbappend.

March 5, 2023
* 3.7.6 support introduced
* Toyota OSS 0223
* local resolution of Flutter SDK and Engine Versions - LTS

Dec 26, 2022

* dart-sdk added - building/linking with the Yocto Clang toolchain.
* Package Groups added - flutter-agl-apps, flutter-test-apps
* Container Image added - app-container-image, app-container-image-flutter-auto
* Breaking Changes

  Removed BBCLASS implementation for -runtimedebug, -runtimeprofile, -runtimerelease
  Removed FLUTTER_RUNTIME
  Flutter Engine runtime variants are now built based on PACKAGECONFIG values: debug, profile, release, jit_release.  The default is release.
  To add additional runtime variants in addition to `release` use this pattern in local.conf:
      `PACKAGECONFIG:append:pn-flutter-engine = " profile debug"`
  flutter-app.bbclass installs app for each engine build available in the target sysroot.
  By default Flutter Apps are not installed for runtime=debug.  This can be overriden in local.conf using `FLUTTER_APP_SKIP_DEBUG_INSTALL = "false"`.

* Breaking Change
  
  Suffix for flutter runtime types has been changed to better define it.  Less confusing. 

  -runtimerelease (was -release)
  -runtimeprofile (was -profile)
  -runtimedebug (was -debug)

* FLUTTER_CHANNEL support has been deprecated

* FLUTTER_SDK_TAG - New approach.  Allows locking SDK and Engine to specific commit hash.
  Valid values for FLUTTER_SDK_TAG are here:  https://github.com/flutter/flutter/tags
  
* Flutter Engine Commit
  The Flutter Engine Commit is based on the value of `FLUTTER_SDK_TAG.`
  The default value of `FLUTTER_SDK_TAG` is set in `conf/include/flutter-version.inc`.  If `FLUTTER_SDK_TAG` is overriden with `"AUTOINC"` in local.conf, stable channel is used for the engine commit.

* build failure due to gn unknown parameter for `--no-build-embedder-examples`.  One solution to resolve this is to exclude `disable-embedder-examples` from PACKAGECONFIG in local.conf using:

  ```
  PACKAGECONFIG:pn-flutter-engine-runtimerelease = "disable-desktop-embeddings embedder-for-target fontconfig release"
  PACKAGECONFIG:pn-flutter-engine-runtimedebug = "disable-desktop-embeddings embedder-for-target fontconfig debug"
  PACKAGECONFIG:pn-flutter-engine-runtimeprofile = "disable-desktop-embeddings embedder-for-target fontconfig profile"
   ```
  This issue is related to missing gn options `--build-embedder-examples` and `--no-build-embedder-examples` from certain builds.  I have `disable-embedder-examples` defined in PACKAGECONFIG by default, so if you have an engine commit that is missing this option, you need to use the PACKAGECONFIG sequence above.  Once the gn option rolls into all channels this override will no longer be needed.
