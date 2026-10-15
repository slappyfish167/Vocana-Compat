#!/usr/bin/env python3
"""The Ice and Fire module's textures (16x16 pixel art, and the 64x32 worn armour layers) drawn with Pillow, written to
src/main/resources/assets/vocanacompat/textures/{item,block}/iceandfire/ and textures/models/armor/. Deterministic: run
it again and the PNGs are identical, so the committed files can always be regenerated. Run tools/generate_textures.py
(or this file alone).
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURES = os.path.join(ROOT, "src/main/resources/assets/vocanacompat/textures")
OUT = os.path.join(TEXTURES, "item/iceandfire")
BLOCK_OUT = os.path.join(TEXTURES, "block/iceandfire")
ARMOR_OUT = os.path.join(TEXTURES, "models/armor")

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




def axe():
    img = canvas()
    # handle
    diagonal(img, 1, 14, 11, WOOD)
    diagonal(img, 2, 14, 10, WOOD_LIGHT)
    # head: a crescent blade to the left of the haft's top, its cutting edge lit, a short poll behind it
    for y, (x0, x1) in zip(range(1, 8), ((7, 9), (6, 10), (5, 10), (5, 11), (5, 10), (6, 10), (7, 9))):
        fill(img, x0, y, x1, y, STEEL)
        put(img, x0, y, SHEEN)
    put(img, 5, 4, GLINT); put(img, 6, 2, GLINT)
    fill(img, 11, 2, 12, 3, DARK); put(img, 11, 2, STEEL)
    for x, y in ((8, 7), (9, 6), (10, 5)):
        put(img, x, y, DARK)
    put(img, 7, 3, FIRE); put(img, 8, 4, FROST); put(img, 7, 5, BOLT)
    outline(img)
    return img


def shovel():
    img = canvas()
    # handle
    diagonal(img, 1, 14, 9, WOOD)
    diagonal(img, 2, 14, 8, WOOD_LIGHT)
    # blade: a rounded spade at the top right
    for y in range(1, 7):
        for x in range(9, 15):
            if (x - 12) ** 2 + (y - 3.5) ** 2 <= 7.5 and x + (6 - y) >= 10:
                put(img, x, y, STEEL)
    for x, y in ((10, 2), (11, 1), (12, 1), (10, 3)):
        put(img, x, y, SHEEN)
    for x, y in ((14, 4), (13, 6), (14, 5), (12, 6)):
        put(img, x, y, DARK)
    put(img, 11, 3, FIRE); put(img, 12, 4, FROST); put(img, 13, 3, BOLT)
    put(img, 11, 2, GLINT)
    outline(img)
    return img


def hoe():
    img = canvas()
    # handle
    diagonal(img, 1, 14, 11, WOOD)
    diagonal(img, 2, 14, 10, WOOD_LIGHT)
    # head: a blade turned down to the left of the top
    for x, y in ((7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (7, 3), (8, 3), (11, 3), (7, 4)):
        put(img, x, y, STEEL)
    for x, y in ((8, 1), (9, 1), (10, 1), (11, 1)):
        put(img, x, y, SHEEN)
    for x, y in ((6, 4), (6, 5), (7, 5)):
        put(img, x, y, DARK)
    put(img, 8, 2, FIRE); put(img, 9, 2, FROST); put(img, 10, 2, BOLT)
    outline(img)
    return img


def shade_box(img, x0, y0, x1, y1):
    """Fills a rectangle as a lit metal plate: sheen along the top and left, dark along the bottom and right."""
    fill(img, x0, y0, x1, y1, STEEL)
    fill(img, x0, y0, x1, y0, SHEEN)
    fill(img, x0, y0, x0, y1, SHEEN)
    fill(img, x0, y1, x1, y1, DARK)
    fill(img, x1, y0, x1, y1, DARK)


def helmet():
    img = canvas()
    shade_box(img, 3, 3, 12, 8)
    fill(img, 3, 9, 4, 11, STEEL); fill(img, 11, 9, 12, 11, DARK)
    # crest and the three glints across the brow
    fill(img, 6, 2, 9, 2, SHEEN); put(img, 7, 1, GLINT)
    put(img, 5, 5, FIRE); put(img, 7, 5, FROST); put(img, 9, 5, BOLT)
    # the eye slit left open
    fill(img, 5, 8, 10, 9, CLEAR)
    outline(img)
    return img


def chestplate():
    img = canvas()
    shade_box(img, 3, 2, 12, 13)
    fill(img, 1, 2, 2, 6, STEEL); fill(img, 13, 2, 14, 6, DARK)
    put(img, 1, 2, SHEEN); put(img, 2, 2, SHEEN)
    # the neck opening and the waist cut
    fill(img, 6, 2, 9, 3, CLEAR)
    fill(img, 7, 13, 8, 13, CLEAR)
    # the three glints on the breast, a gold clasp
    put(img, 5, 6, FIRE); put(img, 7, 7, FROST); put(img, 10, 6, BOLT)
    put(img, 7, 10, GOLD); put(img, 8, 10, GOLD_DARK)
    put(img, 4, 4, GLINT)
    outline(img)
    return img


def leggings():
    img = canvas()
    shade_box(img, 3, 2, 12, 5)
    shade_box(img, 3, 6, 6, 14)
    shade_box(img, 9, 6, 12, 14)
    fill(img, 7, 6, 8, 14, CLEAR)
    put(img, 7, 3, GOLD); put(img, 8, 3, GOLD_DARK)
    put(img, 4, 9, FIRE); put(img, 11, 8, FROST); put(img, 5, 12, BOLT)
    outline(img)
    return img


def boots():
    img = canvas()
    shade_box(img, 2, 6, 5, 12)
    shade_box(img, 10, 6, 13, 12)
    fill(img, 1, 11, 6, 13, STEEL); fill(img, 9, 11, 14, 13, STEEL)
    fill(img, 1, 13, 6, 13, DARK); fill(img, 9, 13, 14, 13, DARK)
    put(img, 3, 8, FIRE); put(img, 11, 8, FROST); put(img, 12, 10, BOLT)
    put(img, 2, 6, GLINT)
    outline(img)
    return img


def block():
    """The storage block: dark wyrmsteel plates riveted in a frame, with the three glints in its seams."""
    img = Image.new("RGBA", (16, 16), STEEL)
    for i in range(16):
        put(img, i, 0, SHEEN); put(img, 0, i, SHEEN)
        put(img, i, 15, EDGE); put(img, 15, i, EDGE)
        put(img, i, 1, GLINT if i in (1, 14) else SHEEN if i < 15 else DARK)
        put(img, 14, i, DARK if i > 0 else SHEEN)
    # two seams that cross the face, dark below and light above
    for i in range(2, 14):
        put(img, i, 7, DARK); put(img, i, 8, SHEEN)
        put(img, 7, i, DARK); put(img, 8, i, SHEEN)
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        put(img, x, y, GLINT); put(img, x + 1, y + 1, DARK)
    put(img, 7, 7, FIRE); put(img, 8, 7, FROST); put(img, 7, 8, BOLT); put(img, 8, 8, GLINT)
    put(img, 4, 10, DARK); put(img, 11, 5, DARK); put(img, 10, 11, SHEEN); put(img, 5, 4, SHEEN)
    return img


# ---- wyrmsteel dragon armour (the item icons; the worn look is made in game from Ice and Fire's own texture) ----

def dragon_head():
    img = canvas()
    # a long helm seen from the side, snout to the right, two horns swept back
    for y in range(5, 12):
        x1 = 13 if y >= 7 else 9 + (y - 5) * 2
        fill(img, 2, y, x1, y, STEEL)
    fill(img, 2, 5, 9, 5, SHEEN); fill(img, 2, 5, 2, 11, SHEEN)
    fill(img, 3, 11, 13, 11, DARK); fill(img, 13, 7, 13, 11, DARK)
    for x, y in ((1, 3), (2, 2), (3, 3), (3, 4), (5, 3), (6, 2), (6, 4)):
        put(img, x, y, SHEEN if y < 3 else STEEL)
    fill(img, 9, 8, 10, 8, CLEAR)
    put(img, 5, 7, FIRE); put(img, 7, 9, FROST); put(img, 11, 9, BOLT)
    put(img, 4, 6, GLINT)
    outline(img)
    return img


def dragon_neck():
    img = canvas()
    # three overlapping rings of plate down a curve
    for i, (cx, cy) in enumerate(((4, 4), (8, 8), (12, 12))):
        fill(img, cx - 3, cy - 2, cx + 2, cy + 1, STEEL)
        fill(img, cx - 3, cy - 2, cx + 2, cy - 2, SHEEN)
        fill(img, cx - 3, cy + 1, cx + 2, cy + 1, DARK)
        put(img, cx - 1, cy - 1, (FIRE, FROST, BOLT)[i])
    put(img, 2, 2, GLINT)
    outline(img)
    return img


def dragon_body():
    img = canvas()
    # a broad saddle plate with a ridge of spines
    for y in range(5, 13):
        inset = max(0, 7 - y) if y < 7 else max(0, y - 10)
        fill(img, 1 + inset, y, 14 - inset, y, STEEL)
    fill(img, 3, 5, 12, 5, SHEEN); fill(img, 2, 12, 13, 12, DARK)
    for x in (4, 7, 10):
        put(img, x, 3, SHEEN); put(img, x, 4, STEEL); put(img, x + 1, 4, DARK)
    put(img, 4, 8, FIRE); put(img, 7, 9, FROST); put(img, 11, 8, BOLT)
    fill(img, 6, 6, 9, 6, GOLD); put(img, 9, 6, GOLD_DARK)
    put(img, 2, 7, GLINT)
    outline(img)
    return img


def dragon_tail():
    img = canvas()
    # tapering segments ending in a blade
    for i in range(6):
        x, y, w = 1 + i * 2, 4 + i, 4 - i // 2
        fill(img, x, y, x + 2, y + w - 1, STEEL)
        put(img, x, y, SHEEN); put(img, x + 2, y + w - 1, DARK)
    for x, y in ((13, 9), (14, 10), (14, 11), (13, 12), (12, 11)):
        put(img, x, y, SHEEN if y < 11 else DARK)
    put(img, 3, 5, FIRE); put(img, 7, 7, FROST); put(img, 11, 9, BOLT)
    outline(img)
    return img


# ---- the worn armour (64x32, the vanilla humanoid layout) ----

def layer_plate(img, x0, y0, w, h):
    """One face of a box in the armour texture: a lit plate with an edge, like the item sprites."""
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            c = STEEL
            if y == y0 or x == x0:
                c = SHEEN
            if y == y0 + h - 1 or x == x0 + w - 1:
                c = DARK
            img.putpixel((x, y), c)


def layer_box(img, u, v, w, h, d, rows=None):
    """The six faces of a box laid out as Minecraft unwraps it at (u, v): w wide, h tall, d deep. rows limits the side
    faces to their lowest rows (boots) or (start, count) from the top (leggings, the waist of a chestplate)."""
    top = (u + d, v, w, d)
    bottom = (u + d + w, v, w, d)
    sides = [(u, v + d, d), (u + d, v + d, w), (u + d + w, v + d, d), (u + d + w + d, v + d, w)]
    start, count = 0, h
    if rows is not None:
        start, count = rows if isinstance(rows, tuple) else (h - rows, rows)
    for (x, y, fw) in sides:
        layer_plate(img, x, y + start, fw, count)
    if start == 0:
        layer_plate(img, *top)
    if start + count == h:
        layer_plate(img, *bottom)


def armor_layer_1():
    img = Image.new("RGBA", (64, 32), CLEAR)
    # helmet: the head box (8x8x8 at 0,0); the visor slit stays open on the front face
    layer_box(img, 0, 0, 8, 8, 8)
    for x in range(9, 15):
        img.putpixel((x, 12), CLEAR); img.putpixel((x, 13), CLEAR)
    img.putpixel((10, 9), FIRE); img.putpixel((12, 9), FROST); img.putpixel((14, 9), BOLT)
    img.putpixel((11, 8), GLINT); img.putpixel((12, 8), GLINT)
    # chestplate: the body (8x12x4 at 16,16) and the arms (4x12x4 at 40,16), the arms to the elbow
    layer_box(img, 16, 16, 8, 12, 4)
    layer_box(img, 40, 16, 4, 12, 4, rows=(0, 7))
    for x in range(22, 26):
        img.putpixel((x, 20), CLEAR)
    img.putpixel((22, 23), FIRE); img.putpixel((24, 24), FROST); img.putpixel((26, 23), BOLT)
    img.putpixel((23, 27), GOLD); img.putpixel((24, 27), GOLD_DARK)
    # boots: the legs (4x12x4 at 0,16), the lowest five rows and the soles
    layer_box(img, 0, 16, 4, 12, 4, rows=5)
    img.putpixel((5, 29), FIRE); img.putpixel((13, 29), FROST)
    return img


def armor_layer_2():
    img = Image.new("RGBA", (64, 32), CLEAR)
    # leggings: the waist of the body (rows 7..11 of 8x12x4 at 16,16) and the legs down to the boot line
    layer_box(img, 16, 16, 8, 12, 4, rows=(7, 5))
    layer_box(img, 0, 16, 4, 12, 4, rows=(0, 9))
    img.putpixel((23, 25), GOLD); img.putpixel((24, 25), GOLD_DARK)
    img.putpixel((5, 23), FIRE); img.putpixel((13, 22), FROST); img.putpixel((6, 26), BOLT)
    return img


ITEMS = {
    "netherite_dragonsteel_ingot": ingot,
    "wyrmsteel_staff": staff,
    "wyrmsteel_sword": sword,
    "wyrmsteel_pickaxe": pickaxe,
    "wyrmsteel_axe": axe,
    "wyrmsteel_shovel": shovel,
    "wyrmsteel_hoe": hoe,
    "wyrmsteel_helmet": helmet,
    "wyrmsteel_chestplate": chestplate,
    "wyrmsteel_leggings": leggings,
    "wyrmsteel_boots": boots,
    "dragonarmor_wyrmsteel_head": dragon_head,
    "dragonarmor_wyrmsteel_neck": dragon_neck,
    "dragonarmor_wyrmsteel_body": dragon_body,
    "dragonarmor_wyrmsteel_tail": dragon_tail,
}
# Every piece made on an assembly line rides its own incomplete piece.
LINES = [name for name in ITEMS if name.startswith("wyrmsteel_")]


def save(img, folder, name):
    os.makedirs(folder, exist_ok=True)
    img.save(os.path.join(folder, name + ".png"), optimize=True)
    print("wrote", os.path.relpath(os.path.join(folder, name + ".png"), TEXTURES))


def main():
    images = {name: draw() for name, draw in ITEMS.items()}
    for name in LINES:
        images["incomplete_" + name] = incomplete(images[name])
    for name, img in images.items():
        save(img, OUT, name)
    save(block(), BLOCK_OUT, "wyrmsteel_block")
    save(armor_layer_1(), ARMOR_OUT, "wyrmsteel_layer_1")
    save(armor_layer_2(), ARMOR_OUT, "wyrmsteel_layer_2")


if __name__ == "__main__":
    main()
