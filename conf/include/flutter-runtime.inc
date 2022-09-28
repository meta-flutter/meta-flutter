# needs a default or recipe validation will fail
FLUTTER_RUNTIME ??= "release"

def gn_get_flutter_runtime_name(d):
    """ Gets Flutter runtime based on FLUTTER_RUNTIME variable """
    runtime_translations = {'release': 'runtimerelease', 'profile': 'runtimeprofile', 'debug': 'runtimedebug'}
   
    flutter_runtime = d.getVar("FLUTTER_RUNTIME")
    res = runtime_translations.get(flutter_runtime, "na")
    if res == 'na':
        bb.fatal('Unsuported FLUTTER_RUNTIME value: "%s"' % flutter_runtime)
    
    return res

def gn_get_pn_without_runtime_suffix(d):
    """ Return PN without runtime suffix """
    pn = d.getVar("PN")
    res = pn.split('-runtime')
    return res[0]
