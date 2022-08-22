# Copyright 2013 The Flutter Authors. All rights reserved.
# Copyright 2021 Joel Winarske. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

def gn_get_tune_features(d):
    """ Returns tune value """
    tune_features = d.getVar("TUNE_FEATURES")
    if not tune_features:
        return tune_features
    tune_features = tune_features.replace("aarch64 ", "")
    tune_features = tune_features.replace("arm ", "")
    tune_features = tune_features.replace("vfp ", "")
    return tune_features.replace(" ", "+")

# GN host architecture helpers.
#
# BUILD_ARCH's value corresponds to what uname returns as the machine name.
# The mapping in gn_host_arch_name() tries to match several possible values
# returned by the Linux kernel in uname(2) into the corresponding values GN
# understands.
def gn_host_arch_name(d):
    """Returns a GN architecture name corresponding to the build host's machine
    architecture."""
    import re
    arch_translations = {
        r'aarch64.*': 'arm64',
        r'arm.*': 'arm',
        r'i[3456]86$': 'x86',
        r'x86_64$': 'x64',
        r'x64$': 'x64',
    }
    build_arch = d.getVar("BUILD_ARCH")
    for arch_regexp, gn_arch_name in arch_translations.items():
        if re.match(arch_regexp, build_arch):
            return gn_arch_name

    bb.fatal('Unsuported BUILD_ARCH value: "%s"' % build_arch)

# GN target architecture helpers.
#
# Determining the target architecture is more difficult, as there are many
# different values we can use on the Yocto side (e.g. TUNE_ARCH, TARGET_ARCH,
# MACHINEOVERRIDES etc). What we do is define the mapping with regular,
# non-Python variables with overrides that are generic enough (i.e. "x86"
# instead of "i586") and then use gn_target_arch_name() to return the right
# value with some validation.
GN_TARGET_ARCH_NAME:aarch64 = "arm64"
GN_TARGET_ARCH_NAME:arm = "arm"
GN_TARGET_ARCH_NAME:x86 = "x86"
GN_TARGET_ARCH_NAME:x86-64 = "x64"

def gn_target_arch_name(d):
    """Returns a GN architecture name corresponding to the target machine's
    architecture."""
    name = d.getVar("GN_TARGET_ARCH_NAME")
    if name is None:
        bb.fatal('Unsupported target architecture. A valid override for the '
                 'GN_TARGET_ARCH_NAME variable could not be found.')
    return name

def gn_clang_triple_prefix(d):
    """Returns compiler triple prefix corresponding to the target's machine
    architecture."""
    import re
    arch_translations = {
        r'arm64.*': 'aarch64-unknown-linux-gnu',
        r'arm.*': 'armv7-unknown-linux-gnueabihf',
        r'i[3456]86$': 'i386-unknown-linux-gnu',
        r'x86_64$': 'x86_64-unknown-linux-gnu',
        r'x64$': 'x86_64-unknown-linux-gnu',
    }
    build_arch = d.getVar("GN_TARGET_ARCH_NAME")
    for arch_regexp, gn_arch_name in arch_translations.items():
        if re.match(arch_regexp, build_arch):
            return gn_arch_name

    bb.fatal('Unsuported GN_TARGET_ARCH_NAME value: "%s"' % build_arch)

