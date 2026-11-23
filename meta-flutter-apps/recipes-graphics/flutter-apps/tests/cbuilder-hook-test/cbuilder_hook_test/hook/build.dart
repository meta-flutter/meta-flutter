// SPDX-License-Identifier: MIT

import 'package:code_assets/code_assets.dart';
import 'package:hooks/hooks.dart';
import 'package:logging/logging.dart';
import 'package:native_toolchain_c/native_toolchain_c.dart';

// Three CBuilder runs, one for each way native_toolchain_c drives a
// toolchain: compile and link in one call, compile with -c and archive, and
// C++ through the C compiler driver.
void main(List<String> args) async {
  await build(args, (input, output) async {
    if (!input.config.buildCodeAssets) {
      return;
    }

    // Print the commands CBuilder runs. `flutter build -v` shows them.
    final logger = Logger('')
      ..level = Level.ALL
      ..onRecord.listen((record) => print(record.message));

    // A static archive. No asset: it only feeds the link below.
    await CBuilder.library(
      name: 'cbuilder_hook_test_twice',
      assetName: null,
      linkModePreference: LinkModePreference.static,
      sources: ['src/twice.c'],
    ).run(input: input, output: output, logger: logger);

    // A shared library in C, linked against the archive.
    await CBuilder.library(
      name: 'cbuilder_hook_test_c',
      assetName: 'c.dart',
      sources: ['src/add.c'],
      libraries: ['cbuilder_hook_test_twice'],
      libraryDirectories: [input.outputDirectory.toFilePath()],
    ).run(input: input, output: output, logger: logger);

    // A shared library in C++.
    await CBuilder.library(
      name: 'cbuilder_hook_test_cpp',
      assetName: 'cpp.dart',
      language: Language.cpp,
      std: 'c++17',
      sources: ['src/join.cc'],
    ).run(input: input, output: output, logger: logger);
  });
}
