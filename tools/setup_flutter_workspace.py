#!/usr/bin/env python3

#
# Copyright (c) 2022-2023 Joel Winarske. All rights reserved.
# Copyright (c) 2023 Toyota Connected North America. All rights reserved.
#
#
# Script that creates a Flutter Workspace
#
# Creates flutter workspace:
#
#   .config
#   .flutter-auto
#   .pub_cache
#   app
#   flutter
#   setup_env.sh
#
#
# One runs this script to create the workspace, then from working terminal
# set up the environment:
#
# "source ./setup_env.sh" or ". ./setup_env.sh"
#
# if emulator image is present type `qemu_run` to run the QEMU image
#


import errno
import io
import json
import os
import platform
import signal
import subprocess
import sys
import zipfile
from platform import system
from sys import stderr as stream

import pkg_resources

FLUTTER_AUTO_DEFAULT_WIDTH = 1920
FLUTTER_AUTO_DEFAULT_HEIGHT = 1080
FLUTTER_AUTO_DEFAULT_CURSOR_THEME = "DMZ-White"

QEMU_DEFAULT_WINDOW_TYPE = "BG"
QEMU_DEFAULT_WIDTH = 1920
QEMU_DEFAULT_HEIGHT = 1080
QEMU_DEFAULT_FULLSCREEN = True

# use kiB's
kb = 1024


def print_banner(text):
    print('*' * (len(text) + 6))
    print("** %s **" % text)
    print('*' * (len(text) + 6))


def handle_ctrl_c(signal_, frame_):
    del signal_, frame_
    sys.exit("Ctl+C - Closing")


def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--clean', default=False, action='store_true', help='Wipes workspace clean')
    parser.add_argument('--workspace-cfg', default='', type=str, help='Selects custom workspace configuration file')
    parser.add_argument('--flutter-version', default='', type=str,
                        help='Select flutter version.  Overrides config file key:'
                             ' flutter-version')
    parser.add_argument('--target-user', default='', type=str, help='Sets custom-device target user name')
    parser.add_argument('--target-address', default='', type=str, help='Sets custom-device target address')
    args = parser.parse_args()

    #
    # Target Folder
    #
    if "FLUTTER_WORKSPACE" in os.environ:
        workspace = os.environ.get('FLUTTER_WORKSPACE')
    else:
        workspace = os.getcwd()

    print_banner("Setting up Flutter Workspace in: %s" % workspace)

    #
    # Install minimum package
    #
    install_minimum_runtime_deps()

    #
    # Install required modules
    #
    required = {'pycurl'}

    installed = {pkg.key for pkg in pkg_resources.working_set}
    missing = required - installed

    if missing:
        print("Installing required Python packages: %s" % required)
        python = sys.executable
        subprocess.check_call([python, '-m', 'pip', 'install', *missing], stdout=subprocess.DEVNULL)

    #
    # Control+C handler
    #
    signal.signal(signal.SIGINT, handle_ctrl_c)

    #
    # Create Workspace
    #
    is_exist = os.path.exists(workspace)
    if not is_exist:
        os.makedirs(workspace)

    if os.path.exists(workspace):
        os.environ['FLUTTER_WORKSPACE'] = workspace

    #
    # Workspace Configuration
    #

    workspace_configuration = 'flutter_workspace_config.json'
    if args.workspace_cfg:
        workspace_configuration = args.workspace_cfg
        print("\n** Custom Workspace Configuration file: %s\n" % workspace_configuration)
    else:
        print("\n** Default Workspace Configuration file: %s\n" % workspace_configuration)

    config = get_workspace_config(workspace_configuration)

    platforms = config.get('platforms')
    for platform_ in platforms:
        if not validate_platform_config(platform_):
            print("Invalid platform configuration")
            exit(1)

    app_folder = os.path.join(workspace, 'app')
    flutter_sdk_folder = os.path.join(workspace, 'flutter')

    tmp_folder = os.path.join(workspace, '.tmp')
    agl_folder = os.path.join(workspace, '.agl')
    config_folder = os.path.join(workspace, '.config')
    flutter_auto_folder = os.path.join(workspace, '.flutter-auto')
    pub_cache_folder = os.path.join(workspace, '.pub_cache')
    vscode_folder = os.path.join(workspace, '.vscode')

    clean_workspace = False
    if args.clean:
        clean_workspace = args.clean
        if clean_workspace:
            print_banner("Cleaning Workspace")

    if clean_workspace:

        try:
            os.remove(os.path.join(workspace, 'setup_env.sh'))
        except FileNotFoundError:
            pass

        try:
            os.remove(os.path.join(workspace, 'qemu_run.scpt'))
        except FileNotFoundError:
            pass

        clear_folder(agl_folder)
        clear_folder(config_folder)
        clear_folder(flutter_auto_folder)
        clear_folder(pub_cache_folder)
        clear_folder(tmp_folder)
        clear_folder(vscode_folder)

        clear_folder(app_folder)
        clear_folder(flutter_sdk_folder)


    #
    # App folder setup
    #
    is_exist = os.path.exists(app_folder)
    if not is_exist:
        os.makedirs(app_folder)

    get_workspace_repos(app_folder, config)

    #
    # Get Flutter SDK
    #
    if args.flutter_version:
        flutter_version = args.flutter_version
    else:
        if 'flutter-version' in config:
            flutter_version = config['flutter-version']
        else:
            flutter_version = "master"

    print_banner("Flutter Version: %s" % flutter_version)
    flutter_sdk_path = get_flutter_sdk(flutter_version)
    flutter_bin_path = os.path.join(flutter_sdk_path, 'bin')

    # force tool rebuild
    force_tool_rebuild(flutter_sdk_folder)

    # Enable custom devices in dev and stable
    if flutter_version != "master":
        patch_flutter_sdk(flutter_sdk_folder)

    #
    # Configure Workspace
    #

    os.environ['PATH'] = '%s:%s' % (os.environ.get('PATH'), flutter_bin_path)
    os.environ['PUB_CACHE'] = os.path.join(os.environ.get('FLUTTER_WORKSPACE'), '.pub_cache')
    os.environ['XDG_CONFIG_HOME'] = os.path.join(os.environ.get('FLUTTER_WORKSPACE'), '.config', 'flutter')

    print("PATH=%s" % os.environ.get('PATH'))
    print("PUB_CACHE=%s" % os.environ.get('PUB_CACHE'))
    print("XDG_CONFIG_HOME=%s" % os.environ.get('XDG_CONFIG_HOME'))

    #
    # Trigger upgrade on Channel if version is all letters
    #
    if flutter_version.isalpha():
        cmd = ["flutter", "upgrade", flutter_version]
        print_banner("Upgrading `%s` Channel" % flutter_version)
        subprocess.check_call(cmd, cwd=flutter_sdk_path)

    #
    # Create script to setup environment
    #
    setup_env_script(workspace, args, platforms)

    #
    # Configure SDK
    #
    configure_flutter_sdk()

    #
    # Get runtime artifacts
    #
    get_artifacts(config, flutter_sdk_path, flutter_auto_folder, agl_folder)

    #
    # custom-devices
    #
    update_flutter_custom_devices_list(platforms)

    if flutter_version == "master":
        cmd = ['flutter', 'custom-devices', 'list']
        subprocess.check_call(cmd)

    #
    # Done
    #
    print_banner("Setup Flutter Workspace - Complete")


