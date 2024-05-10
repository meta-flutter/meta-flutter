# Copyright (C) 2024 Seungkyun Kim. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

"""
BitBake 'Fetch' pubspec.json that is converted from pubspec.lock implementation

pub fetcher support the SRC_URI with format of:
SRC_URI = "pub://pubspec.lock.json;..."

https://github.com/dart-lang/pub/blob/master/doc/repository-spec-v2.md

Supported SRC_URI options are:

- destsuffix
    Specifies the directory to use to unpack the dependencies
"""

import bb
import hashlib
import json
import os
import re
import shlex
import urllib.request, urllib.parse, urllib.error

from bb.fetch2 import Fetch
from bb.fetch2 import FetchMethod
from bb.fetch2 import ParameterError
from bb.fetch2 import runfetchcmd
from bb.fetch2 import URI
from bb.utils import lockfile
from bb.utils import unlockfile

from simple_yaml import SimpleYAMLParser

def foreach_packages(packages, d):
    """
        Run for each a pubspec.loc file.
    """
    pkginfo = []
    for name, params in packages.items():
        pkg = get_package_info(name, params, d)
        if pkg is not None:
          pkginfo.append(pkg)

    return pkginfo


def get_package_info(pkg_name, params, d):
    url = None
    localpath = None
    urlhash = None
    extrapaths = []
    unpack = True

    source = params.get("source", None)
    version = params.get("version", None)
    description = params.get("description", None)

    if description is None or source is None or version is None:
        return None

    if source == "sdk":
        return None

    name = description.get("name", pkg_name)
    integrity = description.get("sha256", None)
    resolved_ref = description.get("resolved-ref", None)
    url = description.get("url", None)

    if url is None:
        return None

    # Handle registry sources
    if source == "hosted" and integrity:
        package_file =  name + "-" + version + ".tar.gz"
        localfile = "pub/" + package_file
        localpath = os.path.join(d.getVar("DL_DIR"), localfile)

        resolved = "file://" + localpath

        #ex) resolved  https://pub.dev/api/archives/plugin_platform_interface-2.1.5.tar.gz
        if d.getVar("BB_CURRENTTASK") == "fetch":
            resolved = get_source_url(url, pkg_name, version, d)

        # hosted/pub.dev/
        destsuffix = os.path.join(source, \
                                  re.sub(r"^https?://", "", url), \
                                  re.sub(r".tar.gz$", "", package_file))

        uri = URI(resolved)
        uri.params["downloadfilename"] = localfile
        uri.params["sha256sum"] = integrity

        ud_url = str(uri)
    elif source == "git" and resolved_ref:
        regex = re.compile(r"""
            ^
            (?P<protocol>[a-z]+)
            ://
            (?P<url>.+/)
            (?P<repo>.+)
            $
            """, re.VERBOSE)

        match = regex.match(url)

        if not match:
            raise ParameterError("Invalid git url: %s" % name, url)

        groups = match.groupdict()
        destsuffix = os.path.join(source, str(groups["repo"]) + "-" + resolved_ref)

        uri = URI("git://" + str(groups["url"]) +str(groups["repo"]))
        uri.params["protocol"] = str(groups["protocol"])
        uri.params["rev"] = str(resolved_ref)
        uri.params["nobranch"] = "1"
        uri.params["destsuffix"] = destsuffix

        urlhash = hashlib.sha1(url.encode('utf-8')).hexdigest()
        ud_url = str(uri)
    else:
        # Do nothing for the "path" or "sdk"
        return None;

    return {
        "url": ud_url,
        "localpath": localpath,
        "extrapaths": extrapaths,
        "destsuffix": destsuffix,
        "unpack": unpack,
        "urlhash": urlhash,
    }

def get_source_url(hosted_url, pkg_name, version, d):
    newenv = bb.fetch2.get_fetcher_environment(d)

    with bb.utils.environment(**newenv):
        import ssl
        # ex) https://pub.dev/api/packages/plugin_platform_interface
        request_url = hosted_url + "/api/packages/" + pkg_name

        try:
            if (d.getVar("BB_CHECK_SSL_CERTS") or "1") != "0":
                context = ssl.create_default_context()
            else:
                context = ssl._create_unverified_context()
            context = ssl._create_unverified_context()

            response = urllib.request.urlopen(request_url, context=context)
        except urllib.error.URLError as e:
            bb.fatal('HTTP error: %s' % (e))
        except (ValueError, TypeError) as e:
            bb.fatal('JSON error: %s' % (e))

        buf = response.read()
        pub_package_json = json.loads(buf.decode('utf-8'))
        for ver_pkg in pub_package_json['versions']:
            if ver_pkg['version'] == version:
                    return ver_pkg['archive_url']

    raise bb.fetch2.FetchError("Unable to fetch package version data.", request_url)
    return ""

