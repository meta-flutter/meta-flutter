# Copyright (C) 2023 Joel Winarske. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

def get_pubspec_appname(d):
    import yaml

    source_dir = d.getVar("S")
    flutter_application_path = d.getVar("FLUTTER_APPLICATION_PATH")
    pubspec_yaml = os.path.join(source_dir, flutter_application_path, 'pubspec.yaml')

    bb.warn("source_dir [%s]" % source_dir)
    bb.warn("flutter_application_path [%s]" % flutter_application_path)
    bb.warn("pubspec_yaml [%s]" % pubspec_yaml)

    if not os.path.exists(pubspec_yaml):
        bb.error("pubspec.yaml not found, check FLUTTER_APPLICATION_PATH value")

    with open(pubspec_yaml, "r") as stream:
        try:
            data_loaded = yaml.safe_load(stream)
        except yaml.YAMLError as exc:
            bb.error("Failed loading %s - %s" % (exc, pubspec_yaml))

        return data_loaded["name"]

    bb.error("failed to open %s" % pubspec_yaml)

def filter_plugin_registrant(d):
    """ Removes unused items from the dart plugin registrant file """
    import os

    dart_file = d.getVar("DART_PLUGIN_REGISTRANT_FILE")

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
            elif line is '    }\n':
                f.write(line)
                discard = False
                continue
            elif not discard:
                f.write(line)
                continue
            else:
                continue
        f.write('    }\n  }\n}\n')
