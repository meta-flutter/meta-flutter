#!/usr/bin/env python3

# 
# Script that creates a Flutter Workspace
#
# Creates flutter workspace:
#
#   .config
#   .pub_cache
#   app
#   flutter
#   setup_env.sh
#
#
# One runs this script to create the workspace, then from working terminal setup the environment:
#
# "source ./setup_env.sh" or ". ./setup_env.sh"
#
# if emulator image is present type `qemu_run' to start the QEMU image
#
# if "github_token": "<XXXXXXXX>", is present in config.json (after flutter-version key)
# - it will download and install:
#   flutter-auto latest CI run
#   AGL QEMU CI run (meta-flutter)
#


import errno
import json
import os
import platform
import pkg_resources
import subprocess
import sys
import zipfile


def print_banner(text):
    print('*' * (len(text) + 6))
    print("** %s **" % text)
    print('*' * (len(text) + 6))


def main():

    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--clean', default=False, action='store_true',
                        help='Wipes workspace clean')
    parser.add_argument('--workspace-cfg', default='', type=str,
                        help='Selects custom workspace configuration file')
    parser.add_argument('--flutter-version', default='', type=str,
                        help='Select flutter version.  Overrides config file key: flutter-version')
    parser.add_argument('--target-user', default='', type=str,
                        help='Sets custom-device target user name')
    parser.add_argument('--target-address', default='', type=str,
                        help='Sets custom-device target address')
    args = parser.parse_args()


    #
    # Install required modules
    #
    required = {'requests'}

    installed = {pkg.key for pkg in pkg_resources.working_set}
    missing = required - installed

    if missing:
        print("Installing required packages: %s" % required)
        python = sys.executable
        subprocess.check_call([python, '-m', 'pip', 'install', *missing], stdout=subprocess.DEVNULL)


    #
    # Create Workspace
    #
    if "FLUTTER_WORKSPACE" in os.environ:
        workspace = os.environ.get('FLUTTER_WORKSPACE')
    else:
        workspace = os.getcwd()

    print_banner("FLUTTER_WORKSPACE: %s" % workspace)

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
        print("\n** Custom Workspace Configuration file: %s\n" % workspace_configuration)
    else:
        print("\n** Default Workspace Configuration file: %s\n" % workspace_configuration)
 
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
    # Create default_config.json
    #
    setup_flutter_auto_files(flutter_auto_folder)

    #
    # Create script to setup environment
    #
    setup_env_script(args, platforms)

    #
    # Configure SDK
    #
    configure_flutter_sdk()


    #
    # Get runtime artifacts
    #
    get_artifacts(config, flutter_sdk_path, flutter_auto_folder)

    #
    # custom-devices
    #
    update_flutter_custom_devices_list(platforms)

    if flutter_version == "master":
        command = ['flutter', 'custom-devices', 'list']
        subprocess.check_call(command)

    print_banner("Setup Flutter Workspace - Complete")


def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def clear_folder(dir):
    ''' Clears folder specified '''
    import shutil
    if  os.path.exists(dir):
        shutil.rmtree(dir)


def get_workspace_config(config_file):
    ''' Returns workspace config '''

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
        print("Invalid JSON in %s" % (config_file)) # in case json is invalid
        exit(1)

    f.close()

    return data