class Pub(FetchMethod):
    """Class to fetch all package from a pubspec.lock file"""

    def supports(self, ud, d):
        """Check if a given url can be fetched with pub"""
        return ud.type in ["pub"]

    def urldata_init(self, ud, d):
        """Init pub specific variables within url data"""

        # Get the 'pubspec' parameter
        ud.pubspec = re.sub(r"^pub://", "", ud.url.split(";")[0])

        try:
            with open(ud.pubspec, "r") as f:
                parser = SimpleYAMLParser()
                pubspec = parser.load(f)
        except Exception as e:
            raise ParameterError("Invalid pubspec file: %s" % str(e), ud.url)

        if 'packages' not in pubspec:
            raise ParameterError("Invalid pubspec file format: " % ud.url)

        # Compute package list
        ud.deps = foreach_packages(pubspec.get("packages", {}), d)

        # Avoid conflicts between the environment data and:
        # - the proxy url revision
        # - the proxy url checksum
        data = bb.data.createCopy(d)
        data.delVar("SRCREV")
        data.delVarFlags("SRC_URI")

        # Reolves multiple URIs from a pubspec.lock and forwards to proxy fetcher
        # the lockfile and the checksums are forwarded to the proxy fetcher.
        ud.proxy = Fetch([dep["url"] for dep in ud.deps if dep["url"]], data)
        ud.needdonestamp = False

    @staticmethod
    def _foreach_proxy_method(ud, handle):
        returns = []
        for proxy_url in ud.proxy.urls:
            proxy_ud = ud.proxy.ud[proxy_url]
            proxy_d = ud.proxy.d
            proxy_ud.setup_localpath(proxy_d)
            lf = lockfile(proxy_ud.lockfile)
            returns.append(handle(proxy_ud.method, proxy_ud, proxy_d))
            unlockfile(lf)
        return returns

    def get_local_path(self, url, d):
        regex = re.compile(r"""
            ^
            (?P<protocol>[a-z]+)
            ://
            (?P<host>.+)
            $
            """, re.VERBOSE)
        match = regex.match(url.split(";")[0])

        if not match:
            raise ParameterError("Invalid git url: %s" % name, url)
        groups = match.groupdict()

        gitsrcname = (groups["host"].replace(':', '.').replace('/', '.').replace('*', '.').replace(' ','_'))
        if gitsrcname.startswith('.'):
            gitsrcname = gitsrcname[1:]
        dl_dir = d.getVar("DL_DIR")
        gitdir = d.getVar("GITDIR") or (dl_dir + "/git2")
        return os.path.join(gitdir, gitsrcname)

    def need_update(self, ud, d):
        """Force a fetch, even if localpath exists ?"""
        def _handle(m, ud, d):
            return m.need_update(ud, d)
        return all(self._foreach_proxy_method(ud, _handle))

    def try_premirror(self, ud, d):
        """Try to use a premirror"""
        def _handle(m, ud, d):
            if hasattr(m, 'try_premirrors'):
                return m.try_premirrors(fetch, ud, d)
            return False
        return all(self._foreach_proxy_method(ud, _handle))

    def download(self, ud, d):
        """Fetch url"""
        ud.proxy.download()

    def unpack(self, ud, rootdir, d):
        """Unpack the downloaded dependencies"""
        destdir = d.getVar("PUB_CACHE") or \
            os.path.join(d.getVar("WORKDIR"), "pub_cache")
        bb.utils.mkdirhier(destdir)

        remotegit = [dep["url"] for dep in ud.deps if not dep["localpath"]]
        hosted = [dep for dep in ud.deps if dep["localpath"]]

        cachedgit = {dep["urlhash"]:dep["url"] for dep in ud.deps if dep["urlhash"] and not dep["localpath"]}

        if remotegit:
            ud.proxy.unpack(destdir, remotegit)

        depdestdir = os.path.join(destdir, "git", "cache")
        bb.utils.mkdirhier(depdestdir)
        # This is to avaoid 'git fetch' by 'pub get' command.
        for urlhash, url in cachedgit.items():
            localpath = self.get_local_path(url, d)

            uri = URI(url)
            if not uri.params["destsuffix"]:
                continue
            destsuffix = re.sub(r"^git", r"cache", \
                re.sub(r"[^-]*$", urlhash, uri.params["destsuffix"]))
            depdestdir = os.path.join(destdir, "git", destsuffix)
            bb.utils.remove(depdestdir, recurse=True)
            runfetchcmd("git clone %s %s/ %s" % ("--mirror", localpath, depdestdir), d)

        for dep in hosted:
            depdestdir = os.path.join(destdir, dep["destsuffix"])
            if dep["url"]:
                """Unpack a pub tarball"""
                bb.utils.mkdirhier(depdestdir)
                cmd = "tar --extract --gzip --file=%s" % shlex.quote(dep["localpath"])
                cmd += " --no-same-owner"
                cmd += " --delay-directory-restore"
                runfetchcmd(cmd, d, workdir=depdestdir)
                runfetchcmd("chmod -R +X '%s'" % (depdestdir), d, quiet=True, \
                    workdir=depdestdir)

                uri = URI(dep["url"])
                if uri.params["sha256sum"]:
                    destdir_hashes = re.sub(r"^hosted/", "hosted-hashes/", dep["destsuffix"])
                    destdir_hashes += ".sha256"
                    dep_hashes= os.path.join(destdir, destdir_hashes)

                    hashpath = os.path.dirname(dep_hashes)
                    if not os.path.exists(hashpath):
                        bb.utils.mkdirhier(hashpath)

                    with open(dep_hashes, "w") as hashfile:
                        hashfile.write(uri.params["sha256sum"])
                        hashfile.close()

    def clean(self, ud, d):
        """ clean the git directory and package tar.gz in pub directory """
        ud.proxy.clean()

        hosted = [dep for dep in ud.deps if dep["localpath"]]

        for dep in hosted:
            if dep["localpath"]:
                bb.utils.remove(dep["localpath"])
                bb.utils.remove(dep["localpath"] + ".done")
                bb.utils.remove(dep["localpath"] + ".lock")

