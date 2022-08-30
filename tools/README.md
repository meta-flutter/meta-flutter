## Flutter Workspace Automation

We developed a Python script, `setup_flutter_workspace.py` to automate embedded flutter setup.
This script reads a JSON configuration file and sets up a Flutter Workspace.


### setup_flutter_workspace.py

setup_flutter_workspace.py does the following tasks automatically for you

* Creates workspace
* Enumerates all repositories defined and clones them in the app folder
* Installs custom-devices
* Installs flutter-auto and runtime dependencies
* Installs QEMU image and runtime dependencies
* Creates setup_env.sh
* Runs on Linux and Mac


### Flutter Workspace

A Flutter workspace contains the following components

* Flutter SDK
* Development Repositories
* Host Runtime images
* flutter-auto binary
* QEMU image
* Versioned x86_64 libflutter_engine.so and icudtl.dat (debug)
* Custom-device configurations
* Public Cache


### JSON Configuration 

flutter_workspace_config.json contains the following components

* General: flutter-version, github_token, and Platforms Object
* General: id, type, arch, flutter_runtime
* Runtime: key/values related to installing binary runtime
* Custom-device: key/values directly installed as custom-device
* Repos Object: Array of GIT repos to clone: uri, branch, rev
* Minimal configuration: {"flutter-version":"stable","platforms":[],"repos":[]}


### Installation Method 1: Easy Install  

```
mkdir -p $HOME/workspace && cd $HOME/workspace && wget https://raw.githubusercontent.com/meta-flutter/meta-flutter/kirkstone/tools/flutter_workspace_config.json && curl --proto '=https' --tlsv1.2 -sSf https://raw.githubusercontent.com/meta-flutter/meta-flutter/kirkstone/tools/setup_flutter_workspace.py | python3
```


### Installation Method 2: Manual Install 

```
git clone –b kirkstone https://github.com/meta-flutter/meta-flutter.git
cd meta-flutter/tools
./setup_flutter_workspace.py
```


### Run flutter app with desktop-auto 

* Login via GDM Wayland Session
* Open Terminal and type
* `source ${FLUTTER_WORKSPACE}/setup_env.sh`
* Navigate to your favorite app
* `flutter run`
* Select "Toyota flutter-auto (desktop-auto)"


### Run flutter app with QEMU 

* Open Terminal and type
* `source ${FLUTTER_WORKSPACE}/setup_env.sh`
* Type `qemu_run`
* Wait until QEMU image reaches login prompt
* Run `ssh –p 2222 root@localhost who` to add remote host to ~/.ssh/known_hosts
* Navigate to your favorite app
* `flutter run`
* Select "AGL x86_64 QEMU Image (AGL-qemu)"


### Create hello_world flutter example 

* Login to Ubuntu desktop via Wayland Session
* Open Terminal and type
* `source ${FLUTTER_WORKSPACE}/setup_env.sh`
* `cd ${FLUTTER_WORKSPACE}/app`
* `flutter create hello_world -t app`
* `cd hello_world`
* `flutter run`
* Select "Toyota flutter-auto (desktop-auto)"

### Visual Studio Code

#### Launching on Ubuntu

```
    cd <your flutter workspace>
    source ./setup_env.sh
    code .
```
#### Debugging

`setup_flutter_workspace.py` creates a `.vscode/launch.json` file if one is not present.
It uses the repo configuration key `pubspec_path`.  If this key is present in the repo
entry, then it will add entry to `.vscode/launch.json`.

### M1 Mac PyCurl install
```
arch -arm64 brew reinstall openssl
pip3 uninstall pycurl
arch -arm64 pip3 install --install-option="--with-openssl" --install-option="--openssl-dir=/usr/local/opt/openssl@1.1" pycurl
```
