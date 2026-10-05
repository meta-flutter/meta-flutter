# Changelog

October 4, 2026
1. one place for the clang toolchain: conf/include/flutter-clang-toolchain.inc
   and conf/include/flutter-clang-libcxx.inc
   - eight recipes and classes each set TOOLCHAIN = "clang"; they require an
     include instead. Compiler and standard library are separate includes
     because they are separate decisions, and most of those recipes want only
     the compiler
   - the toolchain include checks that TOOLCHAIN took effect and raises
     SkipRecipe with a reason when it did not, rather than bb.fatal: a
     parse-time fatal in a recipe halts the whole parse, so a configuration
     that never builds these would stop working over a layer it does not need
   - LIBCPLUSPLUS is meta-clang's. oe-core names it only inside clang_git.bb's
     nativesdk override, so here the variable is inert and a recipe setting it
     alone builds against libstdc++ while saying otherwise. Measured on
     firebase-cpp-sdk: 506 std::__cxx11 symbols and no std::__1 with
     LIBCPLUSPLUS alone, 668 std::__1 and none with -stdlib=libc++ in CXXFLAGS
   - moving the remaining recipes onto the libcxx include is #1124:
     flutter-engine, dart-sdk and rive-text have never been built against
     libc++, so each is a change to prove rather than a line to add
2. firebase-cpp-sdk: declare LICENSE in the syntax oe-core now wants. The new
   license parser deprecates "&", and this was the one recipe in the layer
   still using it. The release branches keep "&": their parsers split on
   "&|()" and nothing else, so a bare AND there is read as a license name

October 3, 2026
1. add firebase-cpp-sdk and the firebase_ffi demo app recipes
   - firebase_ffi puts the FlutterFire plugins on Linux over the Firebase C++
     SDK, with the native library coming from its own build hook as a code
     asset rather than an embedder plugin
   - the SDK's desktop build fetches seven dependencies at configure time. Each
     is supplied as a pinned git SRC_URI and staged where the superbuild
     expects it, so nothing is fetched during the build
   - PACKAGECONFIG: auth, database, storage, functions, remote_config and
     app_check. firestore is off
   - headers install under ${prefix}/src/firebase-cpp-sdk and SYSROOT_DIRS
     carries them, since a build hook resolves the SDK with find_package out of
     the recipe sysroot
   - do_install asserts the app linked the SDK rather than the transport-only
     library the hook produces when find_package misses
   - this libc++ is stricter about transitive includes than the release
     branches', so the SDK's own sources get <ctime> and <cerrno> forced in:
     no type named 'time_t' in namespace 'std', undeclared identifier 'EINTR'
   - boringssl's err_data.c is generated with Go module mode off:
     err_data_generate.go imports only the standard library, and module mode
     reached for proxy.golang.org, which CI cannot
   - a project's google-services.json is not ours to ship, so nothing is
     installed unless FIREBASE_GOOGLE_SERVICES_JSON names one. At runtime the
     app takes $GOOGLE_SERVICES_JSON, which is the one to set: its third
     fallback is the working directory, which under a systemd unit is /

October 2, 2026
1. ivi-homescreen: the plugin-common set matches the plugins tree. The
   parse-time check for disabling plugin-common was wrong in both directions;
   the set is now read from each plugin's own CMakeLists at PLUGINS_COMMIT

October 1, 2026
1. roll ivi-homescreen v3 to b7646dc and the plugins tree to 19f03e2
   - 82 commits on the v3.0 branch. The shell's CMake option surface is
     unchanged, diffed across the roll
   - the four firebase plugins are gone from the plugins tree and their
     PACKAGECONFIGs with them; gamepads_linux is added, which needs libsdl3

