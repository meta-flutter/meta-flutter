#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

def clang_build_arch(d):
    """Returns build arch in format required for clang path."""
    import re
    arch_translations = {
        r'x86_64$': 'x64',
        r'aarch64.*': 'arm64',
    }
    host_arch = d.getVar("BUILD_ARCH")
    for arch_regexp, build_host_arch in arch_translations.items():
        if re.match(arch_regexp, host_arch):
            return build_host_arch

    bb.fatal('Unsuported BUILD_ARCH: "%s"' % host_arch)