def test_internet_connection():
    """Test internet by connecting to nameserver"""
    import pycurl

    c = pycurl.Curl()
    c.setopt(pycurl.URL, "https://dns.google")
    c.setopt(pycurl.FOLLOWLOCATION, 0)
    c.setopt(pycurl.CONNECTTIMEOUT, 5)
    c.setopt(pycurl.NOSIGNAL, 1)
    c.setopt(pycurl.NOPROGRESS, 1)
    c.setopt(pycurl.NOBODY, 1)
    try:
        c.perform()
    except:
        pass

    res = False
    if c.getinfo(pycurl.RESPONSE_CODE) == 200:
        res = True

    c.close
    return res


def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def clear_folder(dir_):
    """ Clears folder specified """
    import shutil
    if os.path.exists(dir_):
        shutil.rmtree(dir_)


def get_workspace_config(config_file):
    """ Returns workspace config """

    data = None
    file_exists = os.path.exists(config_file)
    if not file_exists:
        print("Missing %s file" % config_file)
        default_channel = "stable"
        print("Defaulting to %s channel\n" % default_channel)
        data = json.loads('{"flutter-version":"%s","platforms":[],"repos":[]}' % default_channel)
        return data

    f = open(config_file)
    try:
        data = json.load(f)
    except json.decoder.JSONDecodeError:
        print("Invalid JSON in %s" % config_file)  # in case json is invalid
        exit(1)

    f.close()

    return data


def validate_platform_config(platform_):
    """ Validates Platform Configuration returning bool """

    if 'id' not in platform_:
        print_banner("Missing 'id' key in platform config")
        return False
    if 'type' not in platform_:
        print_banner("Missing 'type' key in platform config")
        return False
    else:
        if platform_['type'] == 'host':
            if 'flutter_runtime' not in platform_:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False
            if 'runtime' not in platform_:
                print_banner("Missing 'runtime' key in platform config")
                return False

            print("Platform ID: %s" % (platform_['id']))

        elif platform_['type'] == 'qemu':
            if 'flutter_runtime' not in platform_:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False
            if 'runtime' not in platform_:
                print_banner("Missing 'runtime' key in platform config")
                return False
            if 'custom-device' not in platform_:
                print_banner("Missing 'custom-device' key in platform config")
                return False

            print("Platform ID: %s" % (platform_['id']))

        elif platform_['type'] == 'target':
            if 'flutter_runtime' not in platform_:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False

            print("Platform ID: %s" % (platform_['id']))

        else:
            print("platform type %s is not currently supported." % (platform_['type']))

    return True


def validate_custom_device_config(config):
    """ Validates custom-device Configuration returning bool """

    if 'id' not in config:
        print_banner("Missing 'id' key in custom-device config")
        return False
    if 'label' not in config:
        print_banner("Missing 'label' key in custom-device config")
        return False
    if 'sdkNameAndVersion' not in config:
        print_banner("Missing 'sdkNameAndVersion' key in custom-device config")
        return False
    if 'platform' not in config:
        print_banner("Missing 'platform' key in custom-device config")
        return False
    if 'enabled' not in config:
        print_banner("Missing 'enabled' key in custom-device config")
        return False
    if 'ping' not in config:
        print_banner("Missing 'ping' key in custom-device config")
        return False
    if 'pingSuccessRegex' not in config:
        print_banner("Missing 'pingSuccessRegex' key in custom-device config")
        return False
    if 'postBuild' not in config:
        print_banner("Missing 'postBuild' key in custom-device config")
        return False
    if 'install' not in config:
        print_banner("Missing 'install' key in custom-device config")
        return False
    if 'uninstall' not in config:
        print_banner("Missing 'uninstall' key in custom-device config")
        return False
    if 'runDebug' not in config:
        print_banner("Missing 'runDebug' key in custom-device config")
        return False
    if 'forwardPort' not in config:
        print_banner("Missing 'forwardPort' key in custom-device config")
        return False
    if 'forwardPortSuccessRegex' not in config:
        print_banner("Missing 'forwardPortSuccessRegex' key in custom-device config")
        return False
    if 'screenshot' not in config:
        print_banner("Missing 'screenshot' key in custom-device config")
        return False

    return True


def get_workspace_repos(base_folder, config):
    """ Clone GIT repos referenced in config repos dict to base_folder """

    if 'repos' in config:
        repos = config['repos']

    else:
        repos = None

    for repo in repos:
        if 'uri' not in repo:
            print("repo entry needs a 'uri' key.  Skipping")
            continue
        if 'branch' not in repo:
            print("repo entry needs a 'branch' key.  Skipping")
            continue

        # get repo folder name
        uri = repo['uri']
        repo_name = uri.rsplit('/', 1)[-1]
        repo_name = repo_name.split(".")
        repo_name = repo_name[0]

        git_folder = os.path.join(base_folder, repo_name)

        git_folder_git = os.path.join(base_folder, repo_name, '.git')

        is_exist = os.path.exists(git_folder_git)
        if not is_exist:

            is_exist = os.path.exists(git_folder)
            if is_exist:
                os.removedirs(git_folder)

            cmd = ['git', 'clone', repo['uri'], '-b', repo['branch'], repo_name]
            subprocess.check_call(cmd, cwd=base_folder)

        if 'rev' in repo:

            cmd = ['git', 'reset', '--hard', repo['rev']]
            subprocess.check_call(cmd, cwd=git_folder)

        else:

            cmd = ['git', 'reset', '--hard']
            subprocess.check_call(cmd, cwd=git_folder)

            cmd = ['git', 'pull', '--all']
            subprocess.check_call(cmd, cwd=git_folder)

    #
    # Create vscode startup tasks
    #
    platform_ids = get_platform_ids(config.get('platforms'))
    create_vscode_launch_file(repos, platform_ids)


def get_flutter_settings_folder():
    """ Returns the path of the Custom Config json file """

    if "XDG_CONFIG_HOME" in os.environ:
        settings_folder = os.path.join(os.environ.get('XDG_CONFIG_HOME'))
    else:
        settings_folder = os.path.join(os.environ.get('HOME'), '.config', 'flutter')

    make_sure_path_exists(settings_folder)

    return settings_folder


def get_flutter_custom_config_path():
    """ Returns the path of the Flutter Custom Config json file """

    fldr_ = get_flutter_settings_folder()
    # print("folder: %s" % fldr_)
    return os.path.join(fldr_, 'custom_devices.json')


def get_flutter_custom_devices():
    """ Returns the Flutter custom_devices.json as dict """

    data = None
    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        f = open(custom_config)
        try:
            data = json.load(f)
        except json.decoder.JSONDecodeError:
            print("Invalid JSON in %s" % custom_config)  # in case json is invalid
            exit(1)
        f.close()

        if 'custom-devices' in data:
            return data['custom-devices']

    print("%s not present in filesystem." % custom_config)

    return {}


