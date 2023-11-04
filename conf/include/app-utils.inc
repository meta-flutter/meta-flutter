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
        bb.error("pubspec.yaml not found, check FLUTTER_APPLICATION_PATH value")

    return filepath


def write_pubspec_obj_to_file(d, obj):
    """ Writes YAML object to file """
    import yaml

    filepath = get_pubspec_yaml_filepath(d)
    with open(filepath, 'w') as outfile:
        yaml.dump(obj, outfile)


def get_pubspec_yaml(d):
    """ Returns python object of pubspec.yaml """
    import yaml

    filepath = get_pubspec_yaml_filepath(d)

    data_loaded = None
    with open(filepath, "r") as stream:
        try:
            data_loaded = yaml.safe_load(stream)

        except yaml.YAMLError as exc:
            bb.error(f'Failed loading {exc} - {filepath}')

        return data_loaded


def get_pubspec_appname(d):
    """ Returns the application name from the pubspec.yaml """
    yaml = get_pubspec_yaml(d)
    return yaml["name"]


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