def parse_args(d):
    """Parses the Package Config arguments."""
    import argparse
    args = d.getVar("GN_ARGS").split()

    parser = argparse.ArgumentParser(description='A script run` gn gen`.')

    parser.add_argument('--unoptimized', default=False, action='store_true')

    parser.add_argument('--enable-unittests', action='store_true', default=False)

    parser.add_argument('--runtime-mode', type=str, choices=['debug', 'profile', 'release', 'jit_release'], default='debug')
    parser.add_argument('--interpreter', default=False, action='store_true')
    parser.add_argument('--dart-debug', default=False, action='store_true', help='Enables assertions in the Dart VM. ' +
        'Does not affect optimization levels. If you need to disable optimizations in Dart, use --full-dart-debug')
    parser.add_argument('--no-dart-version-git-info', default=False, action='store_true',
                        help='Set by default; if unset, turns off the dart SDK git hash check')
    parser.add_argument('--full-dart-debug', default=False, action='store_true', help='Implies --dart-debug ' +
        'and also disables optimizations in the Dart VM making it easier to step through VM code in the debugger.')

    parser.add_argument('--target-os', type=str, choices=['android', 'ios', 'mac', 'linux', 'fuchsia', 'wasm', 'win'])
    parser.add_argument('--android', dest='target_os', action='store_const', const='android')
    parser.add_argument('--android-cpu', type=str, choices=['arm', 'x64', 'x86', 'arm64'], default='arm')
    parser.add_argument('--ios', dest='target_os', action='store_const', const='ios')
    parser.add_argument('--ios-cpu', type=str, choices=['arm', 'arm64'], default='arm64')
    parser.add_argument('--mac', dest='target_os', action='store_const', const='mac')
    parser.add_argument('--mac-cpu', type=str, choices=['x64', 'arm64'], default='x64')
    parser.add_argument('--simulator', action='store_true', default=False)
    parser.add_argument('--linux', dest='target_os', action='store_const', const='linux')
    parser.add_argument('--fuchsia', dest='target_os', action='store_const', const='fuchsia')
    parser.add_argument('--wasm', dest='target_os', action='store_const', const='wasm')
    parser.add_argument('--windows', dest='target_os', action='store_const', const='win')

    parser.add_argument('--linux-cpu', type=str, choices=['x64', 'x86', 'arm64', 'arm'])
    parser.add_argument('--fuchsia-cpu', type=str, choices=['x64', 'arm64'], default = 'x64')
    parser.add_argument('--windows-cpu', type=str, choices=['x64', 'arm64', 'x86'], default = 'x64')
    parser.add_argument('--simulator-cpu', type=str, choices=['x64', 'arm64'], default = 'x64')
    parser.add_argument('--arm-float-abi', type=str, choices=['hard', 'soft', 'softfp'])

    parser.add_argument('--goma', default=True, action='store_true')
    parser.add_argument('--no-goma', dest='goma', action='store_false')
    parser.add_argument('--xcode-symlinks', action='store_true', help='Set to true for builds targeting macOS or iOS ' +
        'when using goma. If set, symlinks to the Xcode provided sysroot and SDKs will be created in a generated ' +
        'folder, which will avoid potential backend errors in Fuchsia RBE. Instead of specifying the flag on each invocation ' +
        'the FLUTTER_GOMA_CREATE_XCODE_SYMLINKS environment variable may be set to 1 to achieve the same effect.')
    parser.add_argument('--no-xcode-symlinks', dest='xcode_symlinks', default=False, action='store_false')
    parser.add_argument('--depot-tools', default='~/depot_tools', type=str,
                        help='Depot tools provides an alternative location for gomacc in ' +
                        '/path/to/depot_tools/.cipd_bin')

    parser.add_argument('--lto', default=True, action='store_true')
    parser.add_argument('--no-lto', dest='lto', action='store_false')

    parser.add_argument('--clang', action='store_const', const=True)
    parser.add_argument('--no-clang', dest='clang', action='store_const', const=False)

    parser.add_argument('--clang-static-analyzer', default=False, action='store_true')
    parser.add_argument('--no-clang-static-analyzer', dest='clang_static_analyzer', action='store_false')

    parser.add_argument('--target-sysroot', type=str)
    parser.add_argument('--target-toolchain', type=str)
    parser.add_argument('--target-triple', type=str)
    parser.add_argument('--operator-new-alignment', dest='operator_new_alignment', type=str, default=None)

    parser.add_argument('--macos-enable-metal', action='store_true', default=False)
    parser.add_argument('--enable-vulkan', action='store_true', default=False)

    parser.add_argument('--enable-fontconfig', action='store_true', default=False)
    parser.add_argument('--enable-vulkan-validation-layers', action='store_true', default=False)

    parser.add_argument('--enable-skshaper', action='store_true', default=True)
    parser.add_argument('--no-enable-skshaper', dest='enable_skshaper', action='store_false')
    parser.add_argument('--always-use-skshaper', action='store_true', default=False)

    parser.add_argument('--embedder-for-target', dest='embedder_for_target', action='store_true', default=False)

    parser.add_argument('--coverage', default=False, action='store_true')

    parser.add_argument('--out-dir', default='', type=str,
                        help='Root out directory. Target specific gn files will be generated in ${out-dir}/')
    parser.add_argument('--target-dir', default='', type=str,
                        help='Use the specified name for target out directory. By default this tool determines one.')

    parser.add_argument('--full-dart-sdk', default=False, action='store_true',
                        help='include trained dart2js and dartdevc snapshots. Enable only on steps that create an SDK')
    parser.add_argument('--no-full-dart-sdk', dest='full_dart_sdk', action='store_false')

    parser.add_argument('--ide', default='', type=str,
                        help='The IDE files to generate using GN. Use `gn gen help` and look for the --ide flag to' +
                        ' see supported IDEs. If this flag is not specified, a platform specific default is selected.')

    parser.add_argument('--allow-deprecated-api-calls', action='store_true', default=False,
                        help='Turns off warnings about the usage of deprecated APIs.')

    parser.add_argument('--disable-desktop-embeddings', default=False, action='store_true',
                        help='Do not include desktop embeddings in the build.')
    parser.add_argument('--build-glfw-shell', action='store_const', const=True,
                        help='Build the GLFW shell on supported platforms where it is not built by default.')
    parser.add_argument('--no-build-glfw-shell', dest='build_glfw_shell', action='store_const', const=False,
                        help='Do not build the GLFW shell on platforms where it is built by default.')
    parser.add_argument('--build-embedder-examples', action='store_const', const=True,
                        help='Build the example embedders using the Embedder API.')
    parser.add_argument('--no-build-embedder-examples', dest='build_embedder_examples', action='store_const', const=False,
                        help='Do not build the example embedders using the Embedder API.')

    parser.add_argument('--bitcode', default=False, action='store_true',
                        help='Enable bitcode for iOS targets. On debug runtime modes, this will be a marker only.')

    parser.add_argument('--stripped', default=True, action='store_true',
                        help='Strip debug symbols from the output. This defaults to true and has no effect on iOS.')
    parser.add_argument('--no-stripped', dest='stripped', action='store_false')

    parser.add_argument('--prebuilt-dart-sdk', default=True, action='store_true',
                        help='Whether to use a prebuilt Dart SDK instead of building one. This defaults to ' +
                        'true and is enabled on CI.')
    parser.add_argument('--no-prebuilt-dart-sdk', dest='prebuilt_dart_sdk', action='store_false')

    parser.add_argument('--fuchsia-target-api-level', dest='fuchsia_target_api_level')

    parser.add_argument('--support-fractional-translation', dest='support_fractional_translation', default=False,
                        action='store_true', help='Whether to allow layers to render at fraction pixel '
                        'boundaries.')

    # Flags for Dart features.
    parser.add_argument('--use-mallinfo2', dest='use_mallinfo2', default=False, action='store_true',
                        help='Use mallinfo2 to collect malloc stats.')

    # Impeller flags.
    parser.add_argument('--enable-impeller-playground', default=False, action='store_true',
                        help='Whether impeller unit tests run in playground mode.')
    parser.add_argument('--prebuilt-impellerc', default=None, type=str,
                        help='Absolute path to a prebuilt impellerc. ' +
                        'Do not use this outside of CI or with impellerc from a different engine version.')

    # Sanitizers.
    parser.add_argument('--asan', default=False, action='store_true')
    parser.add_argument('--lsan', default=False, action='store_true')
    parser.add_argument('--msan', default=False, action='store_true')
    parser.add_argument('--tsan', default=False, action='store_true')
    parser.add_argument('--ubsan', default=False, action='store_true')

    parser.add_argument('--trace-gn', default=False, action='store_true',
                        help='Write a GN trace log (gn_trace.json) in the Chromium tracing format in the build directory.')

    # Verbose output.
    parser.add_argument('--verbose', default=False, action='store_true')

    return parser.parse_known_args(args)[0]