def remove_flutter_custom_devices_id(id_):
    """ Removes Flutter custom devices that match given id from the
    configuration file """

    # print("Removing custom-device with ID: %s" % id_)
    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        obj = None
        f = open(custom_config, "r")
        try:
            obj = json.load(f)
        except json.decoder.JSONDecodeError:
            print_banner("Invalid JSON in %s" % custom_config)  # in case json is invalid
            exit(1)
        f.close()

        new_device_list = []
        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id_ != device['id']:
                    new_device_list.append(device)

        custom_devices = {'custom-devices': new_device_list}

        if 'custom-devices' not in custom_devices:
            print("Removing empty file: %s" % custom_config)
            os.remove(custom_config)
            return

        with open(custom_config, "w") as outfile:
            json.dump(custom_devices, outfile, indent=2)

    return


def patch_string_array(find_token, replace_token, list_):
    return [w.replace(find_token, replace_token) for w in list_]


def patch_custom_device_strings(devices, flutter_runtime):
    """ Patch custom device string environmental variables to use literal
    values """

    workspace = os.getenv('FLUTTER_WORKSPACE')
    bundle_folder = os.getenv('BUNDLE_FOLDER')
    host_arch = get_host_machine_arch()

    for device in devices:

        token = '${FLUTTER_WORKSPACE}'

        if device.get('label'):
            if '${MACHINE_ARCH}' in device['label']:
                device['label'] = device['label'].replace('${MACHINE_ARCH}', host_arch)

        if device.get('platform'):
            if host_arch == 'x86_64':
                device['platform'] = 'linux-x64'
            elif host_arch == 'arm64':
                device['platform'] = 'linux-arm64'

        if device.get('sdkNameAndVersion'):

            if '${FLUTTER_RUNTIME}' in device['sdkNameAndVersion']:
                sdk_name_and_version = device['sdkNameAndVersion'].replace('${FLUTTER_RUNTIME}', flutter_runtime)
                device['sdkNameAndVersion'] = sdk_name_and_version

            if '${MACHINE_ARCH_HYPHEN}' in device['sdkNameAndVersion']:
                device['sdkNameAndVersion'] = device['sdkNameAndVersion'].replace('${MACHINE_ARCH_HYPHEN}', host_arch.replace('_', '-'))

        if device.get('postBuild'):
            device['postBuild'] = patch_string_array(token, workspace, device['postBuild'])

        if device.get('runDebug'):
            device['runDebug'] = patch_string_array(token, workspace, device['runDebug'])

        token = '${BUNDLE_FOLDER}'
        if device.get('install'):
            device['install'] = patch_string_array(token, bundle_folder, device['install'])

    return devices


def add_flutter_custom_device(device_config, flutter_runtime):
    """ Add a single Flutter custom device from json string """

    if not validate_custom_device_config(device_config):
        exit(1)

    # print("Adding custom-device: %s" % device_config)

    custom_devices_file = get_flutter_custom_config_path()

    new_device_list = []
    if os.path.exists(custom_devices_file):

        obj = None
        f = open(custom_devices_file, "r")
        try:
            obj = json.load(f)
        except json.decoder.JSONDecodeError:
            print_banner("Invalid JSON in %s" % custom_devices_file)  # in case json is invalid
            exit(1)
        f.close()

        id_ = device_config['id']

        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id_ != device['id']:
                    new_device_list.append(device)

    new_device_list.append(device_config)
    patched_device_list = patch_custom_device_strings(new_device_list, flutter_runtime)

    custom_devices = {'custom-devices': patched_device_list}

    # print("custom_devices_file: %s" % custom_devices_file)
    with open(custom_devices_file, "w+") as outfile:
        json.dump(custom_devices, outfile, indent=4)

    return


def update_flutter_custom_devices_list(platforms):
    """ Updates the custom_devices.json with all custom-devices in
    platforms dict """

    for platform_ in platforms:

        custom_devices = get_flutter_custom_devices()

        overwrite_existing = platform_.get('overwrite-existing')

        # check if id already exists, remove if overwrite enabled, otherwise
        # skip
        if custom_devices:
            for custom_device in custom_devices:
                if 'id' in custom_device:
                    id_ = custom_device['id']
                    if overwrite_existing and (id_ == platform_['id']):
                        # print("attempting to remove custom-device: %s" % id_)
                        remove_flutter_custom_devices_id(id_)

        add_flutter_custom_device(platform_['custom-device'], platform_['flutter_runtime'])


def configure_flutter_sdk():
    settings = {"enable-web": False, "enable-android": False, "enable-ios": False, "enable-fuchsia": False,
                "enable-custom-devices": True}

    host = get_host_type()
    if host == 'darwin':
        settings['enable-linux-desktop'] = False
        settings['enable-macos-desktop'] = True
        settings['enable-windows-desktop'] = False
    elif host == 'linux':
        settings['enable-linux-desktop'] = True
        settings['enable-macos-desktop'] = False
        settings['enable-windows-desktop'] = False
    elif host == 'windows':
        settings['enable-linux-desktop'] = False
        settings['enable-macos-desktop'] = False
        settings['enable-windows-desktop'] = True

    settings_file = os.path.join(get_flutter_settings_folder(), 'settings')

    with open(settings_file, "w+") as outfile:
        json.dump(settings, outfile, indent=2)

    cmd = ['flutter', 'config', '--no-analytics']
    subprocess.check_call(cmd)
    cmd = ['dart', '--disable-analytics']
    subprocess.check_call(cmd)
    cmd = ['flutter', 'doctor']
    subprocess.check_call(cmd)


def force_tool_rebuild(flutter_sdk_folder):
    tool_script = os.path.join(flutter_sdk_folder, 'bin', 'cache', 'flutter_tools.snapshot')

    if os.path.exists(tool_script):
        print_banner("Cleaning Flutter Tool")

        cmd = ["rm", tool_script]
        subprocess.check_call(cmd, cwd=flutter_sdk_folder)


def patch_flutter_sdk(flutter_sdk_folder):
    host = get_host_type()

    if host == "linux":
        print_banner("Patching Flutter SDK")

        cmd = ["bash", "-c", "sed -i -e \"/const Feature flutterCustomDevicesFeature/a const"
                             " Feature flutterCustomDevicesFeature = Feature\\(\\n  name: "
                             "\\\'Early support for custom device types\\\',\\n  configSetting:"
                             " \\\'enable-custom-devices\\\',\\n  environmentOverride: "
                             "\\\'FLUTTER_CUSTOM_DEVICES\\\',\\n  master: FeatureChannelSetting"
                             "(\\n    available: true,\\n  \\),\\n  beta: FeatureChannelSettin"
                             "\\g(\\n    available: true,\\n  \\),\\n  stable: "
                             "FeatureChannelSetting(\\n    available: true,\\n  \\)\\n);\" -e "
                             "\"/const Feature flutterCustomDevicesFeature/,/);/d\" packages/"
                             "flutter_tools/lib/src/features.dart"]
        subprocess.check_call(cmd, cwd=flutter_sdk_folder)


