#!/usr/bin/env python3

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
# setup the environment:
#
# "source ./setup_env.sh" or ". ./setup_env.sh"
#
# if emulator image is present type `qemu_run' to run the QEMU image
#


import errno
import json
import os
from platform import system
import pkg_resources
import signal
import subprocess
import sys
import zipfile
from sys import stderr as STREAM


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


def handle_ctrl_c(signal, frame):
    print("Ctl+C, Closing")
    sys.exit(0)


def main():

    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--clean', default=False, action='store_true',
                        help='Wipes workspace clean')
    parser.add_argument('--workspace-cfg', default='', type=str,
                        help='Selects custom workspace configuration file')
    parser.add_argument(
        '--flutter-version',
        default='',
        type=str,
        help='Select flutter version.  Overrides config file key:'
                ' flutter-version')
    parser.add_argument('--target-user', default='', type=str,
                        help='Sets custom-device target user name')
    parser.add_argument('--target-address', default='', type=str,
                        help='Sets custom-device target address')
    args = parser.parse_args()

    #
    # Check for network connection
    #
    if not test_internet_connection():
        print_banner("This script requires an internet connection")
        exit(1)

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
    required = {'requests', 'pycurl'}

    installed = {pkg.key for pkg in pkg_resources.working_set}
    missing = required - installed

    if missing:
        print("Installing required Python packages: %s" % required)
        python = sys.executable
        subprocess.check_call(
            [python, '-m', 'pip', 'install', *missing],
            stdout=subprocess.DEVNULL
        )

    #
    # Control+C handler
    #
    signal.signal(signal.SIGINT, handle_ctrl_c)

    #
    # Create Workspace
    #
    isExist = os.path.exists(workspace)
    if not isExist:
        os.makedirs(workspace)

    if os.path.exists(workspace):
        os.environ['FLUTTER_WORKSPACE'] = workspace

    #
    # Workspace Configuration
    #

    workspace_configuration = 'flutter_workspace_config.json'
    if args.workspace_cfg:
        workspace_configuration = args.workspace_cfg
        print(
            "\n** Custom Workspace Configuration file: %s\n" %
            workspace_configuration)
    else:
        print(
            "\n** Default Workspace Configuration file: %s\n" %
            workspace_configuration)

    config = get_workspace_config(workspace_configuration)

    platforms = config.get('platforms')
    for platform in platforms:
        if not validate_platform_config(platform):
            print("Invalid platform configuration")
            exit(1)

    app_folder = os.path.join(workspace, 'app')
    flutter_sdk_folder = os.path.join(workspace, 'flutter')

    tmp_folder = os.path.join(workspace, '.tmp')
    agl_folder = os.path.join(workspace, '.agl')
    config_folder = os.path.join(workspace, '.config')
    flutter_auto_folder = os.path.join(workspace, '.flutter-auto')
    pub_cache_folder = os.path.join(workspace, '.pub_cache')

    clean_workspace = False
    if args.clean:
        clean_workspace = args.clean
        if clean_workspace:
            print_banner("Cleaning Workspace")

    if clean_workspace:
        clear_folder(app_folder)
        clear_folder(flutter_sdk_folder)

        clear_folder(tmp_folder)
        clear_folder(agl_folder)
        clear_folder(config_folder)
        clear_folder(flutter_auto_folder)
        clear_folder(pub_cache_folder)
        clear_folder('.vscode')

    #
    # App folder setup
    #
    isExist = os.path.exists(app_folder)
    if not isExist:
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
    os.environ['PUB_CACHE'] = os.path.join(
        os.environ.get('FLUTTER_WORKSPACE'), '.pub_cache')
    os.environ['XDG_CONFIG_HOME'] = os.path.join(
        os.environ.get('FLUTTER_WORKSPACE'), '.config', 'flutter')

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
        command = ['flutter', 'custom-devices', 'list']
        subprocess.check_call(command)

    #
    # Done
    #
    print_banner("Setup Flutter Workspace - Complete")


