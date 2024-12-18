"""
BitBake 'Fetch' implementations

This fetcher is created for gclient.
The main target is flutter-engine, so for other gclient projects, this fetcher might not work.

Copyright (c) 2020-2022 Woven Alpha, Inc
Copyright (c) 2023-2024 Joel Winarske. All rights reserved.
"""

import os
import bb
import multiprocessing
import subprocess
import urllib
from   bb.fetch2 import FetchMethod
from   bb.fetch2 import FetchError
from   bb.fetch2 import UnpackError
from   bb.fetch2 import logger
from   bb.fetch2 import runfetchcmd
from   bb.fetch2 import subprocess_setup

class GN(FetchMethod):
    """Class to fetch urls via 'wget'"""
    def supports(self, ud, d):
        """
        Check to see if a given url can be fetched with gn.
        """
        return ud.type in ['gn']

    def recommends_checksum(self, urldata):
        return False

    def urldata_init(self, ud, d):
        # syntax: gn://<URL>;gn_name=<NAME>;destdir=<D>;proto=<PROTO>
        name = ud.parm.get("gn_name", "src")

        ud.destdir = "" if "destdir" not in ud.parm else ud.parm["destdir"]
        proto = "https" if "proto" not in ud.parm else ud.parm["proto"]

        ud.basename = "*"

        uri = ud.url.split(';')[0].replace('gn://', f'{proto}://')

        deps_file = d.getVar("GN_DEPS_FILE")
        custom_vars = d.getVar("GN_CUSTOM_VARS")
        custom_deps = d.getVar("GN_CUSTOM_DEPS")

        gclient_config = f'''solutions = [
  {{
    "name": "{name}",
    "url": "{uri}",
    "deps_file": "{deps_file}",
    "managed": False,
    "custom_deps": {custom_deps},
    "custom_vars": {custom_vars},
  }},
]'''

        srcrev = d.getVar("SRCREV")
        dl_dir = d.getVar("DL_DIR")
        gndir = os.path.join(dl_dir, "gn")
        ud.syncdir = uri.replace(":", "").replace("/", "_")
        ud.syncpath = os.path.join(gndir, ud.syncdir)
        ud.localfile = ud.syncdir + "-" + srcrev + ".tar.bz2"
        ud.localpath = os.path.join(gndir, ud.localfile)
        ud.trying_to_fetch_with_gclient = False

        sync_opt = d.getVar("EXTRA_GN_SYNC")
        curl_ca_bundle = d.getVar('CURL_CA_BUNDLE')
        parallel_make = d.getVar('PARALLEL_MAKE')

        depot_tools_path = d.getVar("DEPOT_TOOLS")
        python3_folder = os.path.join(depot_tools_path, d.getVar("PYTHON3_PATH"))
        vpython_virtualenv_root = d.getVar("VPYTHON_VIRTUALENV_ROOT")
        depot_tools_xdg_config_home = d.getVar("DEPOT_TOOLS_XDG_CONFIG_HOME")

        ud.basecmd = f'export DEPOT_TOOLS_UPDATE=0; \
            export XDG_CONFIG_HOME={depot_tools_xdg_config_home}; \
            export CURL_CA_BUNDLE={curl_ca_bundle}; \
            export PATH="{depot_tools_path}:{python3_folder}:$PATH"; \
            rm -rf $VPYTHON_VIRTUALENV_ROOT ||true; \
            mkdir -p $VPYTHON_VIRTUALENV_ROOT; \
            export VPYTHON_VIRTUALENV_ROOT="{vpython_virtualenv_root}"; \
            gclient config --spec \'{gclient_config}\'; \
            gclient sync --force {sync_opt} --revision {srcrev} {parallel_make} -v'

        bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()

        ud.packcmd = f'tar -I "pbzip2 -p{bb_number_threads}" -cf {ud.localpath} ./'

    def _rungnclient(self, ud, d, quiet):
        bb.utils.mkdirhier(ud.syncpath)
        os.chdir(ud.syncpath)

        ud.trying_to_fetch_with_gclient = True
        logger.debug2(f'Fetching {ud.url} using command "{ud.basecmd}"')
        bb.fetch2.check_network_access(d, ud.basecmd, ud.url)
        runfetchcmd(ud.basecmd, d, quiet, workdir=None)
        logger.debug2(f'Packing {ud.url} using command "{ud.packcmd}"')
        runfetchcmd(ud.packcmd, d, quiet, workdir=None)
        ud.trying_to_fetch_with_gclient = False

    def localpath(self, ud, d):
        """
        Return the local filename of a given url assuming a successful fetch.
        Can also setup variables in urldata for use in go (saving code duplication
        and duplicate code execution)
        """
        dl_dir = d.getVar("DL_DIR")
        gndir = os.path.join(dl_dir, "gn")
        return os.path.join(gndir, ud.localfile)

    def download(self, ud, d):
        """Fetch urls"""
        # If tar.bz2 exists, skip
        if os.access(ud.localpath, os.R_OK):
            return True

        uri = ud.url.split(";")[0]

        self._rungnclient(ud, d, False)

        # Sanity check since wget can pretend it succeed when it didn't
        # Also, this used to happen if sourceforge sent us to the mirror page
        if not os.path.exists(ud.localpath):
            raise FetchError(f'The fetch command returned success for url {uri} but {ud.localpath} does note exist?!', uri)

        if os.path.getsize(ud.localpath) == 0:
            os.remove(ud.localpath)
            raise FetchError(f'The fetch of {uri} resulted in a zero size file?! Deleting and failing since this is not right.', uri)

        return True

    def unpack(self, ud, workdir, d):
        file = ud.localpath

        bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
        cmd = f'pbzip2 -dc -p{bb_number_threads} {file} | tar x --no-same-owner -f -'
        unpackdir = os.path.join(workdir, ud.destdir)
        bb.utils.mkdirhier(unpackdir)
        path = d.getVar('PATH')
        if path:
            cmd = f'PATH="{path}" {cmd}'
        bb.note("Unpacking {file} to {unpackdir}")
        try:
            subprocess.check_output(cmd, preexec_fn=subprocess_setup, shell=True, cwd=unpackdir, 
                                    stderr=subprocess.STDOUT, universal_newlines=True)
        except subprocess.CalledProcessError as e:
            raise UnpackError(f'Unpack command {cmd} failed with return value {e.returncode}\n{e.stdout}', ud.url)


    def clean(self, ud, d):
        # If an error occurs during the gclient sync, we aim to retain the partial sync directory.
        # This helps prevent the need to re-fetch a large amount of source code only to encounter the same network issue again.
        # The sync directory can grow up to 14GB.
        # However, it will still be deleted when running bitbake -c cleanall.
        if not ud.trying_to_fetch_with_gclient:
            bb.utils.remove(ud.syncpath, recurse=True)
        bb.utils.remove(ud.localpath, recurse=True)

    def checkstatus(self, fetch, ud, d, try_again=True):
        return True

    def latest_versionstring(self, ud, d):
        """
        Manipulate the URL and try to obtain the latest package version

        sanity check to ensure same name and type.
        """
        return ("", '')