# Check for flutter SDK path. Pull if exists. Create dir and clone sdk if not.
def get_flutter_sdk(version):
    """ Get Flutter SDK clone """

    workspace = os.environ.get('FLUTTER_WORKSPACE')

    flutter_sdk_path = os.path.join(workspace, 'flutter')

    #
    # GIT repo
    #
    if is_repo(flutter_sdk_path):

        print('Checking out %s' % version)
        cmd = ["git", "reset", "--hard"]
        subprocess.check_call(cmd, cwd=flutter_sdk_path)
        cmd = ["git", "checkout", version]
        subprocess.check_call(cmd, cwd=flutter_sdk_path)

    else:

        flutter_repo = 'https://github.com/flutter/flutter.git'

        cmd = ["git", "clone", flutter_repo, flutter_sdk_path]
        subprocess.check_call(cmd)

        print('Checking out %s' % version)
        cmd = ["git", "checkout", version]
        subprocess.check_call(cmd, cwd=flutter_sdk_path)

    print_banner("FLUTTER_SDK: %s" % flutter_sdk_path)

    return flutter_sdk_path


def get_flutter_engine_version(flutter_sdk_path):
    """ Get Engine Commit from Flutter SDK """

    engine_version_file = os.path.join(flutter_sdk_path, 'bin/internal/engine.version')

    with open(engine_version_file) as f:
        engine_version = f.read()
        print(f"Engine Version: {engine_version.strip()}")

    return engine_version.strip()


def get_process_stdout(cmd):
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, universal_newlines=True)
    ret = ""
    for line in process.stdout:
        ret += str(line)
    process.wait()
    return ret


def get_freedesktop_os_release():
    """ Read /etc/os-release into dictionary """

    with open("/etc/os-release") as f:
        d = {}
        for line in f:
            line = line.strip()
            k, v = line.rstrip().split("=")
            d[k] = v.strip('"')
        return d


def get_host_type():
    return system().lower().rstrip()


def fetch_https_progress(download_t, download_d, upload_t, upload_d):
    """callback function for pycurl.XFERINFOFUNCTION"""
    del upload_t, upload_d
    stream.write('Progress: {}/{} kiB ({}%)\r'.format(str(int(download_d / kb)), str(int(download_t / kb)),
                                                      str(int(download_d / download_t * 100) if download_t > 0 else 0)))
    stream.flush()


def fetch_https_binary_file(url, filename, redirect, headers):
    """Fetches binary file via HTTPS"""
    import pycurl
    import time

    retries_left = 3
    delay_between_retries = 5  # seconds
    success = False

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.CONNECTTIMEOUT, 30)
    c.setopt(pycurl.NOSIGNAL, 1)
    c.setopt(pycurl.NOPROGRESS, False)
    c.setopt(pycurl.XFERINFOFUNCTION, fetch_https_progress)

    if headers:
        c.setopt(pycurl.HTTPHEADER, headers)

    if redirect:
        c.setopt(pycurl.FOLLOWLOCATION, 1)
        c.setopt(pycurl.AUTOREFERER, 1)
        c.setopt(pycurl.MAXREDIRS, 255)

    while retries_left > 0:
        try:
            with open(filename, 'wb') as f:
                c.setopt(c.WRITEFUNCTION, f.write)
                c.perform()

            success = True
            break

        except pycurl.error:
            retries_left -= 1
            time.sleep(delay_between_retries)

    c.close()
    os.sync()

    return success


def get_host_machine_arch():
    return platform.machine()


def get_artifacts(config, flutter_sdk_path, flutter_auto_folder, agl_folder):
    """ Get x86_64 Engine artifacts """

    tmp_folder = get_workspace_tmp_folder()
    make_sure_path_exists(tmp_folder)

    arch = get_host_machine_arch()

    fetch_linux_engine = False
    flutter_runtime = 'debug'

    platforms = config['platforms']

    for platform_ in platforms:

        fetch_linux_engine = True

        flutter_runtime = platform_['flutter_runtime']

        # if platform_['id'] == 'AGL-qemu' and platform_['type'] == 'qemu':
        #    install_agl_qemu_image(agl_folder, config, platform_)

        if get_host_type() == 'linux' and arch == "x86_64":
            if platform_['id'] == 'desktop-auto' and platform_['type'] == 'host':
                install_flutter_auto(flutter_auto_folder, config, platform_)

    if fetch_linux_engine:

        engine_arch = 'x64' if arch == 'x86_64' else 'arm64'
        print_banner("Fetching linux-%s Flutter Engine" % engine_arch)

        engine_version = get_flutter_engine_version(flutter_sdk_path)
        if arch == 'x86_64':
            url = 'https://storage.googleapis.com/flutter_infra_release/flutter/%s/linux-x64/linux-x64-embedder' % \
                  engine_version
        elif arch == 'arm64':
            # download something as a place holder for later
            url = 'https://storage.googleapis.com/flutter_infra_release/flutter/%s/linux-arm64/artifacts.zip' % \
                  engine_version
        else:
            print("arch %s not supported" % arch)
            return

        flutter_engine_zip = "%s/embedder.zip" % tmp_folder

        host_type = get_host_type()

        print("** Downloading %s via %s" % (flutter_engine_zip, url))
        res = fetch_https_binary_file(url, flutter_engine_zip, False, None)
        print(res)
        if not res:
            print_banner("Failed to download %s" % flutter_engine_zip)
            return
        if not os.path.exists(flutter_engine_zip):
            print_banner("Failed to download %s" % flutter_engine_zip)
            return
        print("** Downloaded %s" % flutter_engine_zip)

        bundle_folder = os.path.join(flutter_auto_folder, engine_version[0:7], '%s-x64' % host_type, flutter_runtime,
                                     'bundle')
        os.environ['BUNDLE_FOLDER'] = bundle_folder

        lib_folder = os.path.join(bundle_folder, 'lib')
        make_sure_path_exists(lib_folder)

        data_folder = os.path.join(bundle_folder, 'data')
        make_sure_path_exists(data_folder)

        icudtl_source = os.path.join(flutter_sdk_path,
                                     "bin/cache/artifacts/engine/%s/icudtl.dat" %
                                     'linux-x64' if get_host_type() == 'linux' else 'darwin-x64')
        if not os.path.exists(icudtl_source):
            cmd = ["flutter", "doctor", "-v"]
            subprocess.check_call(cmd, cwd=flutter_sdk_path)

        icudtl_source = os.path.join(flutter_sdk_path, "bin/cache/artifacts/engine/%s-x64/icudtl.dat" % host_type)
        subprocess.check_call(["cp", icudtl_source, "%s/" % data_folder])

        with zipfile.ZipFile(flutter_engine_zip, "r") as zip_ref:
            zip_ref.extractall(lib_folder)

        # remove archive
        cmd = ["rm", flutter_engine_zip]
        subprocess.check_output(cmd)

        # remove unused file
        if host_type == 'linux':
            cmd = ["rm", "flutter_embedder.h"]
            subprocess.check_call(cmd, cwd=lib_folder)

    clear_folder(tmp_folder)


def base64_to_string(b):
    import base64
    return base64.b64decode(b).decode('utf-8')


def get_github_token(github_token):
    if not github_token:
        part_a = "Z2hwX0Q5MzRESjJ5SF"
        part_b = "BMRFM1V0xyUTlpQmFr"
        part_c = "VFJyZGRnNzBxU1FCeQ"
        github_token = base64_to_string("%s%s%s==" % (part_a, part_b, part_c))

    return github_token


def get_github_json(token, url):
    """Function to return the JSON of GitHub REST API"""
    import pycurl

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.github+json", "Authorization: Bearer %s" % token])
    buffer = io.BytesIO()
    c.setopt(c.WRITEDATA, buffer)
    c.perform()
    return json.loads(buffer.getvalue().decode('utf-8'))


