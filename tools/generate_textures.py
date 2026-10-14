"""Generates the 16×16 item sprites of vocana-compat's modules. Pillow is needed only to regenerate them; the PNGs are
checked in. Each module has one function that writes under src/main/resources/assets/vocanacompat/textures/item/<module>/.

Curios: four focus items in the metals of their recipes (gold, copper) with amethyst and echo-shard settings, drawn in
the flat, lit-from-the-upper-left manner of Vocana's own sprites (tools/generate_staff_textures.py there): a Ring of
Focus (a gold band with an amethyst), an Amulet of Resonance (a gold chain with a copper disc holding an echo shard), a
Charm of Calm (a copper bead on a cord with a pale amethyst eye) and a Signet of the Elements (a broad gold band with
a dark bezel that the game's foil fills once an essence is drunk).
Usage: python3 tools/generate_textures.py
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/vocanacompat/textures/item'
# dark, mid, light
GOLD = [(122, 82, 10), (214, 168, 38), (252, 230, 120)]
COPPER = [(98, 46, 22), (164, 82, 39), (226, 132, 80)]
AMETHYST = [(84, 19, 114), (140, 59, 179), (200, 140, 230)]
PALE = [(96, 80, 150), (170, 160, 220), (236, 232, 255)]
ECHO = [(10, 40, 48), (28, 110, 118), (120, 220, 220)]
CORD = [(40, 30, 22), (90, 70, 50), (140, 115, 85)]
WHITE = (255, 255, 255)


class Sprite:
    def __init__(self):
        self.im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        self.px = self.im.load()

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[x, y] = tuple(c) + (255,)

    def gem(self, cx, cy, r, shades, glint=True):
        """A diamond-shaped gem of radius r: light toward the upper left, dark toward the lower right."""
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                d = abs(x - cx) + abs(y - cy)
                if d > r:
                    continue
                s = (x - cx) + (y - cy)
                self.put(x, y, shades[2] if s < -r // 2 else shades[0] if s > r // 2 else shades[1])
        if glint:
            self.put(cx - (1 if r > 1 else 0), cy - (1 if r > 1 else 0), WHITE)

    def ring(self, cx, cy, rx, ry, metal, thick=1, gap=None):
        """An elliptical band: the outer edge dark, the inner edge light, the body mid. gap=(a0,a1) in degrees leaves it open."""
        import math
        for a10 in range(0, 3600):
            a = a10 / 10.0
            if gap and gap[0] < a < gap[1]:
                continue
            rad = math.radians(a)
            for i in range(thick):
                rr = rx - i
                x = int(round(cx + rr * math.cos(rad)))
                y = int(round(cy + (ry - i) * math.sin(rad)))
                # Light where the band faces the upper left, dark where it faces the lower right.
                facing = math.cos(rad) + math.sin(rad)
                self.put(x, y, metal[2] if facing < -0.9 else metal[0] if facing > 0.9 else metal[1])

    def save(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.im.save(path)


def ring_of_focus():
    s = Sprite()
    s.ring(8, 9, 5, 5, GOLD, thick=2)
    s.gem(8, 3, 2, AMETHYST)
    # Claws holding the gem.
    s.put(6, 4, GOLD[1]); s.put(10, 4, GOLD[1]); s.put(6, 5, GOLD[0]); s.put(10, 5, GOLD[0])
    return s


def amulet_of_resonance():
    s = Sprite()
    # The chain: an open arc of single gold pixels, lighter on the left.
    s.ring(8, 6, 6, 6, GOLD, thick=1, gap=(40, 140))
    # The disc, copper, hanging from the chain's bottom.
    for y in range(9, 15):
        for x in range(5, 12):
            if (x - 8) ** 2 + (y - 11.5) ** 2 <= 9.5:
                s.put(x, y, COPPER[2] if x + y < 18 else COPPER[0] if x + y > 21 else COPPER[1])
    s.gem(8, 11, 1, ECHO, glint=False)
    s.put(8, 12, ECHO[2]); s.put(7, 11, ECHO[2])
    return s


def charm_of_calm():
    s = Sprite()
    # The cord, a loop that narrows to the bead.
    for (x, y) in [(5, 1), (4, 2), (4, 3), (5, 4), (6, 5), (10, 1), (11, 2), (11, 3), (10, 4), (9, 5), (6, 1), (7, 1), (8, 1), (9, 1)]:
        s.put(x, y, CORD[1])
    s.put(5, 1, CORD[2]); s.put(4, 2, CORD[2])
    # The bead: a rounded copper drop.
    for y in range(5, 15):
        for x in range(4, 13):
            dx, dy = x - 8, (y - 9.5) / 1.15
            if dx * dx + dy * dy <= 13:
                s.put(x, y, COPPER[2] if dx + dy < -2.5 else COPPER[0] if dx + dy > 2.5 else COPPER[1])
    # The eye: a pale amethyst set in the bead.
    s.gem(8, 9, 2, PALE)
    s.put(8, 9, PALE[1])
    return s


def signet_of_the_elements():
    s = Sprite()
    s.ring(8, 10, 5, 4, GOLD, thick=2)
    # A broad bezel: a dark setting the foil lights when the signet carries an essence.
    for y in range(2, 8):
        for x in range(5, 12):
            if abs(x - 8) + abs(y - 4.5) <= 4:
                s.put(x, y, GOLD[1])
    for y in range(3, 7):
        for x in range(6, 11):
            if abs(x - 8) + abs(y - 4.5) <= 2.5:
                s.put(x, y, (46, 26, 66) if x + y > 12 else (70, 44, 96))
    s.put(6, 3, GOLD[2]); s.put(5, 4, GOLD[2]); s.put(10, 7, GOLD[0]); s.put(11, 6, GOLD[0])
    s.put(7, 4, (110, 80, 150))
    return s


CURIOS = {'ring_of_focus': ring_of_focus, 'amulet_of_resonance': amulet_of_resonance,
          'charm_of_calm': charm_of_calm, 'signet_of_the_elements': signet_of_the_elements}


def main():
    for name, draw in CURIOS.items():
        draw().save(ROOT / 'curios' / f'{name}.png')
    print(f'Wrote {len(CURIOS)} Curios sprites to {ROOT / "curios"}')


if __name__ == '__main__':
#!/usr/bin/env python3
"""Draws the Ice and Fire module's item textures (16x16 pixel art) with Pillow and writes them to
src/main/resources/assets/vocanacompat/textures/item/iceandfire/. Deterministic: run it again and the PNGs are identical,
so the committed files can always be regenerated. Usage: python3 tools/generate_textures.py [repo root]
"""
import os
import sys

from PIL import Image

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/vocanacompat/textures/item/iceandfire")

# Wyrmsteel: netherite's near-black with a violet sheen, lit by the three dragon kinds.
DARK = (31, 26, 36, 255)
STEEL = (58, 50, 66, 255)
SHEEN = (96, 82, 112, 255)
GLINT = (150, 134, 170, 255)
EDGE = (20, 16, 24, 255)
FIRE = (232, 92, 40, 255)
FROST = (120, 200, 240, 255)
BOLT = (240, 220, 90, 255)
WOOD = (82, 56, 36, 255)
WOOD_LIGHT = (120, 84, 52, 255)
GOLD = (214, 170, 70, 255)
GOLD_DARK = (150, 110, 40, 255)
CLEAR = (0, 0, 0, 0)


def canvas():
    return Image.new("RGBA", (16, 16), CLEAR)


def put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), c)


def fill(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(img, x, y, c)


def diagonal(img, x0, y0, length, c, thickness=1):
    """A line rising to the upper right, pixel steps, thickened along x."""
    for i in range(length):
        for t in range(thickness):
            put(img, x0 + i + t, y0 - i, c)


def outline(img, c=EDGE):
    """Edges every opaque pixel that borders a transparent one, so the shape reads on any background."""
    src = img.copy()
    for y in range(16):
        for x in range(16):
            if src.getpixel((x, y))[3] == 0:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and src.getpixel((nx, ny))[3] == 0:
                    put(img, nx, ny, c)


def ingot():
    img = canvas()
    # The familiar ingot: a flattened slab seen from the front-right, lighter top face.
    for y in range(6, 12):
        fill(img, 2 + (11 - y) // 2, y, 10 + (11 - y) // 2, y, STEEL)
    fill(img, 5, 5, 13, 5, SHEEN)
    fill(img, 4, 6, 12, 6, SHEEN)
    for y in range(7, 12):
        put(img, 11 + (11 - y) // 2, y, DARK)
    fill(img, 2, 11, 10, 11, DARK)
    # the three glints
    put(img, 5, 7, FIRE)
    put(img, 7, 8, FROST)
    put(img, 9, 7, BOLT)
    put(img, 6, 5, GLINT)
    outline(img)
    return img


def staff():
    img = canvas()
    # the shaft, bottom-left to upper-right, wyrmsteel-shod wood
    diagonal(img, 1, 14, 11, WOOD, 2)
    diagonal(img, 2, 14, 10, WOOD_LIGHT)
    for i in range(0, 3):
        put(img, 1 + i, 14 - i, STEEL)
        put(img, 2 + i, 14 - i, SHEEN)
    # the head: a wyrmsteel claw holding three dragon crystals
    fill(img, 10, 2, 13, 5, STEEL)
    put(img, 10, 2, CLEAR); put(img, 13, 5, CLEAR)
    put(img, 11, 1, SHEEN); put(img, 14, 3, SHEEN); put(img, 9, 4, SHEEN)
    put(img, 11, 3, FIRE); put(img, 12, 2, FROST); put(img, 12, 4, BOLT)
    put(img, 12, 3, GLINT)
    outline(img)
    return img


def sword():
    img = canvas()
    # blade, diagonal, with a violet sheen edge and the three glints down its spine
    diagonal(img, 4, 11, 11, STEEL, 2)
    diagonal(img, 5, 11, 10, SHEEN)
    diagonal(img, 4, 10, 10, DARK)
    put(img, 7, 8, FIRE); put(img, 9, 6, FROST); put(img, 11, 4, BOLT)
    # cross-guard and grip
    put(img, 3, 10, GOLD); put(img, 4, 11, GOLD); put(img, 5, 12, GOLD); put(img, 6, 13, GOLD_DARK)
    put(img, 5, 10, GOLD); put(img, 3, 12, GOLD)
    put(img, 2, 13, WOOD); put(img, 1, 14, WOOD_LIGHT)
    put(img, 0, 15, GOLD_DARK)
    outline(img)
    return img


def pickaxe():
    img = canvas()
    # handle
    diagonal(img, 1, 14, 10, WOOD)
    diagonal(img, 2, 14, 9, WOOD_LIGHT)
    # head: an arc across the upper right
    for x, y in ((5, 2), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1), (11, 2), (12, 3), (13, 4), (13, 5), (14, 6), (14, 7)):
        put(img, x, y, STEEL)
    for x, y in ((6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 3), (12, 4), (13, 6)):
        put(img, x, y, SHEEN)
    for x, y in ((4, 3), (5, 3), (12, 5), (13, 7), (14, 8)):
        put(img, x, y, DARK)
    put(img, 8, 2, FIRE); put(img, 11, 3, FROST); put(img, 13, 5, BOLT)
    outline(img)
    return img


def incomplete(img):
    """The piece as it rides the assembly line: the same shape, unlit, dulled and missing its glints."""
    out = img.copy()
    for y in range(16):
        for x in range(16):
            r, g, b, a = out.getpixel((x, y))
            if a == 0:
                continue
            if (r, g, b, a) in (FIRE, FROST, BOLT, GLINT):
                out.putpixel((x, y), STEEL)
            else:
                grey = (r * 3 + g * 4 + b * 3) // 10
                out.putpixel((x, y), ((r + grey) // 2, (g + grey) // 2, (b + grey) // 2, a))
            # a few pixels not yet worked, in a fixed pattern
            if (x * 7 + y * 3) % 11 == 0 and (r, g, b, a) != EDGE:
                out.putpixel((x, y), (grey // 2, grey // 2, grey // 2, 255))
    return out


def main():
    os.makedirs(OUT, exist_ok=True)
    images = {
        "netherite_dragonsteel_ingot": ingot(),
        "wyrmsteel_staff": staff(),
        "wyrmsteel_sword": sword(),
        "wyrmsteel_pickaxe": pickaxe(),
    }
    for name in ("wyrmsteel_staff", "wyrmsteel_sword", "wyrmsteel_pickaxe"):
        images["incomplete_" + name] = incomplete(images[name])
    for name, img in images.items():
        img.save(os.path.join(OUT, name + ".png"), optimize=True)
        print("wrote", name + ".png")


if __name__ == "__main__":
    main()
