
DESCRIPTION = "LUCI services and tools in Go."

GO_IMPORT = "infra/go/src/go.chromium.org/luci"


inherit go native

SRC_URI = "\
    git://chromium.googlesource.com/chromium/tools/build.git;protocol=https;destsuffix=luci-go-${PV}/src/build/;branch=main;name=chromium_googlesource_com_chromium_tools_build_git \
    git://chromium.googlesource.com/chromium/tools/depot_tools.git;protocol=https;destsuffix=luci-go-${PV}/src/depot_tools/;branch=main;name=chromium_googlesource_com_chromium_tools_depot_tools_git \
    git://chromium.googlesource.com/infra/infra.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/;branch=main;name=chromium_googlesource_com_infra_infra_git \
    git://chromium.googlesource.com/external/github.com/twbs/bootstrap.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/bootstrap/;branch=main;name=chromium_googlesource_com_external_github_com_twbs_bootstrap_git \
    git://chromium.googlesource.com/external/github.com/GoogleCloudPlatform/appengine-gcs-client.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/cloudstorage/;name=chromium_googlesource_com_external_github_com_GoogleCloudPlatform_appengine_gcs_client_git \
    git://chromium.googlesource.com/external/code.launchpad.net/dateutil;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/dateutil/;name=chromium_googlesource_com_external_code_launchpad_net_dateutil \
    git://chromium.googlesource.com/external/github.com/qiao/difflib.js.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/difflibjs/;name=chromium_googlesource_com_external_github_com_qiao_difflib_js_git \
    git://chromium.googlesource.com/external/github.com/GoogleCloudPlatform/endpoints-proto-datastore.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/endpoints-proto-datastore/;name=chromium_googlesource_com_external_github_com_GoogleCloudPlatform_endpoints_proto_datastore_git \
    git://chromium.googlesource.com/external/code.google.com/p/gae-pytz;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/gae-pytz/;name=chromium_googlesource_com_external_code_google_com_p_gae_pytz \
    git://chromium.googlesource.com/external/github.com/google/google-api-python-client.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/google-api-python-client/;name=chromium_googlesource_com_external_github_com_google_google_api_python_client_git \
    git://chromium.googlesource.com/external/github.com/jcgregorio/httplib2.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/httplib2/;name=chromium_googlesource_com_external_github_com_jcgregorio_httplib2_git \
    git://chromium.googlesource.com/infra/third_party/npm_modules.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/npm_modules/;name=chromium_googlesource_com_infra_third_party_npm_modules_git \
    git://chromium.googlesource.com/external/github.com/google/oauth2client.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/oauth2client/;name=chromium_googlesource_com_external_github_com_google_oauth2client_git \
    git://chromium.googlesource.com/external/github.com/GoogleCloudPlatform/appengine-pipelines.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/pipeline/;branch=chromium;name=chromium_googlesource_com_external_github_com_GoogleCloudPlatform_appengine_pipelines_git \
    git://chromium.googlesource.com/external/github.com/benjaminp/six.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/six/;branch=upstream/master;name=chromium_googlesource_com_external_github_com_benjaminp_six_git \
    git://chromium.googlesource.com/external/github.com/uri-templates/uritemplate-py.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/appengine/third_party/uritemplate/;name=chromium_googlesource_com_external_github_com_uri_templates_uritemplate_py_git \
    git://chromium.googlesource.com/chromiumos/config;protocol=https;destsuffix=luci-go-${PV}/src/infra/go/src/go.chromium.org/chromiumos/config/;branch=main;name=chromium_googlesource_com_chromiumos_config \
    git://chromium.googlesource.com/chromiumos/infra/proto;protocol=https;destsuffix=luci-go-${PV}/src/infra/go/src/go.chromium.org/chromiumos/infra/proto/;branch=main;name=chromium_googlesource_com_chromiumos_infra_proto \
    git://chromium.googlesource.com/infra/luci/luci-go;protocol=https;destsuffix=luci-go-${PV}/src/${GO_IMPORT};branch=main;name=chromium_googlesource_com_infra_luci_luci_go \
    git://chromium.googlesource.com/infra/luci/luci-py;protocol=https;destsuffix=luci-go-${PV}/src/infra/luci/;branch=main;name=chromium_googlesource_com_infra_luci_luci_py \
    git://chromium.googlesource.com/infra/testing/expect_tests.git;protocol=https;destsuffix=luci-go-${PV}/src/infra/packages/expect_tests/;branch=main;name=chromium_googlesource_com_infra_testing_expect_tests_git \
    git://chromium.googlesource.com/infra/luci/recipes-py;protocol=https;destsuffix=luci-go-${PV}/src/infra/recipes-py/;branch=main;name=chromium_googlesource_com_infra_luci_recipes_py \
    "

