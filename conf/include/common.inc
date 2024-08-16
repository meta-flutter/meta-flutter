# Copyright (C) 2023 Joel Winarske. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

DEPENDS += " \
    ca-certificates-native \
    flutter-sdk-native \
    python3-pyyaml-native \
    python3-pycurl-native \
    unzip-native \
    "

FLUTTER_APPLICATION_PATH ??= ""
FLUTTER_PREBUILD_CMD ??= ""

PUB_CACHE_EXTRA_ARCHIVE_CMD ??= ""
PUB_CACHE_EXTRA_ARCHIVE_PATH ??= ""

APP_AOT_EXTRA ??= ""
APP_AOT_ENTRY_FILE ??= "main.dart"
APP_GEN_SNAPSHOT_FLAGS ??= ""

FLUTTER_APP_RUNTIME_MODES ?= "release"

FLUTTER_APPLICATION_INSTALL_PREFIX ??= "${datadir}/flutter"
FLUTTER_APPLICATION_INSTALL_SUFFIX ??= "${PUBSPEC_APPNAME}"
FLUTTER_INSTALL_DIR = "${FLUTTER_APPLICATION_INSTALL_PREFIX}/${FLUTTER_APPLICATION_INSTALL_SUFFIX}"

PUB_CACHE = "${WORKDIR}/pub_cache"
_REV = "${@d.getVar('SRCPV') or d.getVar('PV')}"

FLUTTER_SDK_PATH = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"

require conf/include/clang-utils.inc

inherit python3native

python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))

    setup_environment()
}


def get_status_output(cmd, cwd):
    from subprocess import check_output, CalledProcessError, STDOUT

    try:
        data = check_output(cmd, shell=True, universal_newlines=True, stderr=STDOUT, cwd=cwd)
        status = 0
    except CalledProcessError as ex:
        data = ex.output
        status = ex.returncode
    if data[-1:] == '\n':
        data = data[:-1]
    return status, data


def run_command(cmd, cwd):
    import re

    # replace all consecutive whitespace characters (tabs, newlines etc.) with a single space
    cmd = re.sub('\\s{2,}', ' ', cmd)

    bb.debug(1, 'Running [%s] in %s' % (cmd, cwd))

    (retval, output) = get_status_output(cmd, cwd)

    if retval:
        formatted_output = '\n\t' + output.replace('\n','\n  ')
        bb.fatal(f'{cmd} failed: {retval}{formatted_output}')


def get_versions_obj(file_path):
    """
    Returns dictionary of versions JSON
    """
    import json

    try:
        with open(file_path, 'r') as file:
            data = json.load(file)
        return data
    except FileNotFoundError:
        bb.error(f"The file {file_path} does not exist.")
        return dict()
    except json.JSONDecodeError:
        bb.error(f"There was an error decoding the JSON from the file {file_path}")
        return dict()
    except Exception as e:
        bb.error(f"An unexpected error occurred: {e}")
        return dict()


def get_yaml_obj(filepath):
    """
    Returns python object of yaml file
    """
    import yaml

    if not os.path.exists(filepath):
        bb.fatal(f'Failed loading {filepath}')

    with open(filepath, "r") as stream_:
        try:
            data_loaded = yaml.full_load(stream_)

        except yaml.YAMLError:
            bb.fatal(f'Failed loading - {filepath}')
            return []

        return data_loaded