def validate_platform_config(platform):
    ''' Validates Platform Configuration returning bool '''

    if not 'id' in platform:
        print_banner("Missing 'id' key in platform config")
        return False
    if not 'type' in platform:
        print_banner("Missing 'type' key in platform config")
        return False
    else:
        if platform['type'] == 'host':
            if not 'arch' in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'runtime' in platform:
                print_banner("Missing 'runtime' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'qemu':
            if not 'arch' in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'runtime' in platform:
                print_banner("Missing 'runtime' key in platform config")
                return False
            if not 'custom-device' in platform:
                print_banner("Missing 'custom-device' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'target':
            if not 'arch' in platform:
                print_banner("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print_banner("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'target_user' in platform:
                print_banner("Missing 'target_user' key in platform config")
                return False
            if not 'target_address' in platform:
                print_banner("Missing 'target_address' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        else:
            print("platform type %s is not currently supported." % (platform['type']))

    return True


def validate_custom_device_config(config):
    ''' Validates custom-device Configuration returning bool '''

    if not 'id' in config:
        print_banner("Missing 'id' key in custom-device config")
        return False
    if not 'label' in config:
        print_banner("Missing 'label' key in custom-device config")
        return False
    if not 'sdkNameAndVersion' in config:
        print_banner("Missing 'sdkNameAndVersion' key in custom-device config")
        return False
    if not 'platform' in config:
        print_banner("Missing 'platform' key in custom-device config")
        return False
    if not 'enabled' in config:
        print_banner("Missing 'enabled' key in custom-device config")
        return False
    if not 'ping' in config:
        print_banner("Missing 'ping' key in custom-device config")
        return False
    if not 'pingSuccessRegex' in config:
        print_banner("Missing 'pingSuccessRegex' key in custom-device config")
        return False
    if not 'postBuild' in config:
        print_banner("Missing 'postBuild' key in custom-device config")
        return False
    if not 'install' in config:
        print_banner("Missing 'install' key in custom-device config")
        return False
    if not 'uninstall' in config:
        print_banner("Missing 'uninstall' key in custom-device config")
        return False
    if not 'runDebug' in config:
        print_banner("Missing 'runDebug' key in custom-device config")
        return False
    if not 'forwardPort' in config:
        print_banner("Missing 'forwardPort' key in custom-device config")
        return False
    if not 'forwardPortSuccessRegex' in config:
        print_banner("Missing 'forwardPortSuccessRegex' key in custom-device config")
        return False
    if not 'screenshot' in config:
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
        if not 'uri' in repo:
            print("repo entry needs a 'uri' key.  Skipping")
            continue
        if not 'branch' in repo:
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

            cmd = ['git', 'clone', repo['uri'], '-b', repo['branch'], repo_name]
            subprocess.check_call(cmd, cwd=base_folder)

        if 'rev' in repo:
            cmd = ['git', 'reset', '--hard', repo['rev']]
            subprocess.check_call(cmd, cwd=git_folder)

        cmd = ['git', 'log', '-1']
        subprocess.check_call(cmd, cwd=git_folder)


def get_flutter_settings_folder():
    ''' Returns the path of the Custom Config json file '''

    if "XDG_CONFIG_HOME" in os.environ:
        settings_folder = os.path.join(os.environ.get('XDG_CONFIG_HOME'))
    else:
        settings_folder = os.path.join(os.environ.get('HOME'), '.config', 'flutter')

    make_sure_path_exists(settings_folder)

    return settings_folder


def get_flutter_custom_config_path():
    ''' Returns the path of the Flutter Custom Config json file '''

    fldr_ = get_flutter_settings_folder()
    #print("folder: %s" % fldr_)
    return os.path.join(fldr_, 'custom_devices.json')


def get_flutter_custom_devices():
    ''' Returns the Flutter custom_devices.json as dict '''

    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        f = open(custom_config)
        try:
            data = json.load(f)
        except json.decoder.JSONDecodeError:
            print("Invalid JSON in %s" % (custom_config)) # in case json is invalid
            exit(1)
        f.close()

        if 'custom-devices' in data:
            return data['custom-devices']

    print("%s not present in filesystem." % custom_config)

    return {}


def remove_flutter_custom_devices_id(id):
    ''' Removes Flutter custom devices that match given id from the configuration file '''

    # print("Removing custom-device with ID: %s" % id)
    custom_config = get_flutter_custom_config_path()
    if os.path.exists(custom_config):

        f = open(custom_config, "r")
        try:
            obj = json.load(f)
        except json.decoder.JSONDecodeError:
            print_banner("Invalid JSON in %s" % (custom_config)) # in case json is invalid
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
        
        if not 'custom-devices' in custom_devices:
            print("Removing empty file: %s" % custom_config)
            os.remove(custom_config)
            return

        with open(custom_config, "w") as outfile:
            json.dump(custom_devices, outfile, indent=2)

    return


def patch_string_array(find_token, replace_token, list):
    return [w.replace(find_token, replace_token) for w in list]


def patch_custom_device_strings(devices):
    ''' Patch custom device string environmental variables to use literal values '''

    workspace = os.getenv('FLUTTER_WORKSPACE')
    bundle_folder = os.getenv('BUNDLE_FOLDER')
    target_user = os.getenv('TARGET_USER')
    target_address = os.getenv('TARGET_ADDRESS')

    for device in devices:

        token = '${FLUTTER_WORKSPACE}'

        if device.get('postBuild'):
            device['postBuild'] = patch_string_array(token, workspace, device['postBuild'])

        if device.get('runDebug'):
            device['runDebug'] = patch_string_array(token, workspace, device['runDebug'])


        token = '${BUNDLE_FOLDER}'
        if device.get('install'):
            device['install'] = patch_string_array(token, bundle_folder, device['install'])

        if target_user:
            token = '${TARGET_USER}'
            if device.get('install'):
                device['install'] = patch_string_array(token, target_user, device['install'])
            if device.get('uninstall'):
                device['uninstall'] = patch_string_array(token, target_user, device['uninstall'])
            if device.get('runDebug'):
                device['runDebug'] = patch_string_array(token, target_user, device['runDebug'])
            if device.get('forwardPort'):
                device['forwardPort'] = patch_string_array(token, target_user, device['forwardPort'])

        if target_address:
            token = '${TARGET_ADDRESS}'
            if device.get('ping'):
                device['ping'] = patch_string_array(token, target_address, device['ping'])
            if device.get('install'):
                device['install'] = patch_string_array(token, target_address, device['install'])
            if device.get('uninstall'):
                device['uninstall'] = patch_string_array(token, target_address, device['uninstall'])
            if device.get('runDebug'):
                device['runDebug'] = patch_string_array(token, target_address, device['runDebug'])
            if device.get('forwardPort'):
                device['forwardPort'] = patch_string_array(token, target_address, device['forwardPort'])

    return devices


def add_flutter_custom_device(device_config):
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
            print_banner("Invalid JSON in %s" % (custom_devices_file)) # in case json is invalid
            exit(1)
        f.close()

        id = device_config['id']

        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id != device['id']:
                    new_device_list.append(device)

    new_device_list.append(device_config)
    patched_device_list = patch_custom_device_strings(new_device_list)

    custom_devices = {}
    custom_devices['custom-devices'] = patched_device_list

    #print("custom_devices_file: %s" % custom_devices_file)
    with open(custom_devices_file, "w+") as outfile:
        json.dump(custom_devices, outfile, indent=4)

    return


def update_flutter_custom_devices_list(platforms):
    ''' Updates the custom_devices.json with all custom-devices in platforms dict '''

    for platform in platforms:

        custom_devices = get_flutter_custom_devices()

        overwrite_existing = platform.get('overwrite-existing')

        # check if id already exists, remove if overwrite enabled, otherwise skip
        if custom_devices:
            for custom_device in custom_devices:
                if 'id' in custom_device:
                    id = custom_device['id']
                    if overwrite_existing and (id == platform['id']):
                        #print("attempting to remove custom-device: %s" % id)
                        remove_flutter_custom_devices_id(id)

        add_flutter_custom_device(platform['custom-device'])


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

    tool_script = os.path.join(flutter_sdk_folder, 'bin', 'cache', 'flutter_tools.snapshot')

    if os.path.exists(tool_script):

        print_banner("Cleaning Flutter Tool")

        cmd = ["rm", tool_script]
        subprocess.check_call(cmd, cwd=flutter_sdk_folder)


def patch_flutter_sdk(flutter_sdk_folder):

    host = get_host_type()

    if host == "linux":

        print_banner("Patching Flutter SDK")

        cmd = ["bash", "-c", "sed -i -e \"/const Feature flutterCustomDevicesFeature/a const Feature flutterCustomDevicesFeature = Feature\\(\\n  name: \\\'Early support for custom device types\\\',\\n  configSetting: \\\'enable-custom-devices\\\',\\n  environmentOverride: \\\'FLUTTER_CUSTOM_DEVICES\\\',\\n  master: FeatureChannelSetting(\\n    available: true,\\n  \\),\\n  beta: FeatureChannelSettin\\g(\\n    available: true,\\n  \\),\\n  stable: FeatureChannelSetting(\\n    available: true,\\n  \\)\\n);\" -e \"/const Feature flutterCustomDevicesFeature/,/);/d\" packages/flutter_tools/lib/src/features.dart"]
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

        cmd = ["git","clone", flutter_repo, flutter_sdk_path]
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
    ''' Read /etc/os-release into dictionary '''

    with open("/etc/os-release") as f:
        d = {}
        for line in f:
            k,v = line.rstrip().split("=")
            d[k] = v.strip('"')
        return d


def get_host_type():
    return platform.system().lower().rstrip()


def get_artifacts(config, flutter_sdk_path, flutter_auto_folder):
    ''' Get x86_64 Engine artifcats '''
    import requests

    tmp_folder = get_workspace_tmp_folder()
    make_sure_path_exists(tmp_folder)

    fetch_linux_x64_engine = False

    platforms = config['platforms']

    for platform in platforms:

        arch = platform['arch']

        if arch == "x86_64":

            fetch_linux_x64_engine = True

            if platform['id'] == 'AGL-qemu' and platform['type'] == 'qemu':
                install_agl_emu_image(config, platform)

            elif platform['id'] == 'desktop-auto' and platform['type'] == 'host':
                install_flutter_auto(config, platform)

        else:
            print("%s artifacts not yet supported, skipping" % arch)

    if fetch_linux_x64_engine:

        print_banner("Fetching linux-64 Flutter Engine")

        engine_version = get_flutter_engine_version(flutter_sdk_path)
        url = 'https://storage.googleapis.com/flutter_infra_release/flutter/%s/linux-x64/linux-x64-embedder' % engine_version
        flutter_engine_zip = "%s/embedder.zip" % tmp_folder

        print("Attempting to download %s\n...to %s" % (url, flutter_engine_zip))
        try:
            r = requests.get(url)
            open(flutter_engine_zip, 'wb').write(r.content)
        except requests.exceptions.HTTPError as e:
            if e.errno == 404:
                print("Artifact not available.")
                return

        bundle_folder = os.path.join(flutter_auto_folder, engine_version[0:7], 'linux-x64', platform['flutter_runtime'], 'bundle')
        os.environ['BUNDLE_FOLDER'] = bundle_folder
        
        lib_folder = os.path.join(bundle_folder, 'lib')
        make_sure_path_exists(lib_folder)

        data_folder = os.path.join(bundle_folder, 'data')
        make_sure_path_exists(data_folder)

        icudtl_source = os.path.join(flutter_sdk_path, "bin/cache/artifacts/engine/linux-x64/icudtl.dat")
        if not os.path.exists(icudtl_source):
            cmd = ["flutter", "doctor", "-v"]
            subprocess.check_call(cmd, cwd=flutter_sdk_path)

        host_type = get_host_type()
        icudtl_source = os.path.join(flutter_sdk_path, "bin/cache/artifacts/engine/%s-x64/icudtl.dat" % host_type)
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

        headers = {"Accept": "application/vnd.github+json", "Authorization": "token %s" % (token)}
        with requests.get(url, stream=True, headers=headers, allow_redirects=True) as r:
            data = r.json()

        return data['artifacts']


def get_github_workflow_runs(token, owner, repo, workflow):
    ''' Gets workflow run list '''
    import requests

    url = "https://api.github.com/repos/%s/%s/actions/workflows/%s/runs" % (owner, repo, workflow)

    try:
        headers = {"Accept": "application/vnd.github+json", "Authorization": "token %s" % (token)}
        with requests.get(url, stream=True, headers=headers, allow_redirects=True) as r:
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

    url = "https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts" % (owner, repo, id)

    try:
        headers = {"Accept": "application/vnd.github+json", "Authorization": "token %s" % (token)}
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
    tmp_folder = os.path.join(workspace,'.tmp')
    make_sure_path_exists(tmp_folder)
    return tmp_folder


def get_github_artifact(token, url, filename):
    ''' Gets artifact via Github URL'''
    import requests

    try:
        headers = {'Authorization': 'token %s' % (token)}
        with requests.get(url, headers=headers, allow_redirects=True) as r:

            tmp_file = "%s/%s" % (get_workspace_tmp_folder(), filename)

            open(tmp_file, 'wb').write(r.content)

            return tmp_file

    except requests.exceptions.HTTPError as e:
        if e.errno == 404:
            print("Artifact not available.")
            return None

    return None


def install_agl_emu_image(config, platform):

    host_type = get_host_type()

    if host_type == "linux":

        print_banner("Installing AGL emulator image")

        runtime = platform['runtime']
        
        relative_path = runtime.get('relative_path') 
        github_owner = runtime.get('github_owner')
        github_repo = runtime.get('github_repo')
        github_workflow = runtime.get('github_workflow')
        github_artifact = runtime.get('github_artifact')

        if runtime.get('install_dependent_packages'):

            os_release = get_freedesktop_os_release()
            username = os.environ.get('USER')

            if os_release.get('NAME') == 'Ubuntu':

                cmd = ["sudo", "apt", "update", "-y"]
                subprocess.check_output(cmd)

                cmd = ["sudo", "apt-get", "install", "-y", "qemu-system-x86", "ovmf", "qemu-kvm", "libvirt-daemon-system", "libvirt-clients", "bridge-utils"]
                subprocess.call(cmd)

                cmd = ["sudo", "adduser", username, "libvirt"]
                subprocess.call(cmd)

                cmd = ["sudo", "adduser", username, "kvm"]
                subprocess.call(cmd)

                cmd = ["sudo", "systemctl", "status", "libvirtd", "--no-pager", "-l"]
                subprocess.call(cmd)

        github_token = get_github_token(config.get('github_token'))

        if github_token and relative_path and github_owner and github_repo and github_workflow and github_artifact:

            workflow_runs = get_github_workflow_runs(github_token, github_owner, github_repo, github_workflow)
            run_id = None
            for run in workflow_runs:
                if run['conclusion'] == "success":
                    run_id = run['id']
                    break
        
            artifacts = get_github_workflow_artifacts(github_token, github_owner, github_repo, run_id)

            for artifact in artifacts:
                
                name = artifact.get('name')

                if name == "agl-image-flutter-runtimedebug-qemux86-64-linux":

                    url = artifact.get('archive_download_url')

                    print("Downloading %s run_id: %s via %s" % (github_workflow, run_id, url))

                    downloaded_file = get_github_artifact(github_token, url, "%s.zip" % name)
                    print("Downloaded: %s" % downloaded_file)

                    workspace = os.getenv('FLUTTER_WORKSPACE')

                    image_path = os.path.join(workspace, '.agl')
                    with zipfile.ZipFile(downloaded_file, "r") as zip_ref:
                        zip_ref.extractall(image_path)

                    break

            cmd = ["rm", downloaded_file]
            subprocess.check_output(cmd)


def install_flutter_auto(config, platform):

    host_type = get_host_type()

    if host_type == "linux":

        print_banner("Installing flutter-auto")

        runtime = platform['runtime']
        github_owner = runtime.get('github_owner')
        github_repo = runtime.get('github_repo')
        github_workflow = runtime.get('github_workflow')
        artifact_name = runtime.get('artifact_name')

        if runtime.get('install_dependent_packages'):

            os_release = get_freedesktop_os_release()
            if os_release.get('NAME') == 'Ubuntu':

                cmd = ["sudo", "snap", "install", "cmake", "--classic"]
                subprocess.call(cmd)

                cmd = ["sudo", "add-apt-repository", "-y", "ppa:kisak/kisak-mesa"]
                subprocess.call(cmd)

                cmd = ["sudo", "apt", "update", "-y"]
                subprocess.call(cmd)

                cmd = ["sudo", "apt-get", "-y", "install", 
                    "libwayland-dev", "wayland-protocols", "mesa-common-dev",
                    "libegl1-mesa-dev", "libgles2-mesa-dev", "mesa-utils",
                    "clang-12", "lldb-12", "lld-12",
                    "libc++-12-dev", "libc++abi-12-dev", "libunwind-dev",
                    "libxkbcommon-dev", "vulkan-tools",
                    "libgstreamer1.0-dev", "libgstreamer-plugins-base1.0-dev",
                    "gstreamer1.0-plugins-base", "gstreamer1.0-gl", "libavformat-dev"]
                subprocess.call(cmd)

                cmd = ["cmake", "--version"]
                subprocess.call(cmd)

                cmd = ["/usr/lib/llvm-12/bin/clang++", "--version"]
                subprocess.call(cmd)

        github_token = get_github_token(config.get('github_token'))

        if github_token and github_owner and github_repo and github_workflow and artifact_name:

            workflow_runs = get_github_workflow_runs(github_token, github_owner, github_repo, github_workflow)
            run_id = None
            for run in workflow_runs:
                if "success" == run['conclusion']:
                    run_id = run['id']
                    break

            artifacts = get_github_workflow_artifacts(github_token, github_owner, github_repo, run_id)

            for artifact in artifacts:
                
                if artifact_name == artifact.get('name'):

                    url = artifact.get('archive_download_url')

                    print("Downloading %s run_id: %s via %s" % (github_workflow, run_id, url))

                    downloaded_file = get_github_artifact(github_token, url, artifact_name)
                    print("Downloaded: %s" % downloaded_file)

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


def run_flutter_doctor():
    ''' Runs flutter doctor '''

    process = subprocess.Popen("flutter doctor -v", shell=True, stdout=subprocess.PIPE, universal_newlines=True)
    ret = ""
    print("Running flutter doctor -v")
    for line in process.stdout:
        print(line)
        ret += str(line)
    process.wait()
    return ret


def get_flutter_dir(fldoctor_ret):
    search_start = "at"
    search_end = "Upstream repository"
    return fldoctor_ret[fldoctor_ret.find(search_start) + len(search_start):fldoctor_ret.find(search_end) - 5].strip()


def is_repo(path):
    return os.path.exists(os.path.join(path, ".git"))


def setup_flutter_auto_files(folder):
    ''' Creates default files under $FLUTTER_WORKSPACE/.flutter-auto '''

    make_sure_path_exists(folder)

    default_config_filepath = os.path.join(folder,'default_config.json')
    with open(default_config_filepath, 'w+') as default_config_file:
        config = {
            "cursor_theme":"DMZ-White",
            "view":{
                "width":1920,
                "height":720
            }
        }
        json.dump(config, default_config_file, indent=2)


def setup_env_script(args, platform):
    '''Creates bash script to setup environment variables'''

    with open('setup_env.sh', 'w+') as script:

        script.writelines([
    
            "#!/usr/bin/env bash -l\n",
            "\n",
            "pushd . > '/dev/null';\n",
            "SCRIPT_PATH=\"${BASH_SOURCE[0]:-$0}\";\n",
            "\n",
            "while [ -h \"$SCRIPT_PATH\" ];\n",
            "do\n",
            "    cd \"$( dirname -- \"$SCRIPT_PATH\"; )\";\n",
            "    SCRIPT_PATH=\"$( readlink -f -- \"$SCRIPT_PATH\"; )\";\n",
            "done\n",
            "\n",
            "cd \"$( dirname -- \"$SCRIPT_PATH\"; )\" > '/dev/null';\n",
            "SCRIPT_PATH=\"$( pwd; )\";\n",
            "popd  > '/dev/null';\n",
            "\n",
            "echo SCRIPT_PATH=$SCRIPT_PATH\n",
            "\n",
            "export FLUTTER_WORKSPACE=$SCRIPT_PATH;\n",
            "export PATH=$FLUTTER_WORKSPACE/flutter/bin:$PATH;\n",
            "export PUB_CACHE=$FLUTTER_WORKSPACE/.pub_cache;\n",
            "export XDG_CONFIG_HOME=$FLUTTER_WORKSPACE/.config/flutter;\n\n"
            "echo \"********************************************\"\n",
            "echo \"* Setting FLUTTER_WORKSPACE to:\"\n",
            "echo \"* ${FLUTTER_WORKSPACE}\"\n",
            "echo \"********************************************\"\n",
            "flutter doctor -v\n",
            "flutter custom-devices list\n",
            "\n"
        ])

        for platform in platform:
            if 'type' in platform:                    

                if "target" == platform['type']:

                    script.writelines(["\n"])

                    if args.target_user:
                        target_user = args.target_user
                    else:
                        target_user = platform['target_user']

                    os.environ['TARGET_USER'] = target_user

                    if args.target_address:
                        target_address = args.target_address
                    else:
                        target_address = platform['target_address']

                    os.environ['TARGET_ADDRESS'] = target_address

                elif "qemu" == platform['type']:

                    runtime = platform['runtime']

                    script.writelines([
                        "echo \"********************************************\"\n",
                        "echo \" Type 'qemu_run' to start the emulator\"\n",
                        "echo \"********************************************\"\n",
                        "\n",
                        "qemu_run() {\n"
                        "\n",
                        "    if [ -z ${QEMU_IMAGE+x} ];\n",
                        "    then\n",
                        "        export QEMU_IMAGE=${FLUTTER_WORKSPACE}/%s;\n" % (runtime['relative_path']),
                        "    else\n",
                        "        echo \"QEMU_IMAGE is set to '$QEMU_IMAGE'\";\n",
                        "    fi\n",
                        "\n",
                        "    export OVMF_PATH=%s;\n" % runtime['ovmf_path'],
                        "    echo \"OVMF_PATH is set to '$OVMF_PATH'\";\n",
                        "\n",
                        "    if pgrep -x \"%s\" > /dev/null\n" % runtime['cmd'],
                        "    then\n",
                        "        echo \"%s running - do nothing\"\n" % runtime['cmd'],
                        "    else\n",
                        "        gnome-terminal -- bash -c \"%s %s\"\n" % (runtime['cmd'], runtime['args']),
                        "    fi\n"
                        "}\n"
                    ])



if __name__ == "__main__":
    main()
