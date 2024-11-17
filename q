[1mdiff --git a/.github/workflows/qemuarm-linux-dummy.yml b/.github/workflows/qemuarm-linux-dummy.yml[m
[1mindex c4d6980..4b17341 100644[m
[1m--- a/.github/workflows/qemuarm-linux-dummy.yml[m
[1m+++ b/.github/workflows/qemuarm-linux-dummy.yml[m
[36m@@ -71,7 +71,7 @@[m [mjobs:[m
           echo 'INIT_MANAGER = "systemd"' >> ./conf/local.conf[m
           echo '***************************************'[m
           echo 'DISTRO_FEATURES:remove = "sysvinit ptest"' >> ./conf/local.conf[m
[31m-          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit"' >> ./conf/local.conf[m
[32m+[m[32m          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit pulseaudio"' >> ./conf/local.conf[m
           echo 'DISTRO_FEATURES_BACKFILL_CONSIDERED = ""' >> ./conf/local.conf[m
           echo 'PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"' >> ./conf/local.conf[m
           echo 'LICENSE_FLAGS_ACCEPTED += "commercial"' >> ./conf/local.conf[m
[1mdiff --git a/.github/workflows/qemuarm64-linux-dummy.yml b/.github/workflows/qemuarm64-linux-dummy.yml[m
[1mindex 918ed6c..df3e5c6 100644[m
[1m--- a/.github/workflows/qemuarm64-linux-dummy.yml[m
[1m+++ b/.github/workflows/qemuarm64-linux-dummy.yml[m
[36m@@ -71,7 +71,7 @@[m [mjobs:[m
           echo 'INIT_MANAGER = "systemd"' >> ./conf/local.conf[m
           echo '***************************************'[m
           echo 'DISTRO_FEATURES:remove = "sysvinit ptest"' >> ./conf/local.conf[m
[31m-          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit"' >> ./conf/local.conf[m
[32m+[m[32m          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit pulseaudio"' >> ./conf/local.conf[m
           echo 'DISTRO_FEATURES_BACKFILL_CONSIDERED = ""' >> ./conf/local.conf[m
           echo 'PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"' >> ./conf/local.conf[m
           echo 'LICENSE_FLAGS_ACCEPTED += "commercial"' >> ./conf/local.conf[m
[1mdiff --git a/.github/workflows/qemux86-64-linux-dummy.yml b/.github/workflows/qemux86-64-linux-dummy.yml[m
[1mindex 8662d76..74b14d8 100644[m
[1m--- a/.github/workflows/qemux86-64-linux-dummy.yml[m
[1m+++ b/.github/workflows/qemux86-64-linux-dummy.yml[m
[36m@@ -71,7 +71,7 @@[m [mjobs:[m
           echo 'INIT_MANAGER = "systemd"' >> ./conf/local.conf[m
           echo '***************************************'[m
           echo 'DISTRO_FEATURES:remove = "sysvinit ptest"' >> ./conf/local.conf[m
[31m-          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit"' >> ./conf/local.conf[m
[32m+[m[32m          echo 'DISTRO_FEATURES:append = " systemd x11 wayland opengl polkit pulseaudio"' >> ./conf/local.conf[m
           echo 'DISTRO_FEATURES_BACKFILL_CONSIDERED = ""' >> ./conf/local.conf[m
           echo 'PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"' >> ./conf/local.conf[m
           echo 'LICENSE_FLAGS_ACCEPTED += "commercial"' >> ./conf/local.conf[m
[1mdiff --git a/recipes-graphics/libwebrtc/libwebrtc_125.6422.06.bb b/recipes-graphics/libwebrtc/libwebrtc_125.6422.06.bb[m
[1mindex 3a44042..0f8c2c9 100644[m
[1m--- a/recipes-graphics/libwebrtc/libwebrtc_125.6422.06.bb[m
[1m+++ b/recipes-graphics/libwebrtc/libwebrtc_125.6422.06.bb[m
[36m@@ -18,7 +18,8 @@[m [mLIC_FILES_CHKSUM = "\[m
 DEPENDS += "\[m
     glib-2.0 \[m
     gtk+3 \[m
[31m-    pipewire \[m
[32m+[m[32m    ${@bb.utils.contains('DISTRO_FEATURES', 'pulseaudio', 'pulseaudio', '', d)} \[m
[32m+[m[32m    ${@bb.utils.contains('DISTRO_FEATURES', 'pipewire', 'pipewire', '', d)} \[m
     "[m
 [m
 SRCREV = "543121ba1cd47780e92d48546b880333265b37b5"[m