def get_out_dir(d):
    """Gets output directory based on the Package Configuration args."""
    import os

    args = parse_args(d)

    if args.target_os is not None:
        target_dir = [args.target_os]
    else:
        target_dir = ['host']

    target_dir.append(args.runtime_mode)

    if args.simulator:
        target_dir.append('sim')

    if args.unoptimized:
        target_dir.append('unopt')

    if args.target_os != 'ios' and args.interpreter:
        target_dir.append('interpreter')

    if args.android_cpu != 'arm':
        target_dir.append(args.android_cpu)

    if args.ios_cpu != 'arm64':
        target_dir.append(args.ios_cpu)

    if args.mac_cpu != 'x64':
        target_dir.append(args.mac_cpu)

    if args.simulator_cpu != 'x64':
        target_dir.append(args.simulator_cpu)

    if args.linux_cpu is not None:
      target_dir.append(args.linux_cpu)

    if args.windows_cpu != 'x64':
      target_dir.append(args.windows_cpu)

    if args.target_os == 'fuchsia' and args.fuchsia_cpu is not None:
      target_dir.append(args.fuchsia_cpu)

    if args.support_fractional_translation:
      target_dir.append('fractional')

    # This exists for backwards compatibility of tests that are being run
    # on LUCI. This can be removed in coordination with a LUCI change:
    # https://github.com/flutter/flutter/issues/76547
    if args.macos_enable_metal:
      target_dir.append('metal')

    if args.target_dir != '':
      target_dir = [args.target_dir]

    return os.path.join(args.out_dir, 'out', '_'.join(target_dir))
