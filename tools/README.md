# Tools


## How to auto roll meta-flutter

1. Fork https://github.com/meta-flutter/meta-flutter

2. run auto-roll script
```
tools/roll_meta_flutter.py
```

3. Test

4. Create PR documenting what was tested and targets tested on

*Note: Only stable branch versions will be accepted*


## Process to Auto Roll Flutter Applications, Flutter SDK version (includes Engine), and Dart-SDK recipe

channel `stable`

    tools/roll_meta_flutter.py

channel `beta`

    tools/roll_meta_flutter.py --channel=beta

channel `dev`

    tools/roll_meta_flutter.py --channel=dev

specific version

    tools/roll_meta_flutter.py --version=2.40.0
