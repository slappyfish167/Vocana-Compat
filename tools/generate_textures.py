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
    main()
