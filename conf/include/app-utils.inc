# Copyright (C) 2023 Joel Winarske. All rights reserved.
#
# SPDX-License-Identifier: MIT
#


def get_pubspec_yaml_filepath(d):
    source_dir = d.getVar("S")
    flutter_application_path = d.getVar("FLUTTER_APPLICATION_PATH")
    filepath = os.path.join(source_dir, flutter_application_path, 'pubspec.yaml')

    bb.debug(1, f'source_dir [{source_dir}]')
    bb.debug(1, f'flutter_application_path [{flutter_application_path}]')
    bb.debug(1, f'pubspec_yaml filepath [{filepath}]')

    if not os.path.exists(filepath):
        bb.fatal("pubspec.yaml not found, check FLUTTER_APPLICATION_PATH value")

    return filepath


def get_pubspec_yaml_appname(d):
    """ Returns the application name from the pubspec.yaml """

    filepath = get_pubspec_yaml_filepath(d)
    with open(filepath, "r") as f:
        lines = f.readlines()

    import re
    pattern = re.compile(r'^name:\s+(\w+)$')
    name = ''
    for line in lines:
        m = pattern.match(line)
        if m:
           name = m.group(1)
           break
    if not m:
        bb.fatal("'name' not found in pubspec.yaml")

    return name

def filter_plugin_registrant(dart_file):
    """ Removes unused items from the dart plugin registrant file """

    if not os.path.exists(dart_file):
        return

    with open(dart_file, "r") as f:
        lines = f.readlines()

    discard = False
    with open(dart_file, "w") as f:
        for line in lines:
            if line.find('import') != -1 and line.find('_android') != -1:
                continue
            elif line.find('import') != -1 and line.find('_ios') != -1:
                continue
            elif line.find('import') != -1 and line.find('_windows') != -1:
                continue
            elif line.find('import') != -1 and line.find('_macos') != -1:
                continue
            elif line.find('(Platform.isAndroid)') != -1:
                discard = True
                continue
            elif line.find('(Platform.isIOS)') != -1:
                discard = True
                continue
            elif line.find('(Platform.isMacOS)') != -1:
                discard = True
                continue
            elif line.find('(Platform.isWindows)') != -1:
                discard = True
                continue
            elif line.find('(Platform.isLinux)') != -1:
                f.write('    if (Platform.isLinux) {\n')
                discard = False
                continue
            elif line == '    }\n':
                f.write(line)
                discard = False
                continue
            elif not discard:
                f.write(line)
                continue
            else:
                continue
        f.write('    }\n  }\n}\n')
