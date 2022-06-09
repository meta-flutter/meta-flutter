"""
BitBake 'Fetch' implementations

This fetcher is created for gclient.
The main target is flutter-engine, so for other gclient projects, this fetcher might not work.

Copyright (c) 2021-2022 Woven Alpha, Inc
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
        # syntax: gn://<URL>;name=<NAME>;destdir=<D>;proto=<PROTO>
        name = ud.parm.get("name", "src")
        ud.destdir = "" if "destdir" not in ud.parm else ud.parm["destdir"]
        proto = "https" if "proto" not in ud.parm else ud.parm["proto"]

        ud.basename = "*"

        custom_vars = d.getVar("GN_CUSTOM_VARS")
        sync_opt = d.getVar("EXTRA_GN_SYNC")

        depot_tools_path = d.getVar("DEPOT_TOOLS")
        python2_path = d.getVar("PYTHON2_PATH")

        uri = ud.url.split(";")[0].replace("gn://", "%s://" % (proto))
        gclient_config = '''solutions = [
    {
        "managed": False,
        "name": "%s",
        "url": "%s",
        "custom_vars": %s
    }
]''' % (name, uri, custom_vars)

        srcrev = d.getVar("SRCREV")
        dl_dir = d.getVar("DL_DIR")
        gndir = os.path.join(dl_dir, "gn")
        ud.syncdir = uri.replace(":", "").replace("/", "_")
        ud.syncpath = os.path.join(gndir, ud.syncdir)
        ud.localfile = ud.syncdir + "-" + srcrev + ".tar.bz2"
        ud.localpath = os.path.join(gndir, ud.localfile)

        ud.basecmd = "export PATH=\"%s:%s:${PATH}\"; export DEPOT_TOOLS_UPDATE=0; export GCLIENT_PY3=0; \
            export CURL_CA_BUNDLE=%s; \
            gclient.py config --spec '%s' && \
            gclient.py sync %s --revision %s %s -v" % (
                depot_tools_path, os.path.join(depot_tools_path, python2_path),
                d.getVar('CURL_CA_BUNDLE'),
                gclient_config,
                sync_opt, srcrev, d.getVar('PARALLEL_MAKE'))

        bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()

        ud.packcmd = "tar -I \"pbzip2 -p%s\" -cf %s ./" % (bb_number_threads, ud.localpath)

    def _rungnclient(self, ud, d, quiet):
        bb.utils.mkdirhier(ud.syncpath)
        os.chdir(ud.syncpath)

        logger.debug(2, "Fetching %s using command '%s'" % (ud.url, ud.basecmd))
        bb.fetch2.check_network_access(d, ud.basecmd, ud.url)
        runfetchcmd(ud.basecmd, d, quiet, workdir=None)
        logger.debug(2, "Packing %s using command '%s'" % (ud.url, ud.packcmd))
        runfetchcmd(ud.packcmd, d, quiet, workdir=None)

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
            raise FetchError("The fetch command returned success for url %s but %s doesn't exist?!" % (uri, ud.localpath), uri)

        if os.path.getsize(ud.localpath) == 0:
            os.remove(ud.localpath)
            raise FetchError("The fetch of %s resulted in a zero size file?! Deleting and failing since this isn't right." % (uri), uri)

        return True

    def unpack(self, ud, workdir, d):
        file = ud.localpath

        bb_number_threads = d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count()).strip()
        cmd = 'pbzip2 -dc -p%s %s | tar x --no-same-owner -f -' % (bb_number_threads, file)
        unpackdir = os.path.join(workdir, ud.destdir)
        bb.utils.mkdirhier(unpackdir)
        path = d.getVar('PATH')
        if path:
            cmd = "PATH=\"%s\" %s" % (path, cmd)
        bb.note("Unpacking %s to %s" % (file, unpackdir))
        ret = subprocess.call(cmd, preexec_fn=subprocess_setup, shell=True, cwd=unpackdir)

        if ret != 0:
            raise UnpackError("Unpack command %s failed with return value %s" % (cmd, ret), ud.url)

        return


    def clean(self, ud, d):
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

