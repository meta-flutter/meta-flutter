**1. Run Ubuntu on Wayland**

Please make sure your Ubuntu is running **Ubuntu on Wayland**. If you don’t see this option, below are the steps to enable it:

    sudoedit /etc/gdm3/custom.conf

comment out WaylandEnable=false  

    # WaylandEnable=false  

save the file and restart GDM3

    sudo systemctl restart gdm3

**2. Create a user with known user id**

A user "sim" with user id 5000 can be created with the command below: 

    sudo adduser --gecos '' -u 5000 sim
After this user is created, login Ubuntu with this user id.

**3. Login Ubuntu with the new user id**

**4.  Install NVIDIA container-toolkit**

 Follow the steps in "Installing on Ubuntu and Debian" of [NVIDIA's container-toolkit installation guide](%5Bhttps://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html%5D) to install NVIDIA container-toolkit on your Ubuntu machine.
 
**5.  Download the artifact:**

 - Download [containerx86-64-app-container-image-flutter-auto.zip](https://github.com/meta-flutter/meta-flutter/suites/10539746717/artifacts/524169324). 
 - Unzip containerx86-64-app-container-image-flutter-auto.zip, you will get **app-container-image-flutter-auto-containerx86-64.tar.bz2**. This is the file we need later.   
  
**6. import the docker container:**
    
    docker import -c "USER weston" -c "WORKDIR /home/weston" app-container-image-flutter-auto-containerx86-64.tar.bz2 app-container:latest

**7.  Run the docker container:**

    docker run -it -v /run/user/$UID:/run/user/$UID -v $(pwd)/test:/home/weston/test --user 5000:5000 --gpus all -e XDG_RUNTIME_DIR=$XDG_RUNTIME_DIR -e WAYLAND_DISPLAY=$WAYLAND_DISPLAY 5e1fcf6ffb4c /bin/sh
 Note that 5e1fcf6ffb4c is the image id. You might see different number on your system. It can be found by issuing `sudo docker images`. Please update this number accordingly.
 
**8.  Bring up Weston compositor**
 
    weston -B wayland-backend.so
    
A Weston Compositor should be brought up. 