def make_sure_path_exists(path):
    import errno

    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def hash_file(file, hash_obj):
    if not os.path.exists(file):
        return ''
    with open(file, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            hash_obj.update(byte_block)
    return hash_obj.hexdigest()


def get_md5sum(file):
    import hashlib
    return hash_file(file, hashlib.md5())


def get_sha1sum(file):
    import hashlib
    return hash_file(file, hashlib.sha1())


def get_sha256sum(file):
    import hashlib
    return hash_file(file, hashlib.sha256())


def download_https_file(d, cwd, url, file, cookie_file, netrc, md5, sha1, sha256):
    download_filepath = os.path.join(cwd, file)

    sha256_file = os.path.join(cwd, file + '.sha256')
    if compare_sha256(str(download_filepath), str(sha256_file)):
        bb.note("%s exists, skipping download" % download_filepath)
        return True

    if os.path.exists(download_filepath):
        if md5:
            # don't download if md5 is good
            if md5 == get_md5sum(str(download_filepath)):
                bb.note("** Using %s" % download_filepath)
                return True
            else:
                os.remove(download_filepath)
        elif sha1:
            # don't download if sha1 is good
            if sha1 == get_sha1sum(str(download_filepath)):
                bb.note("** Using %s" % download_filepath)
                return True
            else:
                os.remove(str(download_filepath))
        elif sha256:
            # don't download if sha256 is good
            if sha256 == get_sha256sum(str(download_filepath)):
                bb.note("** Using %s" % download_filepath)
                return True
            else:
                os.remove(str(download_filepath))

    bb.note("** Downloading %s via %s" % (file, url))
    res = fetch_https_binary_file(
        url, download_filepath, False, None, cookie_file, netrc)
    if not res:
        os.remove(download_filepath)
        bb.error("Failed to download %s" % file)
        return False

    if os.path.exists(download_filepath):
        if md5:
            expected_md5 = get_md5sum(str(download_filepath))
            if md5 != expected_md5:
                bb.fatal(f'Download artifact {download_filepath} md5: {md5} does not match expected: {expected_md5}')
        elif sha1:
            expected_sha1 = get_sha1sum(str(download_filepath))
            if sha1 != expected_sha1:
                bb.fatal('Download artifact {download_filepath} sha1: {md5} does not match expected: {expected_sha1}')
        elif sha256:
            expected_sha256 = get_sha256sum(str(download_filepath))
            if sha256 != expected_sha256:
                bb.fata('Download artifact {download_filepath} sha256: {sha256} does not match expected: {expected_sha256}')

    write_sha256_file(cwd, file)
    return True


def compare_sha256(archive_path, sha256_file):
    if not os.path.exists(archive_path):
        return False

    if not os.path.exists(sha256_file):
        return False

    archive_sha256_val = get_sha256sum(archive_path)

    with open(sha256_file, 'r') as f:
        sha256_file_val = f.read().replace('\n', '')

        if archive_sha256_val == sha256_file_val:
            return True

    return False


def write_sha256_file(cwd, filename):
    file = os.path.join(cwd, filename)
    sha256_val = get_sha256sum(file)
    sha256_file = os.path.join(cwd, filename + '.sha256')

    with open(sha256_file, 'w+') as f:
        import fcntl
        fcntl.lockf(f, fcntl.LOCK_EX)
        f.write(sha256_val)
        fcntl.lockf(f, fcntl.LOCK_UN)


def fetch_https_progress(download_t, download_d, _upload_t, _upload_d):
    """
    callback function for pycurl.XFERINFOFUNCTION
    """
    from sys import stderr as stream
    stream.write('.')
    stream.flush()


def fetch_https_binary_file(url, filename, redirect, headers, cookie_file, netrc):
    """
    Fetches binary file via HTTPS
    """
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

    if cookie_file:
        cookie_file = os.path.expandvars(cookie_file)
        print("Using cookie file: %s" % cookie_file)
        c.setopt(pycurl.COOKIEFILE, cookie_file)

    if netrc:
        c.setopt(pycurl.NETRC, 1)

    while retries_left > 0:
        try:
            with open(filename, 'wb') as f:
                import fcntl
                fcntl.lockf(f, fcntl.LOCK_EX)
                c.setopt(pycurl.WRITEFUNCTION, f.write)
                c.perform()
                fcntl.lockf(f, fcntl.LOCK_UN)

            success = True
            break

        except pycurl.error:
            retries_left -= 1
            time.sleep(delay_between_retries)

    status = c.getinfo(pycurl.HTTP_CODE)

    c.close()
    os.sync()

    if not redirect and status == 302:
        bb.warn("Download Status: %d" % status)
        return False
    if not status == 200:
        bb.warn("Download Status: %d" % status)
        return False

    return success


def pubspec_hosted_archive_exists(name, url, version, archive_path):
    """
    Check if hosted files exists
    """
    from urllib.parse import urlparse

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(archive_path, url_parse_res.hostname)

    archive_file = os.path.join(hostname_path, name + '-' + version + '.tar.gz')

    if os.path.exists(archive_file):
        return True

    return False


def pubspec_archive_hosted(d, package_name, package, archive_path):
    """
    Archive hosted package
    """
    from urllib.parse import unquote
    from urllib.parse import urlparse

    from datetime import datetime

    bb.note(f'archiving hosted package: {package_name}')
    bb.debug(1, f'{package}')

    version = package['version']

    description = package['description']
    if not isinstance(description, dict):
        bb.error('Invalid description')
        return

    desc_name = description['name']
    url = description['url']

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(archive_path, url_parse_res.hostname)
    hostname_cache_path = os.path.join(hostname_path, '.cache')

    #
    # Check advisories
    #
    advisories = pubspec_get_package_advisories(package_name, url)
    # '{"advisories":[],"advisoriesUpdated":null}'
    if len(advisories) > 42:
        bb.debug(1, f'{package_name} has advisories')
        advisories_file_path = os.path.join(hostname_cache_path, package_name + '-advisories.json')
        with open(advisories_file_path, 'w') as f:
            import fcntl
            fcntl.lockf(f, fcntl.LOCK_EX)
            f.write(advisories)
            fcntl.lockf(f, fcntl.LOCK_UN)

    #
    # Fetch {package}-versions.json file
    #
    version_file_path = os.path.join(hostname_cache_path, package_name + '-versions.json')
    if not os.path.exists(version_file_path):
        make_sure_path_exists(hostname_cache_path)

        versions = pubspec_get_package_versions(package_name, url)
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")

        versions = versions[:-1] + ',"_fetchedAt":"' + timestamp + '"}'

        with open(version_file_path, 'w') as f:
            import fcntl
            fcntl.lockf(f, fcntl.LOCK_EX)
            f.write(versions)
            fcntl.lockf(f, fcntl.LOCK_UN)

    #
    # Fetch archive file
    #
    if not pubspec_hosted_archive_exists(desc_name, url, version, archive_path):

        versions = get_versions_obj(version_file_path)

        package = None
        for v in versions['versions']:
            if v['version'] == version:
                package = v

        if not package:
            bb.warn(f'{package_name}: {version} not found in {version_file_path}')

        else:
            url = package['archive_url']
            bb.debug(1, f'url: {url}')
            sha256 = package['archive_sha256']
            url_parse_res = urlparse(url)

            file = unquote(os.path.basename(url_parse_res.path))

            archive_file = os.path.join(hostname_path, file)
            if not os.path.exists(archive_file):
                download_https_file(d, hostname_path, url, file, None, None, None, None, sha256)


def sha1_hash(to_hash):
    import hashlib
    try:
        message_digest = hashlib.sha1()
        message_digest.update(bytes(to_hash, 'utf'))
        return message_digest.hexdigest()
    except TypeError:
        raise "String to hash was not compatible"


def pubspec_archive_git(package_name, package, project_dir, archive_path):
    """
    Archive git package
    """
    from urllib.parse import urlparse
    bb.debug(1, f'package_name: {package_name}')
    bb.debug(1, f'package: {package}')
    bb.debug(1, f'project_dir: {project_dir}')
    bb.debug(1, f'archive_path: {archive_path}')

    bb.debug(1, f'archiving git: {package_name}')
    bb.debug(1, f'{package}')

    description = package['description']
    bb.debug(1, f'description: {description}')

    url = description['url']
    bb.debug(1, f'url: {url}')

    resolved_ref = description['resolved-ref']
    bb.debug(1, f'resolved_ref: {resolved_ref}')

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]
    bb.debug(1, f'repo_name: {repo_name}')

    git_archive_path = os.path.join(archive_path, 'git', 'cache')
    bb.debug(1, f'git_archive_path: {git_archive_path}')
    make_sure_path_exists(git_archive_path)

    git_folder_name = repo_name + '-' + sha1_hash(url)
    bb.debug(1, f'git_folder_name: {git_folder_name}')

    git_folder = os.path.join(git_archive_path, git_folder_name)
    bb.debug(1, f'git_folder: {git_folder}')
    if os.path.exists(git_folder):
        bb.debug(1, f'git_folder exists: {git_folder}')
        return

    run_command(f'git clone --mirror {url} {git_folder}', project_dir)
    run_command(f'git --git-dir={git_folder} rev-list --max-count=1 {resolved_ref}', git_folder)

    # this fails if the pubspec.yaml is not in the root...
    # run_command(f'git --git-dir={git_folder} show {resolved_ref}:pubspec.yaml', git_folder)