SRCREV_chromium_googlesource_com_chromium_tools_build_git = "514fc4b9f9fb0fd84e34b9e9918808604f2ce9aa"
SRCREV_chromium_googlesource_com_chromium_tools_depot_tools_git = "9ce8be33399a0f4ad2f58c15489a2172a7f94952"
SRCREV_chromium_googlesource_com_infra_infra_git = "efbbe164317b73d4c3f062684b3639af61279053"
SRCREV_chromium_googlesource_com_external_github_com_twbs_bootstrap_git = "b4895a0d6dc493f17fe9092db4debe44182d42ac"
SRCREV_chromium_googlesource_com_external_github_com_GoogleCloudPlatform_appengine_gcs_client_git = "76162a98044f2a481e2ef34d32b7e8196e534b78"
SRCREV_chromium_googlesource_com_external_code_launchpad_net_dateutil = "8c6026ba09716a4e164f5420120bfe2ebb2d9d82"
SRCREV_chromium_googlesource_com_external_github_com_qiao_difflib_js_git = "e11553ba3e303e2db206d04c95f8e51c5692ca28"
SRCREV_chromium_googlesource_com_external_github_com_GoogleCloudPlatform_endpoints_proto_datastore_git = "971bca8e31a4ab0ec78b823add5a47394d78965a"
SRCREV_chromium_googlesource_com_external_code_google_com_p_gae_pytz = "4d72fd095c91f874aaafb892859acbe3f927b3cd"
SRCREV_chromium_googlesource_com_external_github_com_google_google_api_python_client_git = "49d45a6c3318b75e551c3022020f46c78655f365"
SRCREV_chromium_googlesource_com_external_github_com_jcgregorio_httplib2_git = "058a1f9448d5c27c23772796f83a596caf9188e6"
SRCREV_chromium_googlesource_com_infra_third_party_npm_modules_git = "f83fafaa22f5ff396cf5306285ca3806d1b2cf1b"
SRCREV_chromium_googlesource_com_external_github_com_google_oauth2client_git = "e8b1e794d28f2117dd3e2b8feeb506b4c199c533"
SRCREV_chromium_googlesource_com_external_github_com_GoogleCloudPlatform_appengine_pipelines_git = "58cf59907f67db359fe626ee06b6d3ac448c9e15"
SRCREV_chromium_googlesource_com_external_github_com_benjaminp_six_git = "65486e4383f9f411da95937451205d3c7b61b9e1"
SRCREV_chromium_googlesource_com_external_github_com_uri_templates_uritemplate_py_git = "1e780a49412cdbb273e9421974cb91845c124f3f"
SRCREV_chromium_googlesource_com_chromiumos_config = "46ec3fd3c3b0ecafffcba6ce3389c5f282d69709"
SRCREV_chromium_googlesource_com_chromiumos_infra_proto = "0ed6eee834301ea8d41750998494f527494dd406"
SRCREV_chromium_googlesource_com_infra_luci_luci_go = "6fe1639270fc8b347c71ce59b8af28c059d85d6a"
SRCREV_chromium_googlesource_com_infra_luci_luci_py = "96cccf0eee3e4f73cefb5b0cf503234c9baadc60"
SRCREV_chromium_googlesource_com_infra_testing_expect_tests_git = "eae70af12019781088e586ded8891055471233c7"
SRCREV_chromium_googlesource_com_infra_luci_recipes_py = "52ef3060ed5a4f75ef6e4056fb36aaf7274bc291"

LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "file://src/infra/go/src/go.chromium.org/luci/LICENSE;md5=9aab4346540414bf70054c0db9cdd293"

do_compile() {
    cd src/infra/go/src/go.chromium.org/luci
    go build -v -o "${WORKDIR}/build/bin/cipd" go.chromium.org/luci/cipd/client/cmd/cipd
    go clean -modcache
}

do_install() {
    install -d ${D}${bindir}
    cp ${WORKDIR}/build/bin/cipd ${D}${bindir}
}

FILES_${PN} = "${bindir}"

BBCLASSEXTEND += "native nativesdk"