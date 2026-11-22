// SPDX-License-Identifier: MIT

#include <cstdint>
#include <string>

// The length of "<a>,<b>". std::string so that the library needs the C++
// runtime, not only the compiler.
extern "C" int32_t cbuilder_hook_test_join(int32_t a, int32_t b) {
  const std::string joined = std::to_string(a) + "," + std::to_string(b);
  return static_cast<int32_t>(joined.size());
}
