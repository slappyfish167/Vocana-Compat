#!/usr/bin/env python3
"""Draws vocana-compat's textures with Pillow. Pillow is needed only to regenerate them; the PNGs are checked in and
regenerate byte-for-byte. Each module that has textures has one file beside this one, textures_<module>.py, with a
main() that writes under src/main/resources/assets/vocanacompat/textures/:

- textures_curios.py: the four Curios focus items (item/curios/);
- textures_iceandfire.py: the wyrmsteel ingot, block, staff, tools, armour, their incomplete pieces (item/iceandfire/,
  block/iceandfire/) and the worn armour layers (models/armor/).

Usage: python3 tools/generate_textures.py [module ...]   (no argument: every module)
"""
import sys

import textures_curios
import textures_iceandfire

MODULES = {'curios': textures_curios.main, 'iceandfire': textures_iceandfire.main}


def main(names):
    for name in names or MODULES:
        if name not in MODULES:
            sys.exit(f'no textures for module {name!r}; known: {", ".join(MODULES)}')
        MODULES[name]()


if __name__ == '__main__':
    main(sys.argv[1:])
