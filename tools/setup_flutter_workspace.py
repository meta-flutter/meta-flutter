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

import errno
import json
import os
import subprocess
import urllib
import urllib.error
import urllib.request
import zipfile


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
    # Create Workspace
    #
    if "FLUTTER_WORKSPACE" in os.environ:
        workspace = os.environ.get('FLUTTER_WORKSPACE')
    else:
        workspace = os.getcwd()

    print("FLUTTER_WORKSPACE: %s" % workspace)

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
        print("\n** Custom Workspace Configuration file: %s **\n" % workspace_configuration)
    else:
        print("\n** Default Workspace Configuration file: %s **\n" % workspace_configuration)
 
    config = get_workspace_config(workspace_configuration)

    platforms = get_workspace_platforms(config)
    for platform in platforms:
        if not validate_platform_config(platform):
            print("Invalid platform configuration")
            exit(1)

    app_folder = os.path.join(workspace, 'app')
    flutter_sdk_folder = os.path.join(workspace, 'flutter')

    agl_folder = os.path.join(workspace, '.agl')
    config_folder = os.path.join(workspace, '.config')
    flutter_auto_folder = os.path.join(workspace, '.flutter-auto')
    pub_cache_folder = os.path.join(workspace, '.pub_cache')

    clean_workspace = False
    if args.clean:
        clean_workspace = args.clean
        if clean_workspace:
            print("\n** Cleaning Workspace **\n")

    if clean_workspace:
        clear_folder(app_folder)
        clear_folder(flutter_sdk_folder)

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

    print("\n** Flutter Version: %s **" % flutter_version)
    flutter_sdk_path = get_flutter_sdk(flutter_version)
    flutter_bin_path = os.path.join(flutter_sdk_path, 'bin')

    # Enable custom devices in dev and stable
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
        print("** Upgrading `%s` Channel ** " % flutter_version)
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
    get_artifacts(config['platforms'], flutter_sdk_path, flutter_auto_folder)

    #
    # custom-devices
    #
    update_flutter_custom_devices_list(platforms)

    if flutter_version == "master":
        command = ['flutter', 'custom-devices', 'list']
        subprocess.check_call(command)


def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def clear_folder(dir):
    ''' Clears folder specified '''
    if os.path.exists(dir):
        for the_file in os.listdir(dir):
            file_path = os.path.join(dir, the_file)
            try:
                if os.path.isfile(file_path):
                    os.unlink(file_path)
                else:
                    clear_folder(file_path)
                    os.rmdir(file_path)
            except Exception as e:
                print(e)


def get_workspace_config(config_file):
    ''' Returns workspace config '''
    platform_config = False
    file_exists = os.path.exists(config_file)
    if not file_exists:
        print("Missing %s file" % config_file)
        return
    
    f = open(config_file)
    try:
        data = json.load(f)
    except json.decoder.JSONDecodeError:
        print("Invalid JSON in %s" % (config_file)) # in case json is invalid
        exit(1)

    f.close()

    return data


def get_workspace_platforms(data):
    ''' Returns platforms from the config dictionary '''
    if 'platforms' in data:
        return data['platforms']
    return None