def pubspec_archive_package(d, name, package, project_path, archive_path):
    """
    Fetches an archive file if source is hosted, and not already present in archive
    """
    bb.debug(1, f'pubspec_archive_package: name: {name}')
    bb.debug(1, f'pubspec_archive_package: package: {package}')
    bb.debug(1, f'pubspec_archive_package: project_path: {project_path}')
    bb.debug(1, f'pubspec_archive_package: archive_path: {archive_path}')

    source = package.get('source', '')
    if source == '':
        bb.debug(1, f'Skipping: {package}')
        return
    elif source == 'sdk':
        bb.debug(1, f'Skipping: {package}')
        return
    elif source == 'path':
        bb.debug(1, f'Skipping: {package}')
        return
    elif source == 'git':
        pubspec_archive_git(name, package, project_path, archive_path)
        return
    elif source == 'hosted':
        pubspec_archive_hosted(d, name, package, archive_path)
        return
    else:
        bb.error(f'{name}: Unknown source type: {source}')
        return


def pubspec_archive_packages_in_lock_file(d, base_path, archive_path, walk):
    """
    Archive all pubspec packages in a given 'pubspec.lock' file
    """
    bb.debug(1, 'pubspec_archive_packages_in_lock_file++')

    bb.debug(1, f'pubspec_archive_packages_in_lock_file: base_path: {base_path}')
    bb.debug(1, f'pubspec_archive_packages_in_lock_file: archive_path: {archive_path}')
    bb.debug(1, f'pubspec_archive_packages_in_lock_file: walk: {walk}')

    for dir_path, _, filenames in os.walk(base_path):
        if 'pubspec.yaml' in filenames and 'pubspec.lock' not in filenames:
            # create lock file if missing
            run_command('dart pub get', dir_path)
        if not walk:
            break

    for dir_path, _, filenames in os.walk(base_path):
        if 'pubspec.lock' in filenames:

            bb.debug(1, f'Archiving {dir_path}/pubspec.lock')
            bb.note(f'Archiving {dir_path}/pubspec.lock')

            # iterate pubspec.lock file
            pubspec_lock = get_yaml_obj(f'{dir_path}/pubspec.lock')
            packages = pubspec_lock['packages']

            for name in packages:
                bb.debug(1, f'name: {name}')
                pubspec_archive_package(d, name, packages[name], dir_path, archive_path)
            break

        else:
            if not walk:
                break

    bb.debug(1, 'pubspec_archive_packages_in_lock_file--')


