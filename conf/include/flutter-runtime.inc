def gn_get_flutter_runtime_name(d):
    """ Gets Flutter runtime based on FLUTTER_RUNTIME variable """
    import re
    runtime_translations = {
        r'release': 'runtimerelease',
        r'profile': 'runtimeprofile',
        r'debug': 'runtimedebug',
    }
    flutter_runtime = d.getVar("FLUTTER_RUNTIME")
    for runtime_regexp, gn_flutter_runtime_name in runtime_translations.items():
        if re.match(runtime_regexp, flutter_runtime):
            return gn_flutter_runtime_name

    bb.fatal('Unsuported FLUTTER_RUNTIME value: "%s"' % flutter_runtime)
