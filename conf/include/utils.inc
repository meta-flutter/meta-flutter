
def get_binary_pkg_arch(d):
    """Returns an architecture name corresponding to available download."""
    import re
    arch_translations = {
        r'aarch64$': 'aarch64',
        r'armv6.*': 'armv6hf',
        r'armv7.*': 'armv7hf',
        r'x86_64$': 'x64',
        r'x64$': 'x64',
    }
    target_arch = d.getVar("TARGET_ARCH")
    for arch_regexp, pkg_aarch_name in arch_translations.items():
        if re.match(arch_regexp, target_arch):
            return pkg_aarch_name

    bb.fatal('Unsuported TARGET_ARCH value: "%s"' % target_arch)