def pubspec_get_package_advisories(name, url):
    """
    Return version dict for a specified description
    """
    import io
    import pycurl
    from urllib.parse import quote

    if url == '':
        bb.warn(f'Missing url in description, using default')
        url = "https://pub.dev"

    endpoint = '/api/packages/' + name + '/advisories'
    url = url + quote(endpoint)
    bb.debug(1, f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return buffer.getvalue().decode('utf-8')


def pubspec_get_package_versions(name, url):
    """
    Return version dict for a specified description
    """
    import io
    import pycurl
    from urllib.parse import quote

    if url == '':
        bb.warn(f'Missing url in description, using default')
        url = "https://pub.dev"

    endpoint = '/api/packages/' + name
    url = url + quote(endpoint)
    bb.debug(1, f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return buffer.getvalue().decode('utf-8')


def pubspec_get_package_version(d, desc_name, desc_url, version):
    """
    Return version info for package
    """
    import io
    import json
    import pycurl

    from urllib.parse import quote

    if desc_url == '':
        bb.note(f'Missing url in description, using default')
        desc_url = "https://pub.dev"

    endpoint = '/api/packages/' + desc_name + '/versions/' + version
    url = desc_url + quote(endpoint)
    bb.note(f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return json.loads(buffer.getvalue().decode('utf-8'))


def setup_environment():
    env = os.environ

    pub_cache = d.getVar("PUB_CACHE")
    env['PUB_CACHE'] = pub_cache

    workdir = d.getVar("WORKDIR")

    # required for dart: https://github.com/dart-lang/sdk/issues/41560
    env['HOME'] = f'{workdir}'

    # required for flutter: https://github.com/flutter/flutter/issues/59430
    env['XDG_CONFIG_HOME'] = f'{workdir}'

    http_proxy = d.getVar('http_proxy')
    if http_proxy != None:
        env['http_proxy']  = f'{http_proxy}'

    https_proxy = d.getVar('https_proxy')
    if https_proxy != None:
        env['https_proxy'] = f'{https_proxy}'

    http_proxy_ = d.getVar('HTTP_PROXY')
    if http_proxy_ != None:
        env['HTTP_PROXY']  = f'{http_proxy_}'

    https_proxy_ = d.getVar('HTTPS_PROXY')
    if https_proxy_ != None:
        env['HTTPS_PROXY'] = f'{https_proxy_}'

    env['NO_PROXY']        = 'localhost,127.0.0.1,::1'

    flutter_sdk_path = d.getVar("FLUTTER_SDK_PATH")
    env['FLUTTER_SDK_PATH'] = flutter_sdk_path

    env['PATH'] = f'{env["PATH"]}:{flutter_sdk_path}/bin'

    staging_dir_target = d.getVar('STAGING_DIR_TARGET')
    env['PKG_CONFIG_PATH'] = f'{staging_dir_target}/usr/lib/pkgconfig:{staging_dir_target}/usr/share/pkgconfig:{d.getVar("PKG_CONFIG_PATH")}'

    os.environ = env

    bb.debug(1, f'environment: {os.environ}')


addtask archive_pub_cache before restore_pub_cache after do_patch
do_archive_pub_cache[network] = "1"
do_archive_pub_cache[dirs] = "${WORKDIR} ${DL_DIR}"
do_archive_pub_cache[depends] += " \
    flutter-sdk-native:do_populate_sysroot \
    git-native:do_populate_sysroot \
    python3-pyyaml-native:do_populate_sysroot \
    python3-pycurl-native:do_populate_sysroot \
    "
python do_archive_pub_cache() {

    #
    # Application
    #

    app_root = os.path.join(d.getVar("S"), d.getVar("FLUTTER_APPLICATION_PATH"))

    #
    # Archive Project packages
    #
    pub_cache_archive_path = str(os.path.join(d.getVar("DL_DIR"), 'pub_cache'))
    make_sure_path_exists(pub_cache_archive_path)

    flutter_sdk_path = os.environ['FLUTTER_SDK_PATH']

    pubspec_archive_packages_in_lock_file(d, os.path.join(flutter_sdk_path, 'bin', 'cache', 'dart-sdk', 'pkg', 'macros'), pub_cache_archive_path, False)
    pubspec_archive_packages_in_lock_file(d, os.path.join(flutter_sdk_path, 'packages'), pub_cache_archive_path, True)
    pubspec_archive_packages_in_lock_file(d, app_root, pub_cache_archive_path, False)
}


def pubspec_restore_git_archive(name, package, project_path, pub_cache, archive_path):
    """
    Restore git archive
    """
    import shutil

    from urllib.parse import urlparse

    description = package['description']
    if not isinstance(description, dict):
        bb.error('description in not a dict')
        return

    bb.note(f'Restoring git: {name}')

    pub_cache_git_path = os.path.join(pub_cache, 'git')
    pub_cache_git_cache_path = os.path.join(pub_cache_git_path, 'cache')
    make_sure_path_exists(pub_cache_git_cache_path)

    resolved_ref = description['resolved-ref']
    url = description['url']

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]

    src_git_folder_name = repo_name + '-' + sha1_hash(url)
    pub_cache_git_archive_path = os.path.join(archive_path, 'git')
    pub_cache_git_cache_archive_path = os.path.join(pub_cache_git_archive_path, 'cache')
    src_path = os.path.join(pub_cache_git_cache_archive_path, src_git_folder_name)
    dest_path = os.path.join(pub_cache_git_cache_path, src_git_folder_name)

    if not os.path.exists(dest_path):
        shutil.copytree(src_path, dest_path)

    src_path = os.path.join(pub_cache_git_cache_path, src_git_folder_name)
    dest_path = os.path.join(pub_cache_git_path, repo_name + '-' + resolved_ref)
    if not os.path.exists(dest_path):
        run_command(f'git clone {src_path} {dest_path}', project_path)

    run_command(f'git checkout {resolved_ref}', dest_path)


def pubspec_restore_hosted_archive(package_name, package, pub_cache, archive_path):
    """
    Restore Hosted Archive
    """
    import shutil

    from urllib.parse import urlparse

    bb.note(f'Restoring hosted: {package_name}')

    version = package['version']

    description = package['description']
    name = description['name']
    url = description['url']
    url_parsed = urlparse(url)

    hosted_path = os.path.join(pub_cache, 'hosted')
    hosted_cache_path = os.path.join(hosted_path, url_parsed.hostname, '.cache')
    make_sure_path_exists(hosted_cache_path)

    #
    # {package}-advisories.json
    #

    file = os.path.join(archive_path, url_parsed.hostname, '.cache', name + '-advisories.json')
    if os.path.exists(file):
        shutil.copy(str(file), hosted_cache_path)

    #
    # {package}-versions.json
    #

    file = os.path.join(archive_path, url_parsed.hostname, '.cache', name + '-versions.json')
    if not os.path.exists(file):
        bb.error(f'Missing: {file}')
        raise FileNotFoundError

    shutil.copy(str(file), hosted_cache_path)

    #
    # archive name
    #

    filename = name + '-' + version + '.tar.gz'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        bb.error(f'Missing: {file}')
        raise FileNotFoundError

    restore_folder = os.path.join(hosted_path, url_parsed.hostname, name + '-' + version)
    make_sure_path_exists(restore_folder)
    run_command(f'tar -xzf {file} -C {restore_folder}', restore_folder)

    #
    # .sha256 file
    #
    hosted_hashes_path = os.path.join(pub_cache, 'hosted-hashes')

    filename += '.sha256'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        bb.error(f'Missing: {file}')
        raise FileNotFoundError

    bare_filename = filename[:-14]
    hostname_path = os.path.join(hosted_hashes_path, url_parsed.hostname)
    make_sure_path_exists(hostname_path)
    file_dest = os.path.join(hostname_path, bare_filename + '.sha256')

    bb.debug(1, f'sha256: {file_dest}')
    shutil.copy(str(file), file_dest)


def pubspec_restore_project_pub_cache(d, base_path, archive_path, walk):
    """
    Create pub cache for specified project path
    """
    pub_cache_path = env['PUB_CACHE']
    if not pub_cache_path:
        bb.error("PUB_CACHE is not set")
        return

    for dir_path, _, filenames in os.walk(base_path):
        for filename in filenames:
            if filename == 'pubspec.lock':
                if not os.path.exists(archive_path):
                    bb.error(f'Path does not exist: {archive_path}')
                    raise FileNotFoundError

                bb.note(f'Restoring {dir_path}/{filename} to {pub_cache_path}')

                pub_cache = os.environ.get('PUB_CACHE', None)
                if not pub_cache:
                    bb.error('PUB_CACHE is not set.  Cannot restore')
                    continue

                packages = get_yaml_obj(f'{dir_path}/{filename}')['packages']
                for name in packages:
                    package = packages[name]

                    source = package['source']
                    bb.debug(1, f'package {name}: source: {source}')
                    if source == 'sdk':
                        bb.debug(1, 'Skipping')
                        continue
                    elif source == 'path':
                        bb.debug(1, 'Skipping')
                        continue
                    elif source == 'git':
                        pubspec_restore_git_archive(name, package, dir_path, pub_cache, archive_path)
                        continue
                    elif source == 'hosted':
                        pubspec_restore_hosted_archive(name, package, pub_cache, archive_path)
                        continue
                    else:
                        bb.error(f'{name}: Unknown source type: {source}')
                        continue
                break
        if not walk:
            break


#
# Restore Pub Cache
#

addtask restore_pub_cache before do_configure after do_archive_pub_cache
do_restore_pub_cache[dirs] = "${WORKDIR} ${DL_DIR}"
do_restore_pub_cache[depends] += " \
    git-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    "
python do_restore_pub_cache() {

    import shutil

    #
    # Application
    #

    app_root = os.path.join(d.getVar("S"), d.getVar("FLUTTER_APPLICATION_PATH"))
    archive_path = os.path.join(d.getVar("DL_DIR"), 'pub_cache')

    # Restore pub cache
    shutil.rmtree(pub_cache, ignore_errors=True)

    flutter_sdk_path = os.environ['FLUTTER_SDK_PATH']

    pubspec_restore_project_pub_cache(d, os.path.join(flutter_sdk_path, 'bin', 'cache', 'dart-sdk', 'pkg', 'macros'), archive_path, False)
    pubspec_restore_project_pub_cache(d, os.path.join(flutter_sdk_path, 'packages'), archive_path, True)
    pubspec_restore_project_pub_cache(d, app_root, archive_path, False)

    # finalize
    run_command('dart pub get --offline --enforce-lockfile -v', app_root)
}

#
# Compile
#
python do_compile() {
    """
    Compiles project
    """

    pubspec_yaml_appname = get_pubspec_yaml_appname(d)
    pubspec_appname = d.getVar("PUBSPEC_APPNAME")
    if pubspec_appname != pubspec_yaml_appname:
        bb.fatal("Set PUBSPEC_APPNAME to match name value in pubspec.yaml")

    source_dir = d.getVar('S')
    flutter_application_path = d.getVar('FLUTTER_APPLICATION_PATH')
    source_root = os.path.join(source_dir, flutter_application_path)
    cmd = d.getVar('FLUTTER_PREBUILD_CMD')
    if cmd != '':
        run_command(cmd, source_root)

    build_type = d.getVar('BUILD_TYPE')

    if build_type == 'web':
        build_web(d, source_root)

    elif build_type == 'app':
        build_app(d, source_dir, pubspec_appname, flutter_application_path, staging_dir_target, source_root)
}


def build_web(d, source_root):
    """
    Build web app
    """

    build_folder = os.path.join(source_root, 'build')
    if os.path.exists(build_folder):
        run_command('flutter clean', source_root)

    run_command('flutter config --no-cli-animations', source_root)
    run_command('flutter pub get --offline --enforce-lockfile -v', source_root)

    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')
    run_command(f'flutter build web {flutter_build_args}', source_root)

    bb.note(f'Flutter build web {flutter_build_args}: Completed')


def version_tuple(v):
    """
    Return a version tuple
    """
    return tuple(map(int, (v.split("."))))


def build_app(d, source_dir, pubspec_appname, flutter_application_path, staging_dir_target, source_root):
    """
    Builds flutter application
    """
    import glob
    import shutil

    # determine build type based on what flutter-engine installed.
    datadir = d.getVar('datadir')
    flutter_sdk_version = d.getVar('FLUTTER_SDK_VERSION')
    flutter_runtime_modes = os.listdir(f'{staging_dir_target}{datadir}/flutter/{flutter_sdk_version}')

    flutter_app_runtime_modes = d.getVar('FLUTTER_APP_RUNTIME_MODES')
    flutter_build_args = d.getVar('FLUTTER_BUILD_ARGS')

    new_build_scheme = False
    if version_tuple(flutter_sdk_version) >= version_tuple('3.24.0'):
        bb.note(f'Using new build scheme')
        new_build_scheme = True

    flutter_sdk_path = os.environ['FLUTTER_SDK_PATH']

    for runtime_mode in flutter_runtime_modes:
        if runtime_mode not in flutter_app_runtime_modes:
            bb.note(f'Skipping build for: {runtime_mode}')
            continue

        bb.note(f'[{runtime_mode}] flutter build {flutter_build_args}: Starting')

        build_folder = os.path.join(source_root, 'build')
        if os.path.exists(build_folder):
            run_command('flutter clean -v', source_root)

        run_command('flutter config --no-cli-animations', source_root)
        run_command('flutter pub get --offline --enforce-lockfile -v', source_root)

        if runtime_mode == 'jit_release':
            cmd = f'flutter build {flutter_build_args} --local-engine -v'
            run_command(cmd, source_root)
        else:
            cmd = f'flutter build {flutter_build_args} -v'
            run_command(cmd, source_root)

        bb.note(f'[{runtime_mode}] flutter build {flutter_build_args}: Completed')

        if runtime_mode == 'release' or runtime_mode == 'profile':

            bb.note(f'kernel_snapshot_{runtime_mode}: Starting')

            flutter_app_sdk_root = f'{flutter_sdk_path}/bin/cache/artifacts/engine/common/flutter_patched_sdk/'
            flutter_app_vm_product = "false"
            if runtime_mode == 'release':
                flutter_app_sdk_root = f'{flutter_sdk_path}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/'
                flutter_app_vm_product = "true"

            flutter_app_profile_flags = ''
            flutter_app_vm_profile = 'false'
            if runtime_mode == 'profile':
                flutter_app_profile_flags = '--track-widget-creation' 
                flutter_app_vm_profile = 'true'

            flutter_app_debug_flags = ''
            dill_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/app.dill')
            bb.note(f'{dill_path}')
            flutter_app_output_dill = dill_path[0]
            if runtime_mode == 'debug':
                flutter_app_debug_flags = '--enable-asserts'

            flutter_source_file = ''
            flutter_source_package = ''
            flutter_source_defines = ''
            dart_plugin_registrant_file = f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/dart_plugin_registrant.dart'
            if os.path.isfile(dart_plugin_registrant_file):
                flutter_source_file = f'--source file://{dart_plugin_registrant_file}'
                flutter_source_package = '--source package:flutter/src/dart_plugin_registrant.dart'
                flutter_source_defines = f'-Dflutter.dart_plugin_registrant=file://{dart_plugin_registrant_file}'

            flutter_native_assets = ''
            yaml_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/native_assets.yaml')
            if os.path.isfile(yaml_path[0]):
                flutter_native_assets = f'--native-assets {yaml_path[0]}'

            app_aot_extra = d.getVar("APP_AOT_EXTRA")
            app_aot_entry_file = d.getVar("APP_AOT_ENTRY_FILE")

            package_config_json = f'{source_dir}/{flutter_application_path}/.dart_tool/package_config.json'

            if not new_build_scheme:
                dart_runtime = f'{flutter_sdk_path}/bin/cache/dart-sdk/bin/dart'
                frontend_snapshot = f'{flutter_sdk_path}/bin/cache/artifacts/engine/linux-{clang_build_arch(d)}/frontend_server.dart.snapshot'
                dep_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/kernel_snapshot.d')
            else:
                dart_runtime = f'{flutter_sdk_path}/bin/cache/dart-sdk/bin/dartaotruntime'
                frontend_snapshot = f'{flutter_sdk_path}/bin/cache/artifacts/engine/linux-{clang_build_arch(d)}/frontend_server_aot.dart.snapshot'
                dep_path = glob.glob(f'{source_dir}/{flutter_application_path}/.dart_tool/flutter_build/*/kernel_snapshot_program.d')

            cmd = f'{dart_runtime} \
                --disable-analytics \
                --disable-dart-dev \
                {frontend_snapshot} \
                --sdk-root {flutter_app_sdk_root} \
                --target=flutter \
                -Ddart.vm.profile={flutter_app_vm_profile} \
                -Ddart.vm.product={flutter_app_vm_product} \
                {app_aot_extra} \
                {flutter_app_debug_flags} \
                {flutter_app_profile_flags} \
                --aot \
                --tfa \
                --target-os linux \
                --packages {package_config_json} \
                --output-dill {flutter_app_output_dill} \
                --depfile {dep_path[0]} \
                {flutter_source_file} \
                {flutter_source_package} \
                {flutter_source_defines} \
                {flutter_native_assets} \
                --verbosity=error \
                package:{pubspec_appname}/{app_aot_entry_file}'

            run_command(cmd, source_root)

            bb.note(f'kernel_snapshot_{runtime_mode}: Complete')

            # remove kernel_blob.bin to save space
            try:
                os.remove(f'{source_dir}/{flutter_application_path}/build/flutter_assets/kernel_blob.bin')
            except OSError:
                pass

            # create empty file for apps that check for kernel_blob.bin
            run_command('touch build/flutter_assets/kernel_blob.bin', source_root)

            bb.note(f'aot_elf_{runtime_mode}: Started')

            #
            # Extract Engine SDK
            #
            shutil.rmtree(f'{source_dir}/engine_sdk', ignore_errors=True)

            staging_datadir = d.getVar('STAGING_DATADIR')
            cmd = f'unzip {staging_datadir}/flutter/{flutter_sdk_version}/{runtime_mode}/engine_sdk.zip \
                -d {source_dir}/engine_sdk'
            run_command(cmd, source_root)

            #
            # Create libapp.so
            #
            app_gen_snapshot_flags = d.getVar("APP_GEN_SNAPSHOT_FLAGS")
            cmd = f'{source_dir}/engine_sdk/sdk/clang_{clang_build_arch(d)}/gen_snapshot \
                --deterministic \
                --snapshot_kind=app-aot-elf \
                --elf=libapp.so \
                --strip \
                {app_gen_snapshot_flags} \
                {flutter_app_output_dill}'
            run_command(cmd, source_root)
