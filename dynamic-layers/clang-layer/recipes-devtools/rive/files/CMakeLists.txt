#
# Copyright 2024 Joel Winarske
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.10.2)

if (NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE "Release" CACHE STRING "Choose the type of build, options are: Debug, Release, RelWithDebInfo, or MinSizeRel." FORCE)
    message(STATUS "CMAKE_BUILD_TYPE not set, defaulting to Release.")
endif ()

project(rive-text
        VERSION "0.3.0"
        DESCRIPTION "Rive Text Library"
        LANGUAGES CXX C
)

set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_EXTENSIONS OFF)

string(APPEND CMAKE_C_FLAGS " -Wall -ansi -pedantic -Wno-unused-function -Wno-unused-variable")
string(APPEND CMAKE_C_FLAGS " -fno-exceptions -fno-rtti -fno-unwind-tables")
string(APPEND CMAKE_C_FLAGS " -flto=full")
string(APPEND CMAKE_C_LINKER_FLAGS " -flto=full")

string(APPEND CMAKE_CXX_FLAGS " -fno-exceptions -fno-rtti -fno-unwind-tables")
string(APPEND CMAKE_CXX_FLAGS " -flto=full")
string(APPEND CMAKE_CXX_LINKER_FLAGS " -fno-exceptions -fno-rtti -fno-unwind-tables")
string(APPEND CMAKE_CXX_LINKER_FLAGS " -flto=full")

add_library(rive_sheenbidi STATIC
    third_party/sheenbidi/Source/BidiChain.c
    third_party/sheenbidi/Source/BidiTypeLookup.c
    third_party/sheenbidi/Source/BracketQueue.c
    third_party/sheenbidi/Source/GeneralCategoryLookup.c
    third_party/sheenbidi/Source/IsolatingRun.c
    third_party/sheenbidi/Source/LevelRun.c
    third_party/sheenbidi/Source/PairingLookup.c
    third_party/sheenbidi/Source/RunQueue.c
    third_party/sheenbidi/Source/SBAlgorithm.c
    third_party/sheenbidi/Source/SBBase.c
    third_party/sheenbidi/Source/SBCodepointSequence.c
    third_party/sheenbidi/Source/SBLine.c
    third_party/sheenbidi/Source/SBLog.c
    third_party/sheenbidi/Source/SBMirrorLocator.c
    third_party/sheenbidi/Source/SBParagraph.c
    third_party/sheenbidi/Source/SBScriptLocator.c
    third_party/sheenbidi/Source/ScriptLookup.c
    third_party/sheenbidi/Source/ScriptStack.c
    third_party/sheenbidi/Source/StatusStack.c
    third_party/sheenbidi/Source/SheenBidi.c
)

target_compile_definitions(rive_sheenbidi PRIVATE ANSI_DECLARATORS)

target_include_directories(rive_sheenbidi PUBLIC third_party/sheenbidi/Headers)

