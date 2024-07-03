if("${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" LESS 2.8)
   message(FATAL_ERROR "CMake >= 2.8.0 required")
endif()
if(CMAKE_VERSION VERSION_LESS "2.8.3")
   message(FATAL_ERROR "CMake >= 2.8.3 required")
endif()
cmake_policy(PUSH)
cmake_policy(VERSION 2.8.3...3.25)
#----------------------------------------------------------------
# Generated CMake target import file.
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Protect against multiple inclusion, which would fail when already imported targets are added once more.
set(_cmake_targets_defined "")
set(_cmake_targets_not_defined "")
set(_cmake_expected_targets "")
foreach(_cmake_expected_target IN ITEMS matc cmgen filamesh mipgen resgen uberz glslminifier)
  list(APPEND _cmake_expected_targets "${_cmake_expected_target}")
  if(TARGET "${_cmake_expected_target}")
    list(APPEND _cmake_targets_defined "${_cmake_expected_target}")
  else()
    list(APPEND _cmake_targets_not_defined "${_cmake_expected_target}")
  endif()
endforeach()
unset(_cmake_expected_target)
if(_cmake_targets_defined STREQUAL _cmake_expected_targets)
  unset(_cmake_targets_defined)
  unset(_cmake_targets_not_defined)
  unset(_cmake_expected_targets)
  unset(CMAKE_IMPORT_FILE_VERSION)
  cmake_policy(POP)
  return()
endif()
if(NOT _cmake_targets_defined STREQUAL "")
  string(REPLACE ";" ", " _cmake_targets_defined_text "${_cmake_targets_defined}")
  string(REPLACE ";" ", " _cmake_targets_not_defined_text "${_cmake_targets_not_defined}")
  message(FATAL_ERROR "Some (but not all) targets in this export set were already defined.\nTargets Defined: ${_cmake_targets_defined_text}\nTargets not yet defined: ${_cmake_targets_not_defined_text}\n")
endif()
unset(_cmake_targets_defined)
unset(_cmake_targets_not_defined)
unset(_cmake_expected_targets)


# Create imported target matc
add_executable(matc IMPORTED)

# Create imported target cmgen
add_executable(cmgen IMPORTED)

# Create imported target filamesh
add_executable(filamesh IMPORTED)

# Create imported target mipgen
add_executable(mipgen IMPORTED)

# Create imported target resgen
add_executable(resgen IMPORTED)

# Create imported target uberz
add_executable(uberz IMPORTED)

# Create imported target glslminifier
add_executable(glslminifier IMPORTED)

# Import target "matc" for configuration "Release"
set_property(TARGET matc APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(matc PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/matc"
  )

# Import target "cmgen" for configuration "Release"
set_property(TARGET cmgen APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(cmgen PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/cmgen"
  )

# Import target "filamesh" for configuration "Release"
set_property(TARGET filamesh APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(filamesh PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/filamesh"
  )

# Import target "mipgen" for configuration "Release"
set_property(TARGET mipgen APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(mipgen PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/mipgen"
  )

# Import target "resgen" for configuration "Release"
set_property(TARGET resgen APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(resgen PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/resgen"
  )

# Import target "uberz" for configuration "Release"
set_property(TARGET uberz APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(uberz PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/uberz"
  )

# Import target "glslminifier" for configuration "Release"
set_property(TARGET glslminifier APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(glslminifier PROPERTIES
  IMPORTED_LOCATION_RELEASE "${FILAMENT_HOST_TOOLS_ROOT}/glslminifier"
  )

# This file does not depend on other imported targets which have
# been exported from the same project but in a separate export set.

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
cmake_policy(POP)