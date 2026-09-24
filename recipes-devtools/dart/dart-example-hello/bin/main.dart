// Smallest thing that proves the toolchain: no pub dependencies, so the build
// exercises gen_kernel and the cross gen_snapshot and nothing else.
void main(List<String> args) {
  print('hello from a dart app built for ${args.isEmpty ? "this target" : args.first}');
}