def get_github_artifact_list_json(token, url):
    """Function to return the JSON of artifact object array"""

    data = get_github_json(token, url)

    if 'artifacts' in data:
        return data.get('artifacts')

    if 'message' in data:
        sys.exit("[get_github_artifact_list_json] GitHub Message: %s" % data.get('message'))

    return {}


def get_github_workflow_runs(token, owner, repo, workflow):
    """ Gets workflow run list """

    url = "https://api.github.com/repos/%s/%s/actions/workflows/%s/runs" % (owner, repo, workflow)

    data = get_github_json(token, url)

    if 'workflow_runs' in data:
        return data.get('workflow_runs')

    if 'message' in data:
        sys.exit("[get_github_workflow_runs] GitHub Message: %s" % data.get('message'))

    return {}


def get_github_workflow_artifacts(token, owner, repo, id_):
    """ Get Workflow Artifact List """

    url = "https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts" % (owner, repo, id_)

    data = get_github_json(token, url)

    if 'artifacts' in data:
        return data.get('artifacts')

    if 'message' in data:
        sys.exit("[get_github_workflow_artifacts] GitHub Message: %s" % data.get('message'))

    return {}


def get_workspace_tmp_folder():
    """ Gets tmp folder path located in workspace"""
    workspace = os.getenv("FLUTTER_WORKSPACE")
    tmp_folder = os.path.join(workspace, '.tmp')
    make_sure_path_exists(tmp_folder)
    return tmp_folder


def get_github_artifact(token, url, filename):
    """ Gets artifact via GitHub URL"""

    tmp_file = "%s/%s" % (get_workspace_tmp_folder(), filename)

    headers = ['Authorization: token %s' % token]
    if fetch_https_binary_file(url, tmp_file, True, headers):
        return tmp_file

    return None


def ubuntu_is_pkg_installed(package):
    """Ubuntu - checks if package is installed"""

    cmd = ['dpkg-query', '-W', "--showformat='${Status}\n'", package, '|grep "install ok installed"']

    result = subprocess.run(cmd, capture_output=True, text=True).stdout.strip('\'').strip('\n')

    if package in result:
        print("Package %s Found" % package)
        return True
    else:
        print("Package %s Not Found" % package)
        return False


def ubuntu_install_pkg_if_not_installed(package):
    """Ubuntu - Installs package if not already installed"""
    if not ubuntu_is_pkg_installed(package):
        print("\n* Installing runtime package dependency: %s" % package)

        cmd = ["sudo", "apt-get", "install", "-y", package]
        subprocess.call(cmd)


def fedora_is_pkg_installed(package):
    """Fedora - checks if package is installed"""

    ext_pkg = '%s.%s' % (package, platform.machine())
    cmd = ['dnf', 'list', "installed", "|", "grep", ext_pkg]

    result = subprocess.run(cmd, capture_output=True, text=True).stdout.strip('\'').strip('\n')

    if ext_pkg in result:
        print("Package %s Found" % ext_pkg)
        return True
    else:
        print("Package %s Not Found" % ext_pkg)
        return False


def fedora_install_pkg_if_not_installed(package):
    """Fedora - Installs package if not already installed"""
    if not fedora_is_pkg_installed(package):
        print("\n* Installing runtime package dependency: %s" % package)

        cmd = ["sudo", "dnf", "install", "-y", package]
        subprocess.call(cmd)


def is_linux_host_kvm_capable():
    """Determine if CPU supports HW Hypervisor support"""
    cmd = 'cat /proc/cpuinfo |egrep "vmx|svm"'
    ps = subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    output = ps.communicate()[0]
    if len(output):
        return True
    return False