September 30, 2026
1. generate the custom-devices config from the recipe that builds the embedder
   (closes #522)
   - one upstream device object per recipe, written to
     ${datadir}/flutter/custom-devices/<id>.json and packaged in -dev, so
     flutter run -d <device> no longer needs a hand-written custom_devices.json
   - every knob is ?=, so a bbappend can override one without restating the
     object, and nothing is opinionated about the callbacks
   - flutter-custom-devices merges one or all of the shipped configs into the
     developer's custom_devices.json, preserving the entries already there. It
     mirrors flutter_tools' own config path resolution, writes atomically and
     keeps a .bak

September 29, 2026
1. pass the native assets mapping that actually exists: flutter_tools writes
   native_assets.json, and do_compile was globbing for native_assets.yaml
2. docs: record the environment the Flutter tooling reads. Pointing
   PUB_HOSTED_URL at a dead port to fake an offline build silently redirects
   pub's cache directory, which is the third time the environment has caused
   confusion

September 27, 2026
1. flutter-app-native builds cargo hooks (FLUTTER_NATIVE_CARGO = "1")
   - a cargo wrapper beside the cmake one, carrying the OE target, linkers,
     pkg-config and bindgen settings, offline against vendored crates
   - do_archive_pub_cache vendors the crates of hook packages
2. add ihs-wl-server-example, rolled to 7bdad95 so the bundled library is
   loaded first: the ihs_wayland_server example, whose build hook builds
   libihs_wl_server.so with cargo into the bundle at the version its pubspec
   pins. CI builds it and fails when that library is not in the package
3. common: restore pubspec.lock from the pub cache archive, and accept a
   workspace root in the guard. The archive is shared between machines and the
   lockfile is not, so only the first machine to build got one

September 26, 2026
1. add ihs-wl-server, the embedded Wayland server. Rolled to 7f76ba9 for the
   32-bit ARM build and single-pixel buffers, then c3c2ecc for an ABI check
   that does not need rustfmt -- a Yocto build has none
2. flutter-sdk: fetch the pub cache declaratively and unpack it with no network
3. roll ivi-homescreen v3.0 to 3d7a9671 for platform-view ABI 1.16

September 25, 2026
1. flutter-engine: drop the unittests PACKAGECONFIG. It cannot configure in a
   cross build, so the -test package it feeds has never been produced
2. gn: take the DEPS patch commit's dates from upstream rather than the clock

September 24, 2026
1. add dart-app: standalone Dart executables for the target (closes #428). Two
   steps, because nothing in the SDK cross-compiles in one -- the AOT kernel on
   the build machine with gen_kernel_aot out of dart-sdk-native, then the target
   snapshot
2. add dart-app-pub: pub dependencies for Dart apps, arriving through a
   pubvendor fragment, so every package comes via SRC_URI with a checksum
   bitbake verifies and pub resolves against the staged cache offline

September 23, 2026
1. dart-sdk deploys and stages the cross gen_snapshot. The dart-sdk build
   produces exactly the tool a target Dart executable needs -- one that runs on
   the build machine and emits target code -- and threw it away

September 20, 2026
1. flutter-engine: stop suppressing buildpaths on the -test package. Measured
   on 3.47.5: DW_AT_comp_dir is relative and no TMPDIR appears in any deployed
   host tool or packaged output

September 19, 2026
1. flutter-engine: keep the host tools out of the target package entirely. They
   link against the build host's glibc, so a matching architecture says nothing
   about whether the image can run them

September 18, 2026
1. roll Flutter SDK to 3.47.5 (Dart 3.13.4)
2. flutter-engine: split the host tools into their own recipe. gen_snapshot and
   friends were installed into the target package and reached apps through the
   target sysroot, which is why uninative never relocated them -- its hook skips
   target recipes, so sstate built on a newer glibc was unusable on an older
   host
3. flutter-engine: stage the engine SDK unzipped. In the zip nothing could see
   inside it: not sstate, not the buildpaths or reproducibility checks, and not
   uninative, which cannot relocate a binary it cannot find
4. maintainers: correct the spelling of my name in 18 entries, and use the
   linux.com address I sign off with

September 17, 2026
1. pdfium: give gn a native toolchain. On x86 targets pdfium builds its own nasm
   and ran it on libjpeg_turbo's SIMD sources, having built it for the target
2. pdfium: install icudtl.dat only with v8. Nothing loads ICU data unless V8 is
   initialized, so this saves about 10 MB in the default configuration. Patch by
   Harry Bock (fixes #991). CI builds pdfium on qemux86-64
3. tools: fail when common.py disagrees with the branch. OVERRIDE_STYLE and
   LICENSE_OPERATOR are each correct on one branch and a regression on another,
   and both have been carried across by a port before
4. roll: emit packagegroup entries in a stable order. glob.iglob returns
   directory order, so a roll produced a reordering diff with the real change
   buried in it

September 16, 2026
1. ci: parse and resolve the layer for musl. flutter-engine, dart-sdk and pdfium
   all carry libc-musl overrides and nothing ever read them

September 15, 2026
1. pdfium: pass the distro toolchain flags to gn. cc in the generated toolchain
   file is a bare ${TARGET_SYS}-gcc, so the flags oe-core puts on CC never
   reached the compile lines gn bakes into build.ninja, and libpdfium.so tripped
   the 32bit-time QA check on arm

September 11, 2026
1. roll Flutter SDK to 3.47.4 (Dart 3.13.3)

September 9, 2026
1. flutter-sdk: drop the dead material fonts fetch. destsuffix is implemented by
   the git, hg, npm and npmsw fetchers; wget inherits the generic unpack, which
   honors subdir alone and silently ignores an unknown parameter
2. tools: check the layer for the defects that keep shipping -- an unwired patch,
   an ineffective destsuffix, a dangling packagegroup, British spelling

September 8, 2026
1. flutter-engine: give consumers a package name to depend on (fixes #887).
   file-rdeps resolves a shared library against the packages named in a recipe's
   RDEPENDS and does not follow a metapackage's own
2. dart: stop baking the builder's path into the snapshots. Backport of dart
   CL 543800: every snapshot under bin/snapshots/ carried the absolute path of
   its entry point, which building from source makes our TMPDIR

September 4, 2026
1. the flatpak-minimal app recipes move to meta-flatpak-minimal. flatpak_dart
   and appstream_dart both live under github.com/flatpak-minimal, which now has
   its own layer; the recipes move there under the same names and this side
   drops them and the appstream_dart roll entry. CI keeps building both, since
   they are the coverage for flutter-app-native and the vendored pub cache path

September 3, 2026
1. survive clang 23. oe-core moved to clang/llvm 23.1.0, which reports unused
   function templates earlier releases did not, and this libc++ stopped
   providing several C library declarations transitively
   - flutter-engine drops -Werror on the vendored third-party sources rather
     than chasing one diagnostic at a time -- disabling -Wunused-template got
     riscv64 past double-conversion and it then failed in abseil, then in the
     engine's own harfbuzz, which also needs its guard defined
   - ivi-homescreen and its plugins take upstream's fixes: <cstdlib> before fmt
     (ivi-homescreen#506), the missing C library headers
     (ivi-homescreen-plugins#270), and the extern "C" wrappers removed from the
     glib, gstreamer, pipewire and libudev includes (#269, #271) -- glib
     declares its own linkage and reaches <type_traits> when compiled as C++
   - flathub-catalog takes appstream_dart#18 for its <ctime> include
2. flutter-app-native: install native-assets libraries where dlopen looks, and
   copy them with cp -r rather than cp -a, which preserved the build user's
   ownership and failed do_package on a uid absent from the target (#870)

September 2, 2026
1. roll: check that an app's dependencies actually solve. The static gate reads
   declared constraints, which catches an app saying outright that it needs a
   different Dart, but not one that declares a satisfiable range and still fails
   to resolve because something it depends on does not. Path dependencies are
   followed as well
2. flutter-app and flutter-sdk: stamp the SDK resolution on the way in as well
   as out, and make the staged resolution newer than what it resolves. flutter
   re-runs pub get when package_config.json does not look newer than the pubspec
   beside it, and that resolve carries no --offline
3. ci: stage a roll as a pull request when stable moves, now that the roll fails
   loudly rather than exiting 0 with recipes missing
4. ci: move the actions off Node 20

September 1, 2026
1. roll Flutter SDK to 3.47.2 (Dart 3.13.2), 102 SRCREV bumps across the app
   repositories
2. flutter-sdk: build the SDK's own example apps, and generate their recipes as
   part of the roll. The SDK this layer already stages ships dozens of buildable
   Flutter apps and the layer built none of them; they are pinned by
   FLUTTER_SDK_TAG and move with every roll, which makes them the cheapest broad
   regression coverage available. The app list comes from a clone of
   flutter/flutter at the commit the pinned release names, not a hand-kept list
3. flutter-sdk: vendor the workspace pub cache. The SDK apps are pub workspace
   members with one lockfile at the root covering all 78, so a single fragment
   serves every SDK app and its cost does not grow as recipes are added
4. flutter-app: assert the plugin registrant in the package. An app that ships
   without its plugins registered is indistinguishable from one that has none:
   no error, no missing file, just plugins that do nothing
5. pub-cache: stage the cache where pub reads it, and bound the offline resolve
   so it reports why it stopped. One CI round took 43m13s to fail and the next
   42m50s to succeed, which is not the solver working
6. flutter-apps: correct the declared license for flutter/samples. The
   repository bundles a font under OFL-1.1 and carries an Apache-2.0 section;
   the manifest declared plain BSD-3-Clause, so all 31 generated recipes shipped
   the wrong one
7. common: re-resolve after flutter clean, which deletes .dart_tool under a
   build that then carries --no-pub

August 31, 2026
1. make the staged SDK relocatable. pub records where it resolved a package and
   records it absolutely, so every package_config.json in the SDK named its
   dependencies as file:// URIs under TMPDIR
2. flutter-app-native: force libc++ as the standard library, matching what
   upstream expects of a native-assets hook
3. pub-cache: install the lockfile after any pubspec edit, and stop seeding
   pub's metadata cache from the SDK -- the SDK's hosted/pub.dev/.cache holds
   cached version listings and advisory responses, and carrying them across is
   what let an offline resolve reach for the network

August 30, 2026
1. add tools/pubvendor, which turns pubspec.lock into an SRC_URI fragment. An
   app's pub dependencies were fetched by pub get during the build, so a Flutter
   app recipe could not honor BB_NO_NETWORK and its inputs were not checksummed
   by bitbake
2. pub-cache: resolve with flutter pub get rather than dart pub get, which
   writes .dart_tool and nothing else -- a Flutter app also needs
   .flutter-plugins-dependencies. Find the lockfile in UNPACKDIR, and put the
   SDK on the task PATH
3. roll: generate a pub cache fragment for apps that opt in, and ship the
   lockfile the fragment was generated from
4. ci: run the tools test suites, and prove the staged pub cache resolves
   offline per Dart SDK. The unit tests assert what pubvendor emits; they cannot
   assert that the layout it stages into is one pub accepts
5. drop the dead buildpaths skips on the v3 -dbg packages. A -dbg package holds
   split debug info and DEBUG_PREFIX_MAP is in TARGET_CFLAGS by default, so the
   build directory is rewritten out of DWARF before packaging
6. README: note the kTouchSlop workaround for custom touch controllers, carried
   as #348 since 2023

August 29, 2026
1. let buildpaths QA run on app packages again, and keep TMPDIR out of the AOT
   image. Flutter app packages skipped the check, so the absolute path the AOT
   image carried never failed anything -- it shipped in every release libapp.so
   of an app with plugins
2. record that the LICENSE operator differs by branch, in the README and as a
   roll check. master joins licenses with AND and the release branches with "&";
   both are correct on their own branch and converting either way is a
   regression on the other
3. ci: stop demoting license-format for the whole build

August 28, 2026
1. replace the rights reservation with an SPDX identifier, and emit SPDX headers
   from the generators (#283). "All rights reserved" sits badly with the layer's
   own MIT license, which grants the right to use the work
2. flutter-elinux: package the Wayland library, and ignore empty engine modes,
   which otherwise leave a dangling libflutter_engine.so symlink
3. flutter-engine: expose the shared library. file-rdeps checks direct
   dependencies and cannot see the runtime-mode packages behind the metapackage

August 27, 2026
1. consolidate the four machine workflows and build the FFI apps. They had
   drifted apart: riscv64 had lost sentry and rive-text, arm had lost the
   application builds, pdfium was building on two machines of four
2. roll ivi-homescreen v3 to 131cbc37, where two of the ten integration tests no
   longer carry a stale pubspec.lock
3. ci: clean after an interrupted run rather than before every build, which was
   rebuilding the layer from source on every job

August 25, 2026
1. common: report the output of a failed command. run_command logged it with
   bb.note(), which reaches only log.do_* on the builder, so every failure in a
   flutter task arrived as an exit code with no detail
2. tools: declare MIT rather than Apache-2.0 in five scripts; the layer's only
   LICENSE file is MIT

August 24, 2026
1. flutter-app: key the pub cache skip on the source and the build tree, so
   do_archive_pub_cache stops doing nothing when SRCREV moves (#653)
2. README: document FFI plugins that load a system library. Such a plugin lands
   in NativeAssetsManifest.json as ["system", "lib<name>.so"] and is dlopen'd by
   that name

August 23, 2026
1. add recipes for the ivi-homescreen v3.0 integration tests: ten Flutter
   applications, each driving one embedder subsystem through its platform
   channels -- keyboard and hardware-keyboard, multi-touch, multi-view, gestures,
   scrolling, stopwatch stress, watchdog, OSGi activator and MCP drive -- sharing
   ivi-homescreen-test.inc. The bdero flutter_scene smoke-render recipe is
   dropped in the same change
2. license: fix the QA warnings and check declarations against source
3. flutter-elinux: update to the latest version and move to the flutter-elinux
   GitHub org
4. flutter-app: let pub resolve from a workspace root. An application that is a
   pub workspace member could not be built at all -- the lockfile lives at the
   workspace root and pub refuses to operate in the member
5. ci-build: derive parallelism from memory rather than core count. The fixed
   caps were sized against one runner; a second is 48 cores and 128G, and a
   fixed number cannot suit both

August 22, 2026
1. gn-fetcher: keep build output out of the cached tarball. The sync directory
   is also the build directory, so a refetch packed the gn output tree with the
   sources -- 5.2GB against 2.2GB clean on an engine refetch

August 21, 2026
1. flutter-app: skip app recipes on 32-bit targets. flutter build bundle accepts
   only 64-bit Linux target platforms, so the argument built from the target
   arch is rejected outright
2. gn-fetcher: key the fetch on the gclient config rather than SRCREV alone,
   include every fetch input in the signature and cache key, and make a retained
   partial sync resumable so a 14GB tree does not have to be refetched
3. flutter-engine: resolve host fontconfig from fontconfig-native, since the
   host toolchain has no sysroot and was reading the build host's /usr/include;
   scope the ABI gate's unwinder rule to the bundled clang; and fold the JDK gate
   into upstream's DEPS condition
4. dart-sdk: drop the gn.py options removed in 3.13.1. Both were in the default
   PACKAGECONFIG and argparse exits 2 on an unrecognized option
5. declare LAYERSERIES_COMPAT for wrynose alongside blacksail, so the layer
   stays usable against the previous release

August 19, 2026
1. roll Flutter SDK to 3.47.1 (Dart 3.13.1)
   - dart-sdk recipe 3.10.1 -> 3.13.1
   - unblocks packages requiring hooks ^2.1.0, which needs meta >= 1.19.0 and
     could not resolve against 3.38.3
2. add ivi-homescreen v3.0 and flutter-auto v3.0, sharing ivi-homescreen-v3.inc
   (plugins track the plugins repo v2.0 branch, which has no v3.0)
   - backends are no longer mutually exclusive: wayland-egl, wayland-vulkan,
     wayland-leased-drm, drm-kms-egl, drm-kms-vulkan, software, headless-egl,
     headless-vulkan may all be built in and are selected at runtime
   - new options: compositor, hud, compositor-dmabuf-export, vulkan-validation,
     drm-kms-vulkan-probe, software sinks and input seat, client-simple-shell,
     accessibility, mcp, accesskit, osgi, fuzzers, sanitize-thread/-memory/
     -undefined, unit-tests, integration-tests, docs, lto, static-link,
     plugin-common, firebase_core, video-player-pigeon-regen, camera-pipewire
   - Wayland dependencies moved onto the Wayland backends; a DRM/KMS, software
     or headless build no longer requires the wayland distro feature. The
     leased-DRM backend deliberately omits wayland-protocols: it has no
     wl_surface and reads nothing from the package
   - parse-time consistency matrix rejects the option combinations upstream
     CMake rejects, and warns on options that would otherwise silently do
     nothing
   - the plugins tree and the v4l2-webrtc-codec encoder sources are fetched only
     when built, so disable-plugins means no plugins clone and no sd-bus
     dependency
   - the always-on common plugin vendors sdbus-c++, which needs an sd-bus
     implementation at configure time: systemd where the distro has it, else
     basu
   - crash handler DSN now comes from the SENTRY_DSN environment variable;
     CRASH_HANDLER_DSN removed, CRASHPAD_RUNTIME_PATH added
   - filament-view dropped; the plugins repo no longer carries it
   - plugins/webrtc is deliberately not exposed: it links libwebrtc's C++ API
     into the embedder and its vendored flutter-webrtc copy has drifted
3. add ivi-homescreen-shared recipe
   - builds the shared/ subtree standalone, so libihs_shared has a single owner
     and ivi-homescreen and flutter-auto can be installed side by side
   - consumers can depend on the C-ABI library alone instead of pulling in a
     whole embedder
4. add wayland-cxx-scanner recipe; ivi-homescreen v3 generates its Wayland
   bindings with a build-host run of the scanner (wayland-cxx-scanner-native)
5. add flutter-app-native.bbclass for Flutter apps whose Dart packages carry a
   native-assets build hook driving CMake. The hook runs inside flutter build
   and cannot see the OE cross environment, so the class puts a cmake wrapper on
   PATH that injects the toolchain file and pkg-config settings, then installs
   the resulting libraries into the app bundle. FLUTTER_NATIVE_VERBOSE surfaces
   the hook output, which the native-assets builder otherwise swallows
6. add app recipes: bluez_native flutter_ble_scanner, flatpak_dart
   flutter_remote_manager, packagekit_dart packagekit_catalog. Repoint
   appstream_dart flathub_catalog at flatpak-minimal and move it onto the new
   class, replacing its hand-rolled cmake and flutter-app hybrid
7. restore and update the libwebrtc recipe
   - webrtc pinned to the revision the local build tree uses, libwebrtc to main
   - absorbs the v4l2-webrtc-codec hardware decoder as a gn source_set
   - audio backends and X11 follow DISTRO_FEATURES rather than being hardcoded;
     when both wayland and x11 are present, the pipewire path wins and the libX*
     dependencies are dropped
   - desktop capture is optional but requires pipewire: it adds virtual methods
     to the public interfaces, so a consumer built without RTC_DESKTOP_DEVICE
     gets a mismatched vtable
   - stamp a versioned DT_SONAME so consumers record libwebrtc.so.144 and are
     satisfied by the runtime package alone
8. flutter-engine updates for 3.47.1
   - drop patches upstream absorbed or superseded: impeller virtual specifier,
     abseil-cpp and googletest warning fixes, and the two libc++ musl patches
     now covered by -D_LIBCPP_HAS_MUSL_LIBC
   - musl: add -DFLATBUFFERS_LOCALE_INDEPENDENT=0, and widen the swiftshader
     config fix to cover llvm-subzero and HAVE_MALLINFO2/HAVE_BACKTRACE/
     HAVE_EXECINFO_H
   - pass --no-default-linux-sysroot: flutter/tools/gn now forces
     use_default_linux_sysroot true, so editing sysroot.gni no longer works
   - pass dart_include_wasm_opt=false, which upstream applies only to host
     builds; without it the wasm-opt link fails on duplicate libc++abi symbols
   - depend on libx11 and libxcb where the distro has x11, and match the
     vulkan_headers.gni assignment however it is spelled
   - split the runtime modes into their own packages so an image carries only
     the mode it runs; flutter-engine stays as a metapackage that pulls the
     modes that were built
   - add an ABI interop gate over libflutter_engine.so: no dynamically linked
     C++ runtime, libc matching the target, a static unwinder, no exported or
     undefined C++ symbols. This is what makes a clang-built engine safe to drop
     into a GCC or musl userland
9. layer compatibility updated to blacksail

March 14, 2025
1. roll ivi-homescreen/flutter-auto
   -Remove FPS overlay
    Resolves long standing NXP weston protocol errror
    Better to use weston debug feature that prints FPS of all surfaces
   -Cursor Disable - using nullptr instead of surface

Feb 10, 2025
1. roll ivi-homescreen/flutter-auto
2. roll Flutter SDK - 3.27.4

Jan 17, 2025
1. roll ivi-homescreen/flutter-auto

Dec 20, 2024
1. remove comp-surf-pbr
2. remove rive-taffy-ffi
3. update libwebrtc to mux x11/wayland support
4. pdfium x64 builds. Added to x64 CI
5. add rive-text and sentry to CI

Dec 19, 2024
1. rebase main from scarthgap
2. Update copyright range, upstream patch status, typo

Dec 18, 2024
1. roll ivi-homescreen-plugins; filament view wayland protocol error fix

Dec 17, 2024
1. roll ivi-homescreen + plugins
2. roll flutter-auto + plugins
3. Flutter SDK 3.27.1

Nov 4, 2024
1. libwebrtc
2. roll depot_tools
3. create VPYTHON_VIRTUALENV_ROOT directory in gn fetcher
4. roll pdfium to chromium/6694

Nov 1, 2024
1. remove libcamera dynamic layer. Removes requirement for meta-multimedia
2. remove camera and webview_flutter_view from default PACKAGECONFIG

Oct 17, 2024
1. Sony: roll + conditional libuv support
2. Sony: flutter-external-texture-plugin: patch added

Oct 16, 2024
1. flutter-engine: musl cleanup

Oct 15, 2024
1. aarch64 - freetype2 depend
2. Flutter 3.24.3
3. Dart SDK 3.5.3
4. Roll meta-flutter-apps

Oct 14, 2024
1. enable flutter-engine build without wayland
2. build with musl libc

Oct 3, 2024
1. retain partial sync on fetch error

Sep 28, 2024
1. Do not copy parent directories in archive_pub

Sep 10, 2024
1. fix for pub git fetching from secure repos

Sep 9, 2024
1. unique archive name using md5 of app root (directory)
2. APP_CONFIG variable.  Installs string value as config.toml to bundle root.

Sep 8, 2024
1. Flutter SDK 3.24.2
2. dart SDK 3.5.2
3. ivi-homescreen/flutter-auto - rive-text plugin

Sep 6, 2024
1. Update ivi-homescreen-plugins for ivi-homescreen and flutter-auto
2. rive-text 0.4.11

Sep 3, 2024
1. Backout pubspec archiver
2. enforce lockfile
3. roll meta-flutter-apps
4. use PUBSPEC_IGNORE_LOCKFILE="1" as default for all rolled recipes
5. Add flutter sdk apps
6. revise flutter-sdk-native caching
7. roll ivi-homescreen/flutter-auto; filament-view updates

Aug 20, 2024
1. flutter-engine GPU symbol export patch
2. ivi-homescreen/flutter-auto set logging level to debug if !NDEBUG
3. Flutter SDK 3.24.1

Aug 16, 2024
1. ivi-homescreen EGL backend alpha transparency fix
2. env cleanup in common.inc
3. do_restore_pub_cache add --enforce-lockfile
4. improve concurrent archive fetch

Aug 15, 2024
1. add layer dependencies
2. file locking for DL_DIR write files

Aug 14, 2024
1. missing include python3-dir to common.inc

Aug 13. 2024
1. caching pub fetcher v1
2. auto-roll apps; known build breaks on gallery and super-dash

Aug 12, 2024
1. pubspec tool supports hosted and git sources

Aug 11, 2024
1. pubspec.py tool
   Enable pre-populating DL_DIR via generic options
   Walk folder specified by --input
   Restore step if --restore used
2. tools folder linter updates
3. roll recipes

Aug 10, 2024
1. pubspec.py tool
   pub cache package archiver functional unit test

Aug 9, 2024
1. flutter app build update - unique to 3.24.0
2. added missing app_root definitions in common.inc
3. tools/roll_meta_flutter.py - add support for `compiler_requires_network` key
4. remove non-building apps
5. ivi-homescreen/flutter-auto WDT phase I
6. remove pdf from ivi-homescreen default package config; due to riscv64
7. ivi-homescreen - add missing comma in build flags for pdf packageconfig
8. flutter aot generation version agnostic

Aug 8, 2024
1. add channel option to roll_meta_flutter.py
2. roll_meta_flutter.py now updates dart-sdk recipe

Aug 7, 2024
1. Flutter SDK 3.24.0
2. Dart SDK 3.5.0
3. Roll flutter-pi

Aug 6, 2024
1. flutter-engine
   - riscv64 support - requires clang-layer or will be ignored
   - uses external clang toolchain if clang-layer present
   - link everything with ldd.  Required for RISC-V
   - Vulkan DRM support via dynamic "vulkan_header" config
   - only check for x11 package if x11 is in DISTRO_FEATURES
   - do_install refactor
   - shared module .debug files install to -dbg package
2. flutter-auto/homescreen
   - vulkan-headers submodule

Aug 4, 2024
1. Dart SDK 3.4.4

Aug 1, 2024
1. Roll to Flutter SDK 3.22.3
2. ivi-homescreen/flutter-auto v2 Notify Display Update support

Jul 30, 2024
1. ivi-homescreen/flutter-auto v2 fix bad_optional_access

Jul 29, 2024
1. Roll ivi-homescreen v2 and flutter-auto v2

Jul 9, 2024
1. Discord Server

Jul 4, 2024
1. flutter-engine patch/patch rework for 3.22.2
2. remove unused patches from tools
3. set BBFILE_PRIORITY_meta-flutter correctly

Jul 3, 2024
1. roll_meta_flutter.py tool. See tools/README.md
2. correct ivi-homescreen executable name to `homescreen`

June 28, 2024
1. v2 - restore app_on_output callback assignment for AGL client
2. v2 - change ENABLE_AGL_CLIENT to ENABLE_AGL_SHELL_CLIENT

June 27, 2024
1. v2 - update package config flags
2. v2 - remove install of libwayland-gen.a from waypp
3. v2 - remove use of `find_package(OpenGL` in waypp.

June 26, 2024
1. Roll v2 - waypp/libliftoff + examples, plugin updates
2. v2 - remove file-selector from package cfg default as it depends on meta-gnome
3. v2 - add package config flag for v2 examples (waypp+libliftoff)

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
1. roll flutter-rust-bridge example
2. improve flutter app build error handling

Mar 27, 2024
1. sentry-native RISC-V
2. trim layers compatibility
3. add compatibility for deprecated layer names: meta-flutter and meta-flutter-apps

Mar 26, 2024
1. flutter-app build use glob to resolve paths with asterisk

Mar 22, 2024
1. flutter-engine: Resolve tmp path warning
2. pdfium: Resolve tmp path warning
3. membrane-example: remove
4. flutter-rust-bridge rust library: Resolve tmp path warning
5. dart-sdk: Resolve tmp path warning

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

Mar 9, 2024
1. nanbield scarthgap
2. flutter_rust_bridge example_gallery app

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