def validate_platform_config(platform):
    ''' Validates Platform Configuration returning bool '''

    if not 'id' in platform:
        print("Missing 'id' key in platform config")
        return False
    if not 'type' in platform:
        print("Missing 'type' key in platform config")
        return False
    else:
        if platform['type'] == 'host':
            if not 'arch' in platform:
                print("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'enforce_runtime_packages' in platform:
                print("Missing 'enforce_runtime_packages' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'qemu':
            if not 'arch' in platform:
                print("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'image' in platform:
                print("Missing 'image' key in platform config")
                return False
            if not os.path.exists(platform['image']):
                print("WARNING: File Missing: %s" % platform['image'])
            if not 'launch_app' in platform:
                print("Missing 'launch_app' key in platform config")
                return False
            if not 'launch_args' in platform:
                print("Missing 'launch_args' key in platform config")
                return False
            if not 'ovmf_path' in platform:
                print("Missing 'ovmf_path' key in platform config")
                return False
            if not 'custom-device' in platform:
                print("Missing 'custom-device' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        elif platform['type'] == 'target':
            if not 'arch' in platform:
                print("Missing 'arch' key in platform config")
                return False
            if not 'flutter_runtime' in platform:
                print("Missing 'flutter_runtime' key in platform config")
                return False
            if not 'target_user' in platform:
                print("Missing 'target_user' key in platform config")
                return False
            if not 'target_address' in platform:
                print("Missing 'target_address' key in platform config")
                return False

            print("Platform ID: %s" % (platform['id']))

        else:
            print("platform type %s is not currently supported." % (platform['type']))

    return True


def validate_custom_device_config(config):
    ''' Validates custom-device Configuration returning bool '''

    if not 'id' in config:
        print("Missing 'id' key in custom-device config")
        return False
    if not 'label' in config:
        print("Missing 'label' key in custom-device config")
        return False
    if not 'sdkNameAndVersion' in config:
        print("Missing 'sdkNameAndVersion' key in custom-device config")
        return False
    if not 'platform' in config:
        print("Missing 'platform' key in custom-device config")
        return False
    if not 'enabled' in config:
        print("Missing 'enabled' key in custom-device config")
        return False
    if not 'ping' in config:
        print("Missing 'ping' key in custom-device config")
        return False
    if not 'pingSuccessRegex' in config:
        print("Missing 'pingSuccessRegex' key in custom-device config")
        return False
    if not 'postBuild' in config:
        print("Missing 'postBuild' key in custom-device config")
        return False
    if not 'install' in config:
        print("Missing 'install' key in custom-device config")
        return False
    if not 'uninstall' in config:
        print("Missing 'uninstall' key in custom-device config")
        return False
    if not 'runDebug' in config:
        print("Missing 'runDebug' key in custom-device config")
        return False
    if not 'forwardPort' in config:
        print("Missing 'forwardPort' key in custom-device config")
        return False
    if not 'forwardPortSuccessRegex' in config:
        print("Missing 'forwardPortSuccessRegex' key in custom-device config")
        return False
    if not 'screenshot' in config:
        print("Missing 'screenshot' key in custom-device config")
        return False

    return True


def get_workspace_repos(base_folder, config):
    ''' Clone GIT repos referenced in config repos dict to base_folder '''

    #print("base folder: %s" % base_folder)

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
        #print("repo name: %s" % repo_name)

        git_folder = os.path.join(base_folder, repo_name)
        #print("git folder: %s" % git_folder)

        git_folder_git = os.path.join(base_folder, repo_name, '.git')
        #print("git folder test: %s" % git_folder_git)

        isExist = os.path.exists(git_folder_git)
        if not isExist:

            isExist = os.path.exists(git_folder)
            if isExist:
                os.removedirs(git_folder)

            cmd = ['git', 'clone', repo['uri'], '-b', repo['branch'], repo_name]
            #print('%s' % (cmd))
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


def get_flutter_custom_devices_dict():
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
            print("Invalid JSON in %s" % (custom_config)) # in case json is invalid
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
            print("Invalid JSON in %s" % (custom_devices_file)) # in case json is invalid
            exit(1)
        f.close()

        id = device_config['id']

        if 'custom-devices' in obj:
            devices = obj['custom-devices']
            for device in devices:
                if 'id' in device and id != device['id']:
                    new_device_list.append(device)

    new_device_list.append(device_config)

    custom_devices = {}
    custom_devices['custom-devices'] = new_device_list

    #print("custom_devices_file: %s" % custom_devices_file)
    with open(custom_devices_file, "w+") as outfile:
        json.dump(custom_devices, outfile, indent=4)

    #command = ['flutter', 'custom-devices', 'list']
    #subprocess.check_call(command)

    return


def update_flutter_custom_devices_list(platforms):
    ''' Updates the custom_devices.json with all custom-devices in platforms dict '''

    for platform in platforms:
        overwrite_existing = False
        if 'overwrite-existing' in platform:
            overwrite_existing = platform['overwrite-existing']
            # print("Overwrite Existing Custom Devices enabled")
        
        custom_devices = get_flutter_custom_devices_dict()

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
        "enable-linux-desktop": False,
        "enable-macos-desktop": False,
        "enable-windows-desktop": False,
        "enable-android": False,
        "enable-ios": False,
        "enable-fuchsia": False,
        "enable-custom-devices": True
    }

    settings_file = os.path.join(get_flutter_settings_folder(), 'settings')

    with open(settings_file, "w+") as outfile:
        json.dump(settings, outfile, indent=2)

    command = ['flutter', 'config', '--no-analytics']
    subprocess.check_call(command)
    command = ['dart', '--disable-analytics']
    subprocess.check_call(command)
    command = ['flutter', 'doctor']
    subprocess.check_call(command)

def patch_flutter_sdk(flutter_sdk_folder):

    print("Patching Flutter SDK")
    cmd = ["bash", "-c", "sed -i -e \"/const Feature flutterCustomDevicesFeature/a const Feature flutterCustomDevicesFeature = Feature\\(\\n  name: \\\'Early support for custom device types\\\',\\n  configSetting: \\\'enable-custom-devices\\\',\\n  environmentOverride: \\\'FLUTTER_CUSTOM_DEVICES\\\',\\n  master: FeatureChannelSetting(\\n    available: true,\\n  \\),\\n  dev: FeatureChannelSettin\\g(\\n    available: true,\\n  \\),\\n  stable: FeatureChannelSetting(\\n    available: true,\\n  \\)\\n);\" -e \"/const Feature flutterCustomDevicesFeature/,/);/d\" packages/flutter_tools/lib/src/features.dart"]
    subprocess.check_call(cmd, cwd=flutter_sdk_folder)

def get_flutter_release_info(key, version):
    ''' Returns value from key lookup relative to the version '''
    ret = ""
    with urllib.request.urlopen(
            'https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json') as f:

        releases_linux_json = json.load(f)

        releases = releases_linux_json["releases"]
        for r in releases:
            if r["version"] == version or r["hash"] == version:
                ret = r[key]
                break

    if ret == "":
        raise ValueError("Could not get flutter sdk archive url")

    return ret


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


    print("FLUTTER_SDK: %s" % flutter_sdk_path)

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


def get_host_type():
    return get_process_stdout("uname").lower().rstrip()


def get_artifacts(platforms, flutter_sdk_path, flutter_auto_folder):
    ''' Get x86_64 Engine artifcats '''

    tmp_folder = os.path.join(flutter_auto_folder,'tmp')
    make_sure_path_exists(tmp_folder)

    engine_version = get_flutter_engine_version(flutter_sdk_path)
    engine_url = 'https://storage.googleapis.com/flutter_infra_release/flutter/%s/linux-x64/linux-x64-embedder' % engine_version
    engine_file = "%s/embedder.zip" % tmp_folder
    
    print("Attempting to download %s\n...to %s/embedder.zip" % (engine_url, tmp_folder))
    try:
        urllib.request.urlretrieve(engine_url, engine_file)
    except urllib.error.HTTPError as e:
        if e.code == 404:
            print("Engine archive not available.")
            return

    for platform in platforms:

        arch = platform['arch']

        if arch == "x86_64":

            if platform['id'] == 'desktop-auto' and platform['type'] == 'host':
                install_flutter_auto(platform)

            bundle_folder = os.path.join(flutter_auto_folder, engine_version[0:7], arch, platform['flutter_runtime'], 'bundle')
            
            lib_folder = os.path.join(bundle_folder, 'lib')
            make_sure_path_exists(lib_folder)

            data_folder = os.path.join(bundle_folder, 'data')
            make_sure_path_exists(data_folder)

            with zipfile.ZipFile("%s/embedder.zip" % tmp_folder, "r") as zip_ref:
                zip_ref.extractall(lib_folder)

            icudtl_source = os.path.join(flutter_sdk_path, "bin/cache/artifacts/engine/linux-x64/icudtl.dat")
            if not os.path.exists(icudtl_source):
                cmd = ["flutter", "doctor", "-v"]
                subprocess.check_call(cmd, cwd=flutter_sdk_path)

            host_type = get_host_type()
            icudtl_source = os.path.join(flutter_sdk_path, "bin/cache/artifacts/engine/%s-x64/icudtl.dat" % host_type)
            subprocess.check_call(["cp", icudtl_source, "%s/" % data_folder])

        else:
            print("%s not supported yet, skipping" % arch)

        # we only need one to trigger download
        break

    clear_folder(tmp_folder)


def install_flutter_auto(platform):

    host_type = get_host_type()

    if host_type == "linux":
        enforce_runtime_packages = False
        if 'enforce_runtime_packages' in platform:
            enforce_runtime_packages = platform['enforce_runtime_packages']
            if enforce_runtime_packages:

                # TODO test distro - this assumes Ubuntu

                cmd = ["bash", "-c", "sudo snap install cmake --classic"]
                subprocess.check_output(cmd)

                cmd = ["bash", "-c", "sudo add-apt-repository -y ppa:kisak/kisak-mesa"]
                subprocess.check_output(cmd)

                cmd = ["bash", "-c", "sudo apt update -y"]
                subprocess.check_output(cmd)

                cmd = ["bash", "-c", "sudo apt-get -y install libwayland-dev wayland-protocols mesa-common-dev libegl1-mesa-dev libgles2-mesa-dev mesa-utils libc++-11-dev libc++abi-11-dev libunwind-dev libxkbcommon-dev libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev gstreamer1.0-plugins-base gstreamer1.0-gl libavformat-dev"]
                subprocess.check_output(cmd)

                cmd = ["bash", "-c", "clang++ --version"]
                subprocess.check_output(cmd)


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

    post_build_filepath = os.path.join(folder,'post_build.sh')
    with open(post_build_filepath, 'w+') as post_build_file:
        post_build_file.writelines([
            "#!/usr/bin/env bash -l\n",
            "\n",
            "pushd . > '/dev/null';\n",
            "SCRIPT_PATH=\"${BASH_SOURCE[0]:-$0}\";\n",
            "\n",
            "while [ -h \"$SCRIPT_PATH\" ];\n",
            "do\n",
            "\n    cd \"$( dirname -- \"$SCRIPT_PATH\"; )\";\n",
            "\n    SCRIPT_PATH=\"$( readlink -f -- \"$SCRIPT_PATH\"; )\";\n",
            "done\n",
            "\n",
            "cd \"$( dirname -- \"$SCRIPT_PATH\"; )\" > '/dev/null';\n",
            "SCRIPT_PATH=\"$( pwd; )\";\n",
            "popd  > '/dev/null';\n",
            "\n",
            "echo \"********************************************\"\n",
            "echo \"* post_build.sh running from:\"\n",
            "echo \"* ${SCRIPT_PATH}\"\n",
            "echo \"* $0\"\n",
            "echo \"* $1\"\n",
            "echo \"********************************************\"\n"
        ])

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
                    
                    script.writelines(["export TARGET_USER=\"%s\";\n" % target_user])

                    if args.target_address:
                        target_address = args.target_address
                    else:
                        target_address = platform['target_address']

                    script.writelines(["export TARGET_ADDRESS=\"%s\";\n" % target_address])

                elif "qemu" == platform['type']:
                    script.writelines([
                        "echo \"********************************************\"\n",
                        "echo \" Type 'qemu_run' to start the emulator\"\n",
                        "echo \"********************************************\"\n",
                        "\n",
                        "qemu_run() {\n"
                        "\n",
                        "    if [ -z ${QEMU_IMAGE+x} ];\n",
                        "    then\n",
                        "        export QEMU_IMAGE=%s;\n" % platform['image'],
                        "    else\n",
                        "        echo \"QEMU_IMAGE is set to '$QEMU_IMAGE'\";\n",
                        "    fi\n",
                        "\n",
                        "    if [ -z ${QEMU_IMAGE+x} ];\n",
                        "    then\n",
                        "        export OVMF_PATH=%s;\n" % platform['ovmf_path'],
                        "    else\n",
                        "        echo \"OVMF_PATH is set to '$OVMF_PATH'\";\n",
                        "    fi\n",
                        "\n",
                        "    if pgrep -x \"%s\" > /dev/null\n" % platform['launch_app'],
                        "    then\n",
                        "        echo \"%s running - do nothing\"\n" % platform['launch_app'],
                        "    else\n",
                        "        gnome-terminal -- bash -c \"%s %s\"\n" % (platform['launch_app'], platform['launch_args']),
                        "    fi\n"
                        "}\n"
                    ])



if __name__ == "__main__":
    main()