def get_mac_brew_path():
    """ Read which brew """
    result = subprocess.run(['which', 'brew'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').rstrip()


def get_mac_openssl_prefix():
    """ Read brew openssl prefix variable """
    if platform.machine() == 'arm64':
        return subprocess.check_output(
            ['arch', '-arm64', 'brew', '--prefix', 'openssl@3']).decode('utf-8').rstrip()
    else:
        return subprocess.check_output(['brew', '--prefix', 'openssl@3']).decode('utf-8').rstrip()


def mac_brew_reinstall_package(pkg):
    """ Re-installs brew package """
    if platform.machine() == 'arm64':
        subprocess.run(['arch', '-arm64', 'brew', 'reinstall', pkg])
    else:
        subprocess.run(['brew', 'reinstall', pkg])


def mac_pip3_install(pkg):
    """ Install pip3 on mac """
    if platform.machine() == 'arm64':
        cmd = 'arch -arm64 pip3 install %s' % pkg
    else:
        cmd = 'pip3 install %s' % pkg
    p = subprocess.Popen(cmd, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    text = p.stdout.read()
    p.wait()
    print(text)


def mac_is_cocoapods_installed():
    cmd = ['gem', 'list', '|', 'grep', 'cocoapods ']

    result = subprocess.run(cmd, capture_output=True, text=True).stdout.strip('\'').strip('\n')

    if 'cocoapods ' in result:
        print("Package cocoapods Found")
        return True
    else:
        print("Package cocoapods Not Found")
        return False


def mac_install_cocoapods_if_not_installed():
    if not mac_is_cocoapods_installed():
        subprocess.run(['sudo', 'gem', 'install', 'cocoapods'])
        subprocess.run(['sudo', 'gem', 'uninstall', 'ffi', '&&', 'sudo', 'gem', 'install', 'ffi', '--', '--enable-libffi-alloc'])


def install_minimum_runtime_deps():
    """Install minimum runtime deps to run this script"""
    host_type = get_host_type()

    if host_type == "linux":

        os_release = get_freedesktop_os_release().get('NAME')

        if os_release == 'Ubuntu':
            cmd = ["sudo", "apt", "update", "-y"]
            subprocess.check_output(cmd)
            ubuntu_install_pkg_if_not_installed("curl")
            ubuntu_install_pkg_if_not_installed("libcurl4-openssl-dev")
            ubuntu_install_pkg_if_not_installed("libssl-dev")

        elif os_release == 'Fedora Linux':
            cmd = ["sudo", "dnf", "update", "-y"]
            subprocess.check_output(cmd)
            fedora_install_pkg_if_not_installed("curl")
            fedora_install_pkg_if_not_installed("libcurl-devel")
            fedora_install_pkg_if_not_installed("openssl-devel")
            fedora_install_pkg_if_not_installed("gtk3-devel")

    elif host_type == "darwin":
        brew_path = get_mac_brew_path()
        if brew_path == '':
            sys.exit("brew is required for this script.  Please install")

        mac_brew_reinstall_package('openssl@3')

        mac_pip3_install(
            '--install-option="--with-openssl" --install-option="--openssl-dir=%s" pycurl' % (get_mac_openssl_prefix()))

        mac_install_cocoapods_if_not_installed()


def install_agl_qemu_image(folder, config, platform_):
    del config
    host_type = get_host_type()

    print_banner("Installing AGL emulator image")

    runtime = platform_['runtime']

    config = runtime.get('config')
    if config is None:
        config_window_type = QEMU_DEFAULT_WINDOW_TYPE
        config_width = QEMU_DEFAULT_WIDTH
        config_height = QEMU_DEFAULT_HEIGHT
        config_fullscreen = QEMU_DEFAULT_FULLSCREEN

    else:
        config_width = config.get('width')
        if config_width is None:
            config_width = QEMU_DEFAULT_WIDTH

        config_height = config.get('height')
        if config_height is None:
            config_height = QEMU_DEFAULT_HEIGHT

        config_fullscreen = config.get('fullscreen')
        if config_fullscreen is None:
            config_fullscreen = QEMU_DEFAULT_FULLSCREEN

        config_window_type = config.get('window_type')
        if config_window_type is None:
            config_window_type = QEMU_DEFAULT_WINDOW_TYPE

    make_sure_path_exists(folder)
    default_config_filepath = os.path.join(folder, 'default_config.json')
    with open(default_config_filepath, 'w+') as default_config_file:
        config = {"view": {"window_type": config_window_type, "width": config_width, "height": config_height,
                           "fullscreen": config_fullscreen}}
        json.dump(config, default_config_file, indent=2)

    if runtime.get('install_dependent_packages'):

        if host_type == "darwin":
            host_arch = get_host_machine_arch()
            if host_arch == 'arm64':
                subprocess.call(["arch", "-arm64", "brew", "reinstall", "qemu"])
            else:
                subprocess.call(["brew", "reinstall", "qemu"])

        elif host_type == "linux":

            os_release = get_freedesktop_os_release()

            username = os.environ.get('USER')

            if os_release.get('NAME') == 'Ubuntu':
                subprocess.call(["sudo", "apt-get", "install", "-y", "qemu-system-x86", "ovmf"])
                if is_linux_host_kvm_capable():
                    print_banner("KVM is supported")
                    subprocess.call(["sudo", "apt-get", "install", "-y", "qemu-kvm",
                                     "libvirt-daemon-system", "libvirt-clients", "bridge-utils"])
                    subprocess.call(["sudo", "adduser", username, "libvirt"])
                    subprocess.call(["sudo", "adduser", username, "kvm"])
                    subprocess.call(["sudo", "systemctl", "status", "libvirtd", "--no-pager", "-l"])
                else:
                    print_banner("KVM is not supported.  Consider enabling in BIOS for better performance.")

            elif os_release.get('NAME') == 'Fedora Linux':
                subprocess.call(["sudo", "dnf", "install", "-y", "qemu-system-x86", "edk2-ovmf"])
                if is_linux_host_kvm_capable():
                    subprocess.call(["sudo", "dnf", "install", "-y", "bridge-utils", "libvirt",
                                     "virt-install", "qemu-kvm"])
                    subprocess.call(["sudo", "systemctl", "status", "libvirtd", "--no-pager", "-l"])
                    subprocess.call(["sudo", "dnf", "install", "-y", "libvirt-devel", "virt-top",
                                     "libguestfs-tools", "guestfs-tools"])

    if runtime.get('artifact_source') == "github":
        github_artifact = runtime.get('github_artifact')
        if '${FLUTTER_RUNTIME}' in github_artifact:
            github_artifact = github_artifact.replace('${FLUTTER_RUNTIME}', platform_.get('flutter_runtime'))

        arch = get_host_machine_arch()
        if '${MACHINE_ARCH_HYPHEN}' in github_artifact:
            github_artifact = github_artifact.replace('${MACHINE_ARCH_HYPHEN}', arch.replace('_', '-'))

        github_workflow = runtime.get('github_workflow')
        if '${MACHINE_ARCH}' in github_workflow:
            github_workflow = github_workflow.replace('${MACHINE_ARCH}', arch)

        install_github_artifact_agl_emu_image(get_github_token(config.get('github_token')),
                                              runtime.get('github_owner'), runtime.get('github_repo'),
                                              github_workflow, github_artifact)


def install_github_artifact_agl_emu_image(token, owner, repo, workflow, artifact_name):
    """Installs AGL emulation image GitHub artifact"""

    if token and owner and repo and workflow and artifact_name:

        workflow_runs = get_github_workflow_runs(token, owner, repo, workflow)
        run_id = None
        for run in workflow_runs:
            if run['conclusion'] == "success":
                run_id = run['id']
                break

        artifacts = get_github_workflow_artifacts(token, owner, repo, run_id)

        for artifact in artifacts:

            name = artifact.get('name')

            if name == artifact_name:

                url = artifact.get('archive_download_url')

                print("Downloading %s run_id: %s via %s" % (workflow, run_id, url))

                filename = "%s.zip" % name
                downloaded_file = get_github_artifact(token, url, filename)
                if downloaded_file is None:
                    print_banner("Failed to download %s" % filename)
                    break

                print("Downloaded: %s" % downloaded_file)

                workspace = os.getenv('FLUTTER_WORKSPACE')

                image_path = os.path.join(workspace, '.agl')
                with zipfile.ZipFile(downloaded_file, "r") as zip_ref:
                    zip_ref.extractall(image_path)

                cmd = ["rm", downloaded_file]
                subprocess.check_output(cmd)
                break


def install_flutter_auto(folder, config, platform_):
    del config
    host_type = get_host_type()

    if host_type == "linux":

        print_banner("Installing flutter-auto")

        runtime = platform_['runtime']

        config = runtime.get('config')
        if config is None:
            config_width = FLUTTER_AUTO_DEFAULT_WIDTH
            config_height = FLUTTER_AUTO_DEFAULT_HEIGHT
            config_cursor_theme = FLUTTER_AUTO_DEFAULT_CURSOR_THEME

        else:
            config_width = config.get('width')
            if config_width is None:
                config_width = FLUTTER_AUTO_DEFAULT_WIDTH

            config_height = config.get('height')
            if config_height is None:
                config_height = FLUTTER_AUTO_DEFAULT_HEIGHT

            config_cursor_theme = config.get('cursor_theme')
            if config_cursor_theme is None:
                config_cursor_theme = FLUTTER_AUTO_DEFAULT_CURSOR_THEME

        make_sure_path_exists(folder)
        default_config_filepath = os.path.join(folder, 'default_config.json')
        with open(default_config_filepath, 'w+') as default_config_file:
            config = {"cursor_theme": config_cursor_theme, "view": {"width": config_width, "height": config_height}}
            json.dump(config, default_config_file, indent=2)

        if runtime.get('install_dependent_packages'):

            os_release = get_freedesktop_os_release()

            if os_release.get('NAME') == 'Ubuntu':
                subprocess.call(["sudo", "snap", "install", "cmake", "--classic"])
                subprocess.call(["sudo", "add-apt-repository", "-y", "ppa:kisak/kisak-mesa"])
                subprocess.call(["sudo", "apt", "update", "-y"])
                subprocess.call([
                    "sudo", "apt", "purge", "-y", "clang", "libunwind-dev", "clang-14", "lldb-14", "lld-14", "clangd-14",
                    "clang-tidy-14", "clang-format-14", "clang-tools-14", "llvm-14-dev", "lld-14", "lldb-14",
                    "llvm-14-tools", "libomp-14-dev", "libc++-14-dev", "libc++abi-14-dev", "libclang-common-14-dev",
                    "libclang-14-dev", "libclang-cpp14-dev", "libunwind-14-dev"
                    ])
                subprocess.call(["rm", "./llvm.sh"])
                subprocess.call(["wget", "https://apt.llvm.org/llvm.sh"])
                subprocess.call(["chmod", "+x", "./llvm.sh"])
                subprocess.call(["sed", "-i", 's/add-apt-repository "/add-apt-repository -y "/g', "./llvm.sh"])
                subprocess.call(["sudo", "./llvm.sh", "14"])
                subprocess.call(["rm", "./llvm.sh"])
                subprocess.call([
                    "sudo", "apt-get", "install", "-y", "clang-14", "lldb-14", "lld-14", "clangd-14",
                    "clang-tidy-14", "clang-format-14", "clang-tools-14", "llvm-14-dev", "lld-14", "lldb-14",
                    "llvm-14-tools", "libomp-14-dev", "libc++-14-dev", "libc++abi-14-dev", "libclang-common-14-dev",
                    "libclang-14-dev", "libclang-cpp14-dev", "libunwind-14-dev"
                    ])
                subprocess.call([
                    "sudo", "apt-get", "-y", "install", "libwayland-dev", "wayland-protocols", "mesa-common-dev",
                    "libegl1-mesa-dev", "libgles2-mesa-dev", "mesa-utils", "libxkbcommon-dev", "vulkan-tools",
                    "ninja-build"
                    ])
                subprocess.call([
                    "sudo", "apt", "-y", "autoremove"
                    ])

            elif os_release.get('NAME') == 'Fedora Linux':

                subprocess.call(["sudo", "dnf", "-y", "install", "wayland-devel", "wayland-protocols-devel",
                                 "mesa-dri-drivers", "mesa-filesystem", "mesa-libEGL-devel", "mesa-libGL-devel",
                                 "mesa-libGLU-devel", "mesa-libgbm-devel", "mesa-libglapi", "mesa-libxatracker",
                                 "mesa-vulkan-drivers", "vulkan-tools", "libunwind-devel", "libxkbcommon-devel",
                                 "clang", "clang-analyzer", "clang-devel", "clang-libs", "clang-resource-filesystem",
                                 "llvm-devel", "clang-tools-extra", "lld", "lld-libs", "lldb", "libcxx", "libcxx-devel",
                                 "libcxx-static", "libcxxabi", "libcxxabi-devel", "libcxxabi-static",
                                 "gstreamer1-devel", "gstreamer1-plugins-base-devel",
                                 "gstreamer1-plugins-bad-free-devel",
                                 "gstreamer1-plugins-bad-free-extras", "gstreamer1-plugins-base-tools",
                                 "gstreamer1-plugins-good", "gstreamer1-plugins-good-extras",
                                 "gstreamer1-plugins-ugly-free", "cmake", "ninja-build"])

            print("** CMake Version")
            subprocess.call(["cmake", "--version"])

        if 'github' == runtime.get('artifact_source'):

            github_artifact = runtime.get('github_artifact')
            if '${BACKEND}' in github_artifact:
                backend = runtime.get('backend')
                github_artifact = github_artifact.replace('${BACKEND}', backend)

            install_flutter_auto_github_artifact(get_github_token(config.get('github_token')),
                                                 runtime.get('github_owner'), runtime.get('github_repo'),
                                                 runtime.get('github_workflow'), github_artifact)


def install_flutter_auto_github_artifact(token, owner, repo, workflow, github_artifact):
    """Installs flutter-auto GitHub artifact"""

    os_release = get_freedesktop_os_release().get('NAME')

    if os_release == 'Ubuntu':
        github_artifact = "%s.amd64.deb.zip" % github_artifact
    elif os_release == 'Fedora Linux':
        github_artifact = "%s.x86_64.rpm.zip" % github_artifact

    if token and owner and repo and workflow and github_artifact:

        workflow_runs = get_github_workflow_runs(token, owner, repo, workflow)
        run_id = None
        for run in workflow_runs:
            if "success" == run['conclusion']:
                run_id = run['id']
                break

        artifacts = get_github_workflow_artifacts(token, owner, repo, run_id)

        for artifact in artifacts:

            if github_artifact in artifact.get('name'):

                url = artifact.get('archive_download_url')

                print("** Downloading %s run_id: %s via %s" % (workflow, run_id, url))

                downloaded_file = get_github_artifact(token, url, github_artifact)
                print("** Downloaded: %s" % downloaded_file)

                files_to_remove = None
                with zipfile.ZipFile(downloaded_file, "r") as zip_ref:
                    filelist = zip_ref.namelist()
                    files_to_remove = filelist
                    zip_ref.extractall()

                cmd = ["rm", downloaded_file]
                subprocess.check_output(cmd)


                if os_release == 'Ubuntu':

                    deb_file = None
                    for f in filelist:
                        if ".deb" in f:
                            deb_file = f
                            break

                    cmd = ["sudo", "apt", "purge", "-y", "flutter-auto"]
                    subprocess.call(cmd)

                    cmd = ["sudo", "apt", "purge", "-y", "flutter-auto-dbg"]
                    subprocess.call(cmd)

                    cmd = ["sudo", "apt", "install", "-y", "./%s" % deb_file]
                    subprocess.call(cmd)

                    cmd = ["rm", "./%s" % deb_file]
                    subprocess.call(cmd)

                if os_release == 'Fedora Linux':

                    rpm_file = None
                    for f in filelist:
                        if ".rpm" in f:
                            rpm_file = f
                            break

                    cmd = ["sudo", "dnf", "remove", "-y", "flutter-auto"]
                    subprocess.call(cmd)

                    cmd = ["sudo", "dnf", "remove", "-y", "flutter-auto-dbg"]
                    subprocess.call(cmd)

                    cmd = ["sudo", "dnf", "install", "-y", "./%s" % rpm_file]
                    subprocess.call(cmd)

                for f in files_to_remove:
                    cmd = ["rm", f]
                    subprocess.check_output(cmd)
                    break


def is_repo(path):
    return os.path.exists(os.path.join(path, ".git"))


def random_mac():
    import random
    return [0x00, 0x16, 0x3e,
            random.randint(0x00, 0x7f),
            random.randint(0x00, 0xff),
            random.randint(0x00, 0xff)]


def mac_pretty_print(mac):
    return ':'.join(map(lambda x: "%02x" % x, mac))


env_prefix = '''#!/usr/bin/env bash -l

pushd . > '/dev/null'
SCRIPT_PATH=\"${BASH_SOURCE[0]:-$0}\"

while [ -h \"$SCRIPT_PATH\" ]
do
    cd \"$( dirname -- \"$SCRIPT_PATH\"; )\"
    SCRIPT_PATH=\"$( readlink -f -- \"$SCRIPT_PATH\"; )\"
done
cd \"$( dirname -- \"$SCRIPT_PATH\"; )\" > '/dev/null'

SCRIPT_PATH=\"$( pwd; )\"
popd  > '/dev/null'
echo SCRIPT_PATH=$SCRIPT_PATH

export FLUTTER_WORKSPACE=$SCRIPT_PATH
export PATH=$FLUTTER_WORKSPACE/flutter/bin:$PATH
export PUB_CACHE=$FLUTTER_WORKSPACE/.pub_cache
export XDG_CONFIG_HOME=$FLUTTER_WORKSPACE/.config/flutter

echo \"********************************************\"
echo \"* Setting FLUTTER_WORKSPACE to:\"
echo \"* ${FLUTTER_WORKSPACE}\"
echo \"********************************************\"

flutter doctor -v
echo \"\"

flutter custom-devices list
echo \"\"
'''

env_qemu = '''
echo \"********************************************\"
echo \"* Type 'qemu_run' to start the emulator    *\"
echo \"********************************************\"
qemu_run() {
    if [ -z ${QEMU_IMAGE+x} ];
    then
        export QEMU_IMAGE=${FLUTTER_WORKSPACE}/%s
    fi
    if pgrep -x \"%s\" > /dev/null
    then
        echo '%s running - do nothing'
    else
        %s
    fi
}
'''
env_qemu_applescript = '''
#!/usr/bin/osascript

tell application "Finder"
        set flutter_workspace to system attribute "FLUTTER_WORKSPACE"
    set p_path to POSIX path of flutter_workspace
    tell application "Terminal"
        activate
        set a to do script "cd " & quoted form of p_path & " && %s %s"
    end tell
end tell
'''


def setup_env_script(workspace, args, platform_):
    """Creates bash script to set up environment variables"""

    del args
    environment_script = os.path.join(workspace, 'setup_env.sh')

    with open(environment_script, 'w+') as script:
        script.write(env_prefix)
        for item in platform_:
            if 'type' in item:

                if "qemu" == item['type']:

                    runtime = item['runtime']

                    arch = get_host_machine_arch()
                    arch_hyphen = arch.replace('_', '-')


                    # qemu command
                    cmd = runtime.get('cmd')
                    if '${FORMAL_MACHINE_ARCH}' in cmd:
                        if arch == 'arm64':
                            cmd = cmd.replace('${FORMAL_MACHINE_ARCH}', 'aarch64')
                        elif arch == 'x86_64':
                            cmd = cmd.replace('${FORMAL_MACHINE_ARCH}', 'x86_64')

                    args = runtime.get('args')
                    kernel = runtime.get('kernel')
                    qemu_image = runtime.get('qemu_image')

                    if arch == 'x86_64':

                        # args
                        if args == None:
                            args = runtime.get('args_x86_64')

                        if '${KERNEL}' in args:
                            if kernel == None:
                                kernel = runtime.get('kernel_x86_64')
                                if kernel == None:
                                    print_banner('Missing kernel_x86_64 key value in config')
                                    exit(1)
                            args = args.replace('${KERNEL}', kernel)

                        # qemu image
                        if qemu_image == None:
                            qemu_image = runtime.get('qemu_image_x86_64')


                    elif arch == 'arm64':

                        # args
                        if args == None:
                            args = runtime.get('args_arm64')

                        if '${KERNEL}' in args:
                            if kernel == None:
                                kernel = runtime.get('kernel_arm64')
                                if kernel == None:
                                    print_banner('Missing kernel_arm64 key value in config')
                                    exit(1)
                            args = args.replace('${KERNEL}', kernel)

                        # qemu image
                        if qemu_image == None:
                            qemu_image = runtime.get('qemu_image_arm64')

                        if '${QEMU_IMAGE}' in args:
                            args = args.replace('${QEMU_IMAGE}', qemu_image)


                    if '${MACHINE_ARCH_HYPHEN}' in qemu_image:
                        qemu_image = qemu_image.replace('${MACHINE_ARCH_HYPHEN}', arch_hyphen)

                    if '${FLUTTER_RUNTIME}' in qemu_image:
                        qemu_image = qemu_image.replace('${FLUTTER_RUNTIME}', item.get('flutter_runtime'))

                    if '${FLUTTER_RUNTIME}' in args:
                        args = args.replace('${FLUTTER_RUNTIME}', item.get('flutter_runtime'))

                    if '${RANDOM_MAC}' in args:
                        args = args.replace('${RANDOM_MAC}', mac_pretty_print(random_mac()))


                    #
                    # extras
                    #
                    qemu_extra = ''

                    host_type = get_host_type()

                    if host_type == "linux":

                        os_release = get_freedesktop_os_release().get('NAME')

                        if os_release == 'Ubuntu':
                            print_banner('QEMU_EXTRA: %s' % runtime.get('qemu_extra_ubuntu'))
                            qemu_extra = runtime.get('qemu_extra_ubuntu')

                        elif os_release == 'Fedora Linux':
                            print_banner('QEMU_EXTRA: %s' % runtime.get('qemu_extra_fedora'))
                            qemu_extra = runtime.get('qemu_extra_fedora')


                        if is_linux_host_kvm_capable():
                            args = format('-enable-kvm %s' % args)

                    elif host_type == "darwin":

                        print_banner('QEMU_EXTRA: %s' % runtime.get('qemu_extra_darwin'))
                        qemu_extra = runtime.get('qemu_extra_darwin')


                    if '${QEMU_EXTRA}' in args:
                        args = args.replace('${QEMU_EXTRA}', qemu_extra)


                    #
                    # writes scripts
                    #
                    terminal_cmd = ''
                    if host_type == "linux":
                        terminal_cmd = format('gnome-terminal -- bash -c \"%s %s\"' % (cmd, args))
                    elif host_type == "darwin":
                        apple_script_filename = 'qemu_run.scpt'
                        terminal_cmd = format('osascript ${FLUTTER_WORKSPACE}/%s' % apple_script_filename)
                        apple_script_file = os.path.join(workspace, apple_script_filename)
                        with open(apple_script_file, 'w+') as f:
                            f.write(format(env_qemu_applescript % (cmd, args)))

                    script.write(env_qemu % (qemu_image, cmd, cmd, terminal_cmd))


def get_platform_ids(platforms):
    """returns list of platform ids"""
    res = []
    for platform_ in platforms:
        res.append(platform_.get('id'))
    return res


def get_launch_obj(repo, device_id):
    """returns dictionary of launch target"""
    uri = repo.get('uri')
    repo_name = uri.rsplit('/', 1)[-1]
    repo_name = repo_name.split(".")
    repo_name = repo_name[0]

    pubspec_path = repo.get('pubspec_path')
    if pubspec_path is not None:
        pubspec_path = os.path.join('app', pubspec_path)
        return {"name": "%s (%s)" % (repo_name, device_id), "cwd": pubspec_path, "request": "launch", "type": "dart",
                "deviceId": device_id}
    else:
        return {}


def create_vscode_launch_file(repos, device_ids):
    """Creates a default vscode launch.json"""

    workspace = os.getenv("FLUTTER_WORKSPACE")
    vscode_folder = os.path.join(workspace, '.vscode')
    launch_file = os.path.join(vscode_folder, 'launch.json')
    if not os.path.exists(launch_file):
        launch_objs = []
        for repo in repos:
            if 'pubspec_path' in repo:
                for device_id in device_ids:
                    obj = get_launch_obj(repo, device_id)
                    launch_objs.append(obj)

        launch = {'version': '0.2.0', 'configurations': launch_objs}
        make_sure_path_exists(vscode_folder)
        with open(launch_file, 'w+') as f:
            json.dump(launch, f, indent=4)


if __name__ == "__main__":
    main()