add_library(rive_text SHARED
    macos/rive-cpp/src/audio/audio_engine.cpp
    macos/rive-cpp/src/audio/audio_source.cpp
    macos/rive-cpp/src/audio/audio_reader.cpp
    macos/rive-cpp/src/audio/audio_sound.cpp
    macos/rive-cpp/src/renderer.cpp
    macos/rive-cpp/src/math/mat2d.cpp
    macos/rive-cpp/src/math/raw_path.cpp
    macos/rive-cpp/src/text/font_hb.cpp
    macos/rive-cpp/src/text/line_breaker.cpp
    macos/rive_text/rive_text.cpp
    macos/common.cpp
    third_party/harfbuzz/src/hb-aat-layout-ankr-table.hh
    third_party/harfbuzz/src/hb-aat-layout-bsln-table.hh
    third_party/harfbuzz/src/hb-aat-layout-common.hh
    third_party/harfbuzz/src/hb-aat-layout-feat-table.hh
    third_party/harfbuzz/src/hb-aat-layout-just-table.hh
    third_party/harfbuzz/src/hb-aat-layout-kerx-table.hh
    third_party/harfbuzz/src/hb-aat-layout-morx-table.hh
    third_party/harfbuzz/src/hb-aat-layout-opbd-table.hh
    third_party/harfbuzz/src/hb-aat-layout-trak-table.hh
    third_party/harfbuzz/src/hb-aat-layout.cc
    third_party/harfbuzz/src/hb-aat-layout.hh
    third_party/harfbuzz/src/hb-aat-ltag-table.hh
    third_party/harfbuzz/src/hb-aat-map.cc
    third_party/harfbuzz/src/hb-aat-map.hh
    third_party/harfbuzz/src/hb-aat.h
    third_party/harfbuzz/src/hb-algs.hh
    third_party/harfbuzz/src/hb-array.hh
    third_party/harfbuzz/src/hb-atomic.hh
    third_party/harfbuzz/src/hb-bimap.hh
    third_party/harfbuzz/src/hb-bit-page.hh
    third_party/harfbuzz/src/hb-bit-set-invertible.hh
    third_party/harfbuzz/src/hb-bit-set.hh
    third_party/harfbuzz/src/hb-blob.cc
    third_party/harfbuzz/src/hb-blob.hh
    third_party/harfbuzz/src/hb-buffer-deserialize-json.hh
    third_party/harfbuzz/src/hb-buffer-deserialize-text.hh
    third_party/harfbuzz/src/hb-buffer-serialize.cc
    third_party/harfbuzz/src/hb-buffer-verify.cc
    third_party/harfbuzz/src/hb-buffer.cc
    third_party/harfbuzz/src/hb-buffer.hh
    third_party/harfbuzz/src/hb-cache.hh
    third_party/harfbuzz/src/hb-cff-interp-common.hh
    third_party/harfbuzz/src/hb-cff-interp-cs-common.hh
    third_party/harfbuzz/src/hb-cff-interp-dict-common.hh
    third_party/harfbuzz/src/hb-cff1-interp-cs.hh
    third_party/harfbuzz/src/hb-cff2-interp-cs.hh
    third_party/harfbuzz/src/hb-common.cc
    third_party/harfbuzz/src/hb-config.hh
    third_party/harfbuzz/src/hb-debug.hh
    third_party/harfbuzz/src/hb-dispatch.hh
    third_party/harfbuzz/src/hb-draw.cc
    third_party/harfbuzz/src/hb-draw.h
    third_party/harfbuzz/src/hb-draw.hh
    third_party/harfbuzz/src/hb-face.cc
    third_party/harfbuzz/src/hb-face.hh
    third_party/harfbuzz/src/hb-font.cc
    third_party/harfbuzz/src/hb-font.hh
    third_party/harfbuzz/src/hb-iter.hh
    third_party/harfbuzz/src/hb-kern.hh
    third_party/harfbuzz/src/hb-machinery.hh
    third_party/harfbuzz/src/hb-map.cc
    third_party/harfbuzz/src/hb-map.hh
    third_party/harfbuzz/src/hb-meta.hh
    third_party/harfbuzz/src/hb-ms-feature-ranges.hh
    third_party/harfbuzz/src/hb-mutex.hh
    third_party/harfbuzz/src/hb-null.hh
    third_party/harfbuzz/src/hb-number-parser.hh
    third_party/harfbuzz/src/hb-number.cc
    third_party/harfbuzz/src/hb-number.hh
    third_party/harfbuzz/src/hb-object.hh
    third_party/harfbuzz/src/hb-open-file.hh
    third_party/harfbuzz/src/hb-open-type.hh
    third_party/harfbuzz/src/hb-ot-cff-common.hh
    third_party/harfbuzz/src/hb-ot-cff1-std-str.hh
    third_party/harfbuzz/src/hb-ot-cff1-table.cc
    third_party/harfbuzz/src/hb-ot-cff1-table.hh
    third_party/harfbuzz/src/hb-ot-cff2-table.cc
    third_party/harfbuzz/src/hb-ot-cff2-table.hh
    third_party/harfbuzz/src/hb-ot-cmap-table.hh
    third_party/harfbuzz/src/hb-ot-color-cbdt-table.hh
    third_party/harfbuzz/src/hb-ot-color-colr-table.hh
    third_party/harfbuzz/src/hb-ot-color-colrv1-closure.hh
    third_party/harfbuzz/src/hb-ot-color-cpal-table.hh
    third_party/harfbuzz/src/hb-ot-color-sbix-table.hh
    third_party/harfbuzz/src/hb-ot-color-svg-table.hh
    third_party/harfbuzz/src/hb-ot-color.cc
    third_party/harfbuzz/src/hb-ot-color.h
    third_party/harfbuzz/src/hb-ot-deprecated.h
    third_party/harfbuzz/src/hb-ot-face-table-list.hh
    third_party/harfbuzz/src/hb-ot-face.cc
    third_party/harfbuzz/src/hb-ot-face.hh
    third_party/harfbuzz/src/hb-ot-font.cc
    third_party/harfbuzz/src/hb-ot-gasp-table.hh
    third_party/harfbuzz/src/hb-ot-glyf-table.hh
    third_party/harfbuzz/src/hb-ot-hdmx-table.hh
    third_party/harfbuzz/src/hb-ot-head-table.hh
    third_party/harfbuzz/src/hb-ot-hhea-table.hh
    third_party/harfbuzz/src/hb-ot-hmtx-table.hh
    third_party/harfbuzz/src/hb-ot-kern-table.hh
    third_party/harfbuzz/src/hb-ot-layout-base-table.hh
    third_party/harfbuzz/src/hb-ot-layout-common.hh
    third_party/harfbuzz/src/hb-ot-layout-gdef-table.hh
    third_party/harfbuzz/src/hb-ot-layout-gpos-table.hh
    third_party/harfbuzz/src/hb-ot-layout-gsub-table.hh
    third_party/harfbuzz/src/hb-ot-layout-gsubgpos.hh
    third_party/harfbuzz/src/hb-ot-layout-jstf-table.hh
    third_party/harfbuzz/src/hb-ot-layout.cc
    third_party/harfbuzz/src/hb-ot-layout.hh
    third_party/harfbuzz/src/hb-ot-map.cc
    third_party/harfbuzz/src/hb-ot-map.hh
    third_party/harfbuzz/src/hb-ot-math-table.hh
    third_party/harfbuzz/src/hb-ot-math.cc
    third_party/harfbuzz/src/hb-ot-maxp-table.hh
    third_party/harfbuzz/src/hb-ot-meta-table.hh
    third_party/harfbuzz/src/hb-ot-meta.cc
    third_party/harfbuzz/src/hb-ot-meta.h
    third_party/harfbuzz/src/hb-ot-metrics.cc
    third_party/harfbuzz/src/hb-ot-metrics.hh
    third_party/harfbuzz/src/hb-ot-name-language-static.hh
    third_party/harfbuzz/src/hb-ot-name-language.hh
    third_party/harfbuzz/src/hb-ot-name-table.hh
    third_party/harfbuzz/src/hb-ot-name.cc
    third_party/harfbuzz/src/hb-ot-name.h
    third_party/harfbuzz/src/hb-ot-os2-table.hh
    third_party/harfbuzz/src/hb-ot-os2-unicode-ranges.hh
    third_party/harfbuzz/src/hb-ot-post-macroman.hh
    third_party/harfbuzz/src/hb-ot-post-table-v2subset.hh
    third_party/harfbuzz/src/hb-ot-post-table.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic-fallback.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic-joining-list.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic-pua.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic-table.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic-win1256.hh
    third_party/harfbuzz/src/hb-ot-shaper-arabic.cc
    third_party/harfbuzz/src/hb-ot-shaper-arabic.hh
    third_party/harfbuzz/src/hb-ot-shaper-default.cc
    third_party/harfbuzz/src/hb-ot-shaper-hangul.cc
    third_party/harfbuzz/src/hb-ot-shaper-hebrew.cc
    third_party/harfbuzz/src/hb-ot-shaper-indic-table.cc
    third_party/harfbuzz/src/hb-ot-shaper-indic.cc
    third_party/harfbuzz/src/hb-ot-shaper-indic.hh
    third_party/harfbuzz/src/hb-ot-shaper-khmer.cc
    third_party/harfbuzz/src/hb-ot-shaper-myanmar.cc
    third_party/harfbuzz/src/hb-ot-shaper-syllabic.cc
    third_party/harfbuzz/src/hb-ot-shaper-syllabic.hh
    third_party/harfbuzz/src/hb-ot-shaper-thai.cc
    third_party/harfbuzz/src/hb-ot-shaper-use-table.hh
    third_party/harfbuzz/src/hb-ot-shaper-use.cc
    third_party/harfbuzz/src/hb-ot-shaper-vowel-constraints.cc
    third_party/harfbuzz/src/hb-ot-shaper-vowel-constraints.hh
    third_party/harfbuzz/src/hb-ot-shaper.hh
    third_party/harfbuzz/src/hb-ot-shaper-indic-machine.hh
    third_party/harfbuzz/src/hb-ot-shaper-khmer-machine.hh
    third_party/harfbuzz/src/hb-ot-shaper-myanmar-machine.hh
    third_party/harfbuzz/src/hb-ot-shaper-use-machine.hh
    third_party/harfbuzz/src/hb-ot-shape-fallback.cc
    third_party/harfbuzz/src/hb-ot-shape-fallback.hh
    third_party/harfbuzz/src/hb-ot-shape-normalize.cc
    third_party/harfbuzz/src/hb-ot-shape-normalize.hh
    third_party/harfbuzz/src/hb-ot-shape.cc
    third_party/harfbuzz/src/hb-ot-shape.hh
    third_party/harfbuzz/src/hb-ot-stat-table.hh
    third_party/harfbuzz/src/hb-ot-tag-table.hh
    third_party/harfbuzz/src/hb-ot-tag.cc
    third_party/harfbuzz/src/hb-ot-var-avar-table.hh
    third_party/harfbuzz/src/hb-ot-var-common.hh
    third_party/harfbuzz/src/hb-ot-var-fvar-table.hh
    third_party/harfbuzz/src/hb-ot-var-gvar-table.hh
    third_party/harfbuzz/src/hb-ot-var-hvar-table.hh
    third_party/harfbuzz/src/hb-ot-var-mvar-table.hh
    third_party/harfbuzz/src/hb-ot-var.cc
    third_party/harfbuzz/src/hb-ot-vorg-table.hh
    third_party/harfbuzz/src/hb-pool.hh
    third_party/harfbuzz/src/hb-priority-queue.hh
    third_party/harfbuzz/src/hb-repacker.hh
    third_party/harfbuzz/src/hb-sanitize.hh
    third_party/harfbuzz/src/hb-serialize.hh
    third_party/harfbuzz/src/hb-set-digest.hh
    third_party/harfbuzz/src/hb-set.cc
    third_party/harfbuzz/src/hb-set.hh
    third_party/harfbuzz/src/hb-shape-plan.cc
    third_party/harfbuzz/src/hb-shape-plan.hh
    third_party/harfbuzz/src/hb-shape.cc
    third_party/harfbuzz/src/hb-shaper-impl.hh
    third_party/harfbuzz/src/hb-shaper-list.hh
    third_party/harfbuzz/src/hb-shaper.cc
    third_party/harfbuzz/src/hb-shaper.hh
    third_party/harfbuzz/src/hb-static.cc
    third_party/harfbuzz/src/hb-string-array.hh
    third_party/harfbuzz/src/hb-subset-cff-common.cc
    third_party/harfbuzz/src/hb-subset-cff-common.hh
    third_party/harfbuzz/src/hb-subset-cff1.cc
    third_party/harfbuzz/src/hb-subset-cff1.hh
    third_party/harfbuzz/src/hb-subset-cff2.cc
    third_party/harfbuzz/src/hb-subset-cff2.hh
    third_party/harfbuzz/src/hb-subset-input.cc
    third_party/harfbuzz/src/hb-subset-input.hh
    third_party/harfbuzz/src/hb-subset-plan.cc
    third_party/harfbuzz/src/hb-subset-plan.hh
    third_party/harfbuzz/src/hb-subset-repacker.cc
    third_party/harfbuzz/src/hb-subset-repacker.h
    third_party/harfbuzz/src/hb-subset.cc
    third_party/harfbuzz/src/hb-subset.hh
    third_party/harfbuzz/src/hb-ucd-table.hh
    third_party/harfbuzz/src/hb-ucd.cc
    third_party/harfbuzz/src/hb-unicode-emoji-table.hh
    third_party/harfbuzz/src/hb-unicode.cc
    third_party/harfbuzz/src/hb-unicode.hh
    third_party/harfbuzz/src/hb-utf.hh
    third_party/harfbuzz/src/hb-vector.hh
    third_party/harfbuzz/src/hb.hh
    third_party/harfbuzz/src/graph/gsubgpos-context.cc
)

set_target_properties(rive_text PROPERTIES
    PREFIX         ""
    SOVERSION      "${PROJECT_VERSION}"
)

target_compile_definitions(rive_text PRIVATE
    WITH_RIVE_TEXT
    WITH_RIVE_AUDIO
    HAVE_OT
    HB_NO_FALLBACK_SHAPE
    HB_NO_WIN1256
)

target_include_directories(rive_text PUBLIC
    macos/rive-cpp/include
    macos/rive-cpp/skia/renderer/include
    third_party/harfbuzz/src
    third_party/sheebidi/Headers
    third_party/miniaudio
)

target_link_libraries(rive_text PUBLIC
    rive_sheenbidi
    ${TAFFY_FFI_LINK_LIBRARY}
)

target_compile_definitions(rive_text PRIVATE ANSI_DECLARATORS)

install(TARGETS rive_text DESTINATION lib)