def test_internet_connection():
    import http.client as httplib

    conn = httplib.HTTPSConnection("8.8.8.8", timeout=5)
    try:
        conn.request("HEAD", "/")
        return True
    except Exception:
        return False
    finally:
        conn.close()


def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def clear_folder(dir):
    ''' Clears folder specified '''
    import shutil
    if os.path.exists(dir):
        shutil.rmtree(dir)


def get_workspace_config(config_file):
    ''' Returns workspace config '''

    file_exists = os.path.exists(config_file)
    if not file_exists:
        print("Missing %s file" % config_file)
        default_channel = "stable"
        print("Defaulting to %s channel\n" % default_channel)
        data = json.loads(
            '{"flutter-version":"%s","platforms":[],"repos":[]}' %
            default_channel)
        return data

    f = open(config_file)
    try:
        data = json.load(f)
    except json.decoder.JSONDecodeError:
        print("Invalid JSON in %s" % (config_file))  # in case json is invalid
        exit(1)

    f.close()

    return data


def validate_platform_config(platform):
    ''' Validates Platform Configuration returning bool '''

    if 'id' not in platform:
        print_banner("Missing 'id' key in platform config")
        return False
    if 'type' not in platform:
        print_banner("Missing 'type' key in platform config")
        return False
    else:
        if platform['type'] == 'host':
            if 'arch' not in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if 'flutter_runtime' not in platform:
                print_banner(
                    "Missing 'flutter_runtime' key in platform config")
                return False
            if 'runtime' not in platform:
                print_banner("Missing 'runtime' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'qemu':
            if 'arch' not in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if 'flutter_runtime' not in platform:
                print_banner(
                    "Missing 'flutter_runtime' key in platform config")
                return False
            if 'runtime' not in platform:
                print_banner("Missing 'runtime' key in platform config")
                return False
            if 'custom-device' not in platform:
                print_banner("Missing 'custom-device' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'target':
            if 'arch' not in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if 'flutter_runtime' not in platform:
                print_banner(
                    "Missing 'flutter_runtime' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        else:
            print(
                "platform type %s is not currently supported." %
                (platform['type']))

    return True


def validate_custom_device_config(config):
    ''' Validates custom-device Configuration returning bool '''

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
        print_banner(
            "Missing 'forwardPortSuccessRegex' key in custom-device config")
        return False
    if 'screenshot' not in config:
        print_banner("Missing 'screenshot' key in custom-device config")
        return False

    return True


def get_workspace_repos(base_folder, config):
    ''' Clone GIT repos referenced in config repos dict to base_folder '''

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

        isExist = os.path.exists(git_folder_git)
        if not isExist:

            isExist = os.path.exists(git_folder)
            if isExist:
                os.removedirs(git_folder)

            cmd = [
                'git',
                'clone',
                repo['uri'],
                '-b',
                repo['branch'],
                repo_name]
            subprocess.check_call(cmd, cwd=base_folder)

        if 'rev' in repo:

            cmd = ['git', 'reset', '--hard', repo['rev']]
            subprocess.check_call(cmd, cwd=git_folder)

        else:

            cmd = ['git', 'reset', '--hard']
            subprocess.check_call(cmd, cwd=git_folder)

            cmd = ['git', 'pull', '--all']
            subprocess.check_call(cmd, cwd=git_folder)

        cmd = ['git', 'log', '-1']
        subprocess.check_call(cmd, cwd=git_folder)

    #
    # Create vscode startup tasks
    #
    platform_ids = get_platform_ids(config.get('platforms'))
    create_vscode_launch_file(repos, platform_ids)


def get_flutter_settings_folder():
    ''' Returns the path of the Custom Config json file '''

    if "XDG_CONFIG_HOME" in os.environ:
        settings_folder = os.path.join(os.environ.get('XDG_CONFIG_HOME'))
    else:
        settings_folder = os.path.join(
            os.environ.get('HOME'), '.config', 'flutter')

    make_sure_path_exists(settings_folder)

    return settings_folder


def get_flutter_custom_config_path():
    ''' Returns the path of the Flutter Custom Config json file '''

    fldr_ = get_flutter_settings_folder()
    # print("folder: %s" % fldr_)
    return os.path.join(fldr_, 'custom_devices.json')


def get_flutter_custom_devices():
    ''' Returns the Flutter custom_devices.json as dict '''

    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        f = open(custom_config)
        try:
            data = json.load(f)
        except json.decoder.JSONDecodeError:
            print(
                "Invalid JSON in %s" %
                (custom_config))  # in case json is invalid
            exit(1)
        f.close()

        if 'custom-devices' in data:
            return data['custom-devices']

    print("%s not present in filesystem." % custom_config)

    return {}


def remove_flutter_custom_devices_id(id):
    ''' Removes Flutter custom devices that match given id from the
    configuration file '''

    # print("Removing custom-device with ID: %s" % id)
    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        f = open(custom_config, "r")
        try:
            obj = json.load(f)
        except json.decoder.JSONDecodeError:
            print_banner("Invalid JSON in %s" %
                         (custom_config))  # in case json is invalid
            exit(1)
        f.close()

        new_device_list = []
        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id != device['id']:
                    new_device_list.append(device)

        custom_devices = {}
        custom_devices['custom-devices'] = new_device_list

        if 'custom-devices' not in custom_devices:
            print("Removing empty file: %s" % custom_config)
            os.remove(custom_config)
            return

        with open(custom_config, "w") as outfile:
            json.dump(custom_devices, outfile, indent=2)

    return


def patch_string_array(find_token, replace_token, list):
    return [w.replace(find_token, replace_token) for w in list]


def patch_custom_device_strings(devices, flutter_runtime):
    ''' Patch custom device string environmental variables to use literal
    values '''

    workspace = os.getenv('FLUTTER_WORKSPACE')
    bundle_folder = os.getenv('BUNDLE_FOLDER')

    for device in devices:

        token = '${FLUTTER_WORKSPACE}'

        if device.get('sdkNameAndVersion'):
            if '${FLUTTER_RUNTIME}' in device['sdkNameAndVersion']:
                sdkNameAndVersion = device['sdkNameAndVersion'].replace(
                    '${FLUTTER_RUNTIME}',
                    flutter_runtime)
                device['sdkNameAndVersion'] = sdkNameAndVersion

        if device.get('postBuild'):
            device['postBuild'] = patch_string_array(
                token, workspace, device['postBuild'])

        if device.get('runDebug'):
            device['runDebug'] = patch_string_array(
                token, workspace, device['runDebug'])

        token = '${BUNDLE_FOLDER}'
        if device.get('install'):
            device['install'] = patch_string_array(
                token, bundle_folder, device['install'])

    return devices


def add_flutter_custom_device(device_config, flutter_runtime):
    ''' Add a single Flutter custom device from json string '''

    if not validate_custom_device_config(device_config):
        exit(1)

    # print("Adding custom-device: %s" % device_config)

    custom_devices_file = get_flutter_custom_config_path()

    new_device_list = []
    if os.path.exists(custom_devices_file):

        f = open(custom_devices_file, "r")
        try:
            obj = json.load(f)
        except json.decoder.JSONDecodeError:
            print_banner("Invalid JSON in %s" %
                         (custom_devices_file))  # in case json is invalid
            exit(1)
        f.close()

        id = device_config['id']

        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id != device['id']:
                    new_device_list.append(device)

    new_device_list.append(device_config)
    patched_device_list = patch_custom_device_strings(
        new_device_list, flutter_runtime)

    custom_devices = {}
    custom_devices['custom-devices'] = patched_device_list

    # print("custom_devices_file: %s" % custom_devices_file)
    with open(custom_devices_file, "w+") as outfile:
        json.dump(custom_devices, outfile, indent=4)

    return


def update_flutter_custom_devices_list(platforms):
    ''' Updates the custom_devices.json with all custom-devices in
    platforms dict '''

    for platform in platforms:

        custom_devices = get_flutter_custom_devices()

        overwrite_existing = platform.get('overwrite-existing')

        # check if id already exists, remove if overwrite enabled, otherwise
        # skip
        if custom_devices:
            for custom_device in custom_devices:
                if 'id' in custom_device:
                    id = custom_device['id']
                    if overwrite_existing and (id == platform['id']):
                        # print("attempting to remove custom-device: %s" % id)
                        remove_flutter_custom_devices_id(id)

        add_flutter_custom_device(
            platform['custom-device'], platform['flutter_runtime'])


def configure_flutter_sdk():

    settings = {
        "enable-web": False,
        "enable-android": False,
        "enable-ios": False,
        "enable-fuchsia": False,
        "enable-custom-devices": True
    }

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

    command = ['flutter', 'config', '--no-analytics']
    subprocess.check_call(command)
    command = ['dart', '--disable-analytics']
    subprocess.check_call(command)
    command = ['flutter', 'doctor']
    subprocess.check_call(command)


def force_tool_rebuild(flutter_sdk_folder):

    tool_script = os.path.join(
        flutter_sdk_folder,
        'bin',
        'cache',
        'flutter_tools.snapshot')

    if os.path.exists(tool_script):

        print_banner("Cleaning Flutter Tool")

        cmd = ["rm", tool_script]
        subprocess.check_call(cmd, cwd=flutter_sdk_folder)


def patch_flutter_sdk(flutter_sdk_folder):

    host = get_host_type()

    if host == "linux":

        print_banner("Patching Flutter SDK")

        cmd = [
            "bash",
            "-c",
            "sed -i -e \"/const Feature flutterCustomDevicesFeature/a const"
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
    ''' Get Flutter SDK clone '''

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

    cmd = ['git', 'log', '-1']
    subprocess.check_call(cmd, cwd=flutter_sdk_path)

    return flutter_sdk_path


def get_flutter_engine_version(flutter_sdk_path):
    ''' Get Engine Commit from Flutter SDK '''

    engine_version_file = os.path.join(
        flutter_sdk_path, 'bin/internal/engine.version')

    with open(engine_version_file) as f:
        engine_version = f.read()
        print(f"Engine Version: {engine_version.strip()}")

    return engine_version.strip()


def get_process_stdout(cmd):
    process = subprocess.Popen(
        cmd,
        shell=True,
        stdout=subprocess.PIPE,
        universal_newlines=True)
    ret = ""
    for line in process.stdout:
        ret += str(line)
    process.wait()
    return ret


def get_freedesktop_os_release():
    ''' Read /etc/os-release into dictionary '''

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
    '''callback function for pycurl.XFERINFOFUNCTION'''
    STREAM.write('Progress: {}/{} kiB ({}%)\r'.format(
        str(int(download_d / kb)),
        str(int(download_t / kb)),
        str(int(download_d / download_t * 100) if download_t > 0 else 0)
    ))
    STREAM.flush()


def fetch_https_binary_file(url, filename, redirect, headers):
    '''Fetches binary file via HTTPS'''
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

        except BaseException:
            retries_left -= 1
            time.sleep(delay_between_retries)

    c.close()
    os.sync()

    return success


def get_artifacts(config, flutter_sdk_path, flutter_auto_folder, agl_folder):
    ''' Get x86_64 Engine artifcats '''

    tmp_folder = get_workspace_tmp_folder()
    make_sure_path_exists(tmp_folder)

    fetch_linux_x64_engine = False
    flutter_runtime = 'debug'

    platforms = config['platforms']

    for platform in platforms:

        arch = platform['arch']

        if arch == "x86_64":

            fetch_linux_x64_engine = True

            flutter_runtime = platform['flutter_runtime']

            if platform['id'] == 'AGL-qemu' and platform['type'] == 'qemu':
                install_agl_emu_image(agl_folder, config, platform)

            elif (platform['id'] == 'desktop-auto' and
                    platform['type'] == 'host'):
                install_flutter_auto(flutter_auto_folder, config, platform)

        else:
            print("%s artifacts not yet supported, skipping" % arch)

    if fetch_linux_x64_engine:

        print_banner("Fetching linux-64 Flutter Engine")

        engine_version = get_flutter_engine_version(flutter_sdk_path)
        url = 'https://storage.googleapis.com/flutter_infra_release/flutter/%s/linux-x64/linux-x64-embedder' % engine_version
        flutter_engine_zip = "%s/embedder.zip" % tmp_folder

        print("** Downloading %s via %s" % (flutter_engine_zip, url))
        res = fetch_https_binary_file(url, flutter_engine_zip, False, None)
        print(res)
        if not res:
            print_banner("Failed to download %s" % (flutter_engine_zip))
            return
        if not os.path.exists(flutter_engine_zip):
            print_banner("Failed to download %s" % (flutter_engine_zip))
            return
        print("** Downloaded %s" % (flutter_engine_zip))

        bundle_folder = os.path.join(flutter_auto_folder,
                                     engine_version[0:7],
                                     'linux-x64',
                                     flutter_runtime,
                                     'bundle')
        os.environ['BUNDLE_FOLDER'] = bundle_folder

        lib_folder = os.path.join(bundle_folder, 'lib')
        make_sure_path_exists(lib_folder)

        data_folder = os.path.join(bundle_folder, 'data')
        make_sure_path_exists(data_folder)

        icudtl_source = os.path.join(
            flutter_sdk_path,
            "bin/cache/artifacts/engine/linux-x64/icudtl.dat")
        if not os.path.exists(icudtl_source):
            cmd = ["flutter", "doctor", "-v"]
            subprocess.check_call(cmd, cwd=flutter_sdk_path)

        host_type = get_host_type()
        icudtl_source = os.path.join(
            flutter_sdk_path,
            "bin/cache/artifacts/engine/%s-x64/icudtl.dat" %
            host_type)
        subprocess.check_call(["cp", icudtl_source, "%s/" % data_folder])

        with zipfile.ZipFile(flutter_engine_zip, "r") as zip_ref:
            zip_ref.extractall(lib_folder)

        # remove archive
        cmd = ["rm", flutter_engine_zip]
        subprocess.check_output(cmd)

        # remove unused file
        cmd = ["rm", "flutter_embedder.h"]
        subprocess.check_call(cmd, cwd=lib_folder)

    clear_folder(tmp_folder)


def base64ToString(b):
    import base64
    return base64.b64decode(b).decode('utf-8')


def get_github_token(github_token):

    if not github_token:
        part_a = "Z2hwX0Q5MzRESjJ5SF"
        part_b = "BMRFM1V0xyUTlpQmFr"
        part_c = "VFJyZGRnNzBxU1FCeQ"
        github_token = base64ToString("%s%s%s==" % (part_a, part_b, part_c))

    return github_token


def get_github_artifact_list_json(token, url):
    """Function to return the JSON of artifact object array."""
    import requests

    headers = {
        "Accept": "application/vnd.github+json",
        "Authorization": "token %s" %
        (token)}
    with requests.get(
            url,
            stream=True,
            headers=headers,
            allow_redirects=True
    ) as r:
        data = r.json()

    return data['artifacts']


def get_github_workflow_runs(token, owner, repo, workflow):
    ''' Gets workflow run list '''
    import requests

    url = "https://api.github.com/repos/%s/%s/actions/workflows/%s/runs" % (
        owner, repo, workflow)

    try:
        headers = {
            "Accept": "application/vnd.github+json",
            "Authorization": "token %s" %
            (token)}
        with requests.get(
                url,
                stream=True,
                headers=headers,
                allow_redirects=True
        ) as r:
            json_data = json.loads(r.text)
            return json_data['workflow_runs']

    except requests.exceptions.HTTPError as e:
        if e.errno == 404:
            print("Artifact not available.")
            return None

    return None


def get_github_workflow_artifacts(token, owner, repo, id):
    ''' Get Workflow Artifact List '''
    import requests

    url = "https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts" % (
        owner, repo, id)

    try:
        headers = {
            "Accept": "application/vnd.github+json",
            "Authorization": "token %s" %
            (token)}
        with requests.get(url, stream=True, headers=headers) as r:
            data = r.json()

        return data['artifacts']

    except requests.exceptions.HTTPError as e:
        if e.errno == 404:
            print("Artifact list not available.")
            return None

    return None


def get_workspace_tmp_folder():
    ''' Gets tmp folder path located in workspace'''
    workspace = os.getenv("FLUTTER_WORKSPACE")
    tmp_folder = os.path.join(workspace, '.tmp')
    make_sure_path_exists(tmp_folder)
    return tmp_folder


def get_github_artifact(token, url, filename):
    ''' Gets artifact via Github URL'''

    tmp_file = "%s/%s" % (get_workspace_tmp_folder(), filename)

    headers = ['Authorization: token %s' % (token)]
    if fetch_https_binary_file(url, tmp_file, True, headers):
        return tmp_file

    return None


def ubuntu_is_pkg_installed(package):
    '''Ubuntu - checks if package is installed'''

    cmd = [
        'dpkg-query',
        '-W',
        "--showformat='${Status}\n'",
        package,
        '|grep "install ok installed"']

    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True).stdout.strip('\'').strip('\n')

    if isinstance(result, list):
        test = result[0]
    else:
        test = result

    if test == "install ok installed":
        return True
    elif test == "unknown ok not-installed":
        return False


def ubuntu_install_pkg_if_not_installed(package):
    '''Ubuntu - Installs package if not already installed'''
    if not ubuntu_is_pkg_installed(package):

        print("\n* Installing runtime package dependency: %s" % package)

        cmd = ["sudo", "apt", "update", "-y"]
        subprocess.check_output(cmd)

        cmd = ["sudo", "apt-get", "install", "-y", package]
        subprocess.call(cmd)


def install_minimum_runtime_deps():
    '''Install minimum runtime deps to run this script'''
    host_type = get_host_type()

    if host_type == "linux":

        os_release = get_freedesktop_os_release()

        if os_release.get('NAME') == 'Ubuntu':

            ubuntu_install_pkg_if_not_installed("curl")
            ubuntu_install_pkg_if_not_installed("libcurl4-openssl-dev")
            ubuntu_install_pkg_if_not_installed("libssl-dev")


def install_agl_emu_image(folder, config, platform):

    host_type = get_host_type()

    if host_type == "linux":

        print_banner("Installing AGL emulator image")

        runtime = platform['runtime']

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
            config = {
                "view": {
                    "window_type": config_window_type,
                    "width": config_width,
                    "height": config_height,
                    "fullscreen": config_fullscreen
                }
            }
            json.dump(config, default_config_file, indent=2)

        if runtime.get('install_dependent_packages'):

            os_release = get_freedesktop_os_release()
            username = os.environ.get('USER')

            if os_release.get('NAME') == 'Ubuntu':

                cmd = [
                    "sudo",
                    "apt-get",
                    "install",
                    "-y",
                    "qemu-system-x86",
                    "ovmf",
                    "qemu-kvm",
                    "libvirt-daemon-system",
                    "libvirt-clients",
                    "bridge-utils"]
                subprocess.call(cmd)

                cmd = ["sudo", "adduser", username, "libvirt"]
                subprocess.call(cmd)

                cmd = ["sudo", "adduser", username, "kvm"]
                subprocess.call(cmd)

                cmd = [
                    "sudo",
                    "systemctl",
                    "status",
                    "libvirtd",
                    "--no-pager",
                    "-l"]
                subprocess.call(cmd)

        if runtime.get('artifact_source') == "github":
            github_artifact = runtime['github_artifact']
            if '${FLUTTER_RUNTIME}' in github_artifact:
                github_artifact = github_artifact.replace(
                    '${FLUTTER_RUNTIME}',
                    platform.get('flutter_runtime')
                )

            install_github_artifact_agl_emu_image(
                get_github_token(
                    config.get('github_token')
                ),
                runtime.get('github_owner'),
                runtime.get('github_repo'),
                runtime.get('github_workflow'),
                github_artifact
            )


def install_github_artifact_agl_emu_image(
    token,
    owner,
    repo,
    workflow,
    artifact_name
):
    '''Installs AGL emulation image github artifact'''

    if (token and owner and repo and workflow and artifact_name):

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

                print(
                    "Downloading %s run_id: %s via %s" %
                    (workflow, run_id, url))

                filename = "%s.zip" % name
                downloaded_file = get_github_artifact(
                    token, url, filename)
                if downloaded_file is None:
                    print_banner("Failed to download %s" % (filename))
                    break

                print("Downloaded: %s" % downloaded_file)

                workspace = os.getenv('FLUTTER_WORKSPACE')

                image_path = os.path.join(workspace, '.agl')
                with zipfile.ZipFile(downloaded_file, "r") as zip_ref:
                    zip_ref.extractall(image_path)

                cmd = ["rm", downloaded_file]
                subprocess.check_output(cmd)
                break


def install_flutter_auto(folder, config, platform):

    host_type = get_host_type()

    if host_type == "linux":

        print_banner("Installing flutter-auto")

        runtime = platform['runtime']

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
            config = {
                "cursor_theme": config_cursor_theme,
                "view": {
                    "width": config_width,
                    "height": config_height
                }
            }
            json.dump(config, default_config_file, indent=2)

        if runtime.get('install_dependent_packages'):

            os_release = get_freedesktop_os_release()
            if os_release.get('NAME') == 'Ubuntu':

                cmd = ["sudo", "snap", "install", "cmake", "--classic"]
                subprocess.call(cmd)

                cmd = [
                    "sudo",
                    "add-apt-repository",
                    "-y",
                    "ppa:kisak/kisak-mesa"]
                subprocess.call(cmd)

                cmd = ["sudo", "apt", "update", "-y"]
                subprocess.call(cmd)

                cmd = [
                    "sudo",
                    "apt-get",
                    "-y",
                    "install",
                    "libwayland-dev",
                    "wayland-protocols",
                    "mesa-common-dev",
                    "libegl1-mesa-dev",
                    "libgles2-mesa-dev",
                    "mesa-utils",
                    "clang-12",
                    "lldb-12",
                    "lld-12",
                    "libc++-12-dev",
                    "libc++abi-12-dev",
                    "libunwind-dev",
                    "libxkbcommon-dev",
                    "vulkan-tools",
                    "libgstreamer1.0-dev",
                    "libgstreamer-plugins-base1.0-dev",
                    "gstreamer1.0-plugins-base",
                    "gstreamer1.0-gl",
                    "libavformat-dev"]
                subprocess.call(cmd)

                print("** CMake Version")
                cmd = ["cmake", "--version"]
                subprocess.call(cmd)

                print("** Clang Version")
                cmd = ["/usr/lib/llvm-12/bin/clang++", "--version"]
                subprocess.call(cmd)

        if 'github' == runtime.get('artifact_source'):

            github_artifact = runtime.get('github_artifact')
            if '${BACKEND}' in github_artifact:
                backend = runtime.get('backend')
                github_artifact = github_artifact.replace('${BACKEND}', backend)

            install_flutter_auto_github_artifact(
                get_github_token(config.get('github_token')),
                runtime.get('github_owner'),
                runtime.get('github_repo'),
                runtime.get('github_workflow'),
                github_artifact)


def install_flutter_auto_github_artifact(
    token,
    owner,
    repo,
    workflow,
    github_artifact
):
    '''Installs flutter-auto github artifact'''

    if (token and owner and repo and workflow and github_artifact):

        workflow_runs = get_github_workflow_runs(
            token, owner, repo, workflow)
        run_id = None
        for run in workflow_runs:
            if "success" == run['conclusion']:
                run_id = run['id']
                break

        artifacts = get_github_workflow_artifacts(
            token, owner, repo, run_id)

        for artifact in artifacts:

            if github_artifact == artifact.get('name'):

                url = artifact.get('archive_download_url')

                print(
                    "** Downloading %s run_id: %s via %s" %
                    (workflow, run_id, url))

                downloaded_file = get_github_artifact(
                    token, url, github_artifact)
                print("** Downloaded: %s" % downloaded_file)

                with zipfile.ZipFile(downloaded_file, "r") as zip_ref:
                    filelist = zip_ref.namelist()
                    zip_ref.extractall()

                cmd = ["rm", downloaded_file]
                subprocess.check_output(cmd)

                dbgsym_file = None
                for f in filelist:
                    if ".deb" in f:
                        deb_file = f
                        break
                    elif ".ddeb" in f:
                        dbgsym_file = f

                cmd = ["sudo", "apt", "purge", "-y", "flutter-auto"]
                subprocess.call(cmd)

                cmd = ["sudo", "apt", "purge", "-y", "flutter-auto-dbg"]
                subprocess.call(cmd)

                cmd = ["sudo", "apt", "install", "-y", "./%s" % deb_file]
                subprocess.call(cmd)

                if ".ddeb" in dbgsym_file:
                    cmd = ["rm", dbgsym_file]
                    subprocess.check_output(cmd)

                cmd = ["rm", deb_file]
                subprocess.check_output(cmd)
                break


def is_repo(path):
    return os.path.exists(os.path.join(path, ".git"))


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
    else
        echo 'QEMU_IMAGE is set to ${QEMU_IMAGE}'
    fi
    export OVMF_PATH=%s
    echo \"OVMF_PATH is set to '$OVMF_PATH'\"
    if pgrep -x \"%s\" > /dev/null
    then
        echo '%s running - do nothing'
    else
        gnome-terminal -- bash -c \"%s %s\"
    fi
}
'''


def setup_env_script(workspace, args, platform):
    '''Creates bash script to setup environment variables'''

    environment_script = os.path.join(workspace, 'setup_env.sh')

    with open(environment_script, 'w+') as script:
        script.write(env_prefix)
        for item in platform:
            if 'type' in item:

                if "qemu" == item['type']:

                    runtime = item['runtime']

                    relative_path = runtime['relative_path']
                    if '${FLUTTER_RUNTIME}' in relative_path:
                        relative_path = relative_path.replace(
                            '${FLUTTER_RUNTIME}',
                            item.get('flutter_runtime')
                        )

                    script.write(env_qemu % (
                        relative_path,
                        runtime['ovmf_path'],
                        runtime['cmd'],
                        runtime['cmd'],
                        runtime['cmd'],
                        runtime['args']
                    ))


def get_platform_ids(platforms):
    '''returns list of platform ids'''
    res = []
    for platform in platforms:
        res.append(platform.get('id'))
    return res


def get_launch_obj(repo, device_id):
    '''returns dictionary of launch target'''
    uri = repo.get('uri')
    repo_name = uri.rsplit('/', 1)[-1]
    repo_name = repo_name.split(".")
    repo_name = repo_name[0]

    pubspec_path = repo.get('pubspec_path')
    if pubspec_path is not None:
        pubspec_path = os.path.join('app', pubspec_path)
        return {
            "name": "%s (%s)" % (repo_name, device_id),
            "cwd": pubspec_path,
            "request": "launch",
            "type": "dart",
            "deviceId": device_id
        }
    else:
        return {}


def create_vscode_launch_file(repos, device_ids):
    '''Creates a default vscode launch.json'''

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
