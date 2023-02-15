# Running `app-container-image-flutter-auto-containerx86-64` container in a Wayland Session

### Pre-requisites

1. Install Docker and ensure it is functioning via `docker run hello-world`

2. Running Wayland Session

## Ubuntu

Please make sure your Ubuntu is running Ubuntu on Wayland.  Quick check:
```
$ echo $XDG_SESSION_TYPE
wayland
```

 If you don’t see `wayland`, see steps below to enable it.

```
sudoedit /etc/gdm3/custom.conf
```

comment out `WaylandEnable=false`

```
# WaylandEnable=false  
```

save the file and restart GDM3

```
sudo systemctl restart gdm3
```

* logout
* select the gear icon on lower right of screen
* select the Wayland option
* login

Confirm correct session type using `echo $XDG_SESSION_TYPE`

## Setup Host to Enable PCIe Passthrough for KVM

### NVidia

Follow the steps in "Installing on Ubuntu and Debian" of NVIDIA's container-toolkit installation guide to install NVIDIA container-toolkit on your Ubuntu machine.
https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html

### AMD

Follow the setps in "PCIe Passthrough on KVM":
https://rocmdocs.amd.com/en/latest/ROCm_Virtualization_Containers/ROCm-Virtualization-&-Containers.html


## Download and Import Container

- Download container from current CI job here:
https://github.com/meta-flutter/meta-flutter/actions/workflows/kirkstone-container-x86-64.yml
 - Unzip containerx86-64-app-container-image-flutter-auto.zip, you will get **app-container-image-flutter-auto-containerx86-64.tar.bz2**. This is the file we need later.   


```
sudo docker import -c "USER weston" -c "WORKDIR /home/weston" app-container-image-flutter-auto-containerx86-64.tar.bz2 app-container:latest
```

## Launch Docker Container

to list the docker containers in your local registry run
```
docker images
```
Note the container id of the container we imported above.


### NVidia
```
sudo docker run -it -v /run/user/$UID:/run/user/$UID -v $(pwd)/test:/home/weston/test --user $UID:$UID --gpus all -e XDG_RUNTIME_DIR=$XDG_RUNTIME_DIR -e WAYLAND_DISPLAY=$WAYLAND_DISPLAY <container id> /bin/sh
```

### AMD
```
cd $HOME
mkdir tmp
docker run -it --device=/dev/kfd --device=/dev/dri --security-opt seccomp=unconfined --group-add video --user $UID:$UID -e XDG_RUNTIME_DIR=$XDG_RUNTIME_DIR -v /run/user/$UID:/run/user/$UID -e WAYLAND_DISPLAY=wayland-0 -v $HOME/tmp:/home/weston/tmp <container id> /bin/sh
```

## Weston - Run Compositor

Run weston connecting to the preferred Host WAYLAND_DISPLAY.

```
$ weston -B wayland-backend.so &
```

#### Container

Note contents of XDG_RUNTIME_DIR
```
$ ls -la $XDG_RUNTIME_DIR
drwx------   12 weston   weston         440 Jan 31 15:01 .
drwxr-xr-x    1 root     root             8 Jan 31 15:01 ..
-rw-------    1 weston   weston         100 Jan 31 14:01 .mutter-Xwaylandauth.OCDSZ1
-rw-------    1 weston   weston         310 Jan 31 14:01 ICEauthority
drwx------    2 weston   weston          60 Jan 31 14:01 at-spi
srw-rw-rw-    1 weston   weston           0 Jan 31 14:01 bus
drwx------    2 weston   weston          60 Jan 31 14:02 dconf
dr-x------    2 weston   weston           0 Jan  1  1970 doc
prw-r--r--    1 weston   weston           0 Jan 31 14:01 gnome-session-leader-fifo
drwx------    3 weston   weston          60 Jan 31 14:01 gnome-shell
dr-x------    2 weston   weston           0 Jan 31 14:01 gvfs
drwx------    2 weston   weston          40 Jan 31 14:27 gvfsd
drwx------    2 weston   weston         120 Jan 31 14:20 keyring
srw-rw-rw-    1 weston   weston           0 Jan 31 14:01 pipewire-0
-rw-r-----    1 weston   weston           0 Jan 31 14:01 pipewire-0.lock
drwx------    2 weston   weston          80 Jan 31 14:01 pulse
drwx------    4 weston   weston         100 Jan 31 14:17 speech-dispatcher
drwxr-xr-x    7 weston   weston         180 Jan 31 14:01 systemd
srwxr-xr-x    1 weston   weston           0 Jan 31 14:01 wayland-0
-rw-r-----    1 weston   weston           0 Jan 31 14:01 wayland-0.lock
```

## See what new Wayland Display value is available
```
$ ls -la $XDG_RUNTIME_DIR
drwx------   12 weston   weston         440 Jan 31 15:01 .
drwxr-xr-x    1 root     root             8 Jan 31 15:01 ..
-rw-------    1 weston   weston         100 Jan 31 14:01 .mutter-Xwaylandauth.OCDSZ1
-rw-------    1 weston   weston         310 Jan 31 14:01 ICEauthority
drwx------    2 weston   weston          60 Jan 31 14:01 at-spi
srw-rw-rw-    1 weston   weston           0 Jan 31 14:01 bus
drwx------    2 weston   weston          60 Jan 31 14:02 dconf
dr-x------    2 weston   weston           0 Jan  1  1970 doc
prw-r--r--    1 weston   weston           0 Jan 31 14:01 gnome-session-leader-fifo
drwx------    3 weston   weston          60 Jan 31 14:01 gnome-shell
dr-x------    2 weston   weston           0 Jan 31 14:01 gvfs
drwx------    2 weston   weston          40 Jan 31 14:27 gvfsd
drwx------    2 weston   weston         120 Jan 31 14:20 keyring
srw-rw-rw-    1 weston   weston           0 Jan 31 14:01 pipewire-0
-rw-r-----    1 weston   weston           0 Jan 31 14:01 pipewire-0.lock
drwx------    2 weston   weston          80 Jan 31 14:01 pulse
drwx------    4 weston   weston         100 Jan 31 14:17 speech-dispatcher
drwxr-xr-x    7 weston   weston         180 Jan 31 14:01 systemd
srwxr-xr-x    1 weston   weston           0 Jan 31 14:01 wayland-0
-rw-r-----    1 weston   weston           0 Jan 31 14:01 wayland-0.lock
srwxr-xr-x    1 weston   weston           0 Jan 31 15:01 wayland-1
-rw-r-----    1 weston   weston           0 Jan 31 15:01 wayland-1.lock
```

Notice `wayland-1` and `wayland.lock` are new.  `wayland-1` is the container compositor we just launched, and `wayland-0` is the host compositor.


### Launch Wayland App

To select the compositor which to run your Wayland app from prefix command from either host or container side with `WAYLAND_DISPLAY`.


Run host wayland app in container compositor:
```
WAYLAND_DISPLAY=display-1 flutter-auto --b=$HOME/tmp/flutter/gallery/3.3.10/release
```
