#!/usr/bin/env python3
"""Dibuja las texturas del mod Cocteleria (16x16 items, 18x18 icono de efecto)."""
import math
import os
import sys
from PIL import Image

OUT = sys.argv[1]
ITEM_DIR = os.path.join(OUT, "item")
EFFECT_DIR = os.path.join(OUT, "mob_effect")
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(EFFECT_DIR, exist_ok=True)

GLASS_O = (205, 228, 240, 255)
GLASS_H = (255, 255, 255, 255)
GLASS_S = (140, 170, 192, 255)
GLASS_T = (215, 238, 250, 70)
ICE = (235, 248, 255, 215)
ICE_H = (255, 255, 255, 255)


def new():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def put(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c if len(c) == 4 else (*c, 255))


def shade(c, d):
    return (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), c[3] if len(c) == 4 else 255)


def glass(img, rows, liquid_y, liquid, base_thick=0, foam=None):
    """rows: [(y, x0, x1)]. liquid: color o lista de colores (de arriba hacia abajo)."""
    n = len(rows)
    for i, (y, x0, x1) in enumerate(rows):
        for x in range(x0, x1 + 1):
            edge = x in (x0, x1)
            bottom = i >= n - 1 - base_thick + (1 if base_thick else 0) - (0 if base_thick else 0) and base_thick and i >= n - base_thick
            last = i == n - 1
            if i == 0 and not edge:
                continue  # borde superior abierto
            if edge or last or bottom:
                if x == x0:
                    c = GLASS_H if (y % 3 != 0) else GLASS_O
                elif x == x1:
                    c = GLASS_S
                else:
                    c = GLASS_O
                put(img, x, y, c)
            else:
                if y >= liquid_y:
                    depth = y - liquid_y
                    if isinstance(liquid, list):
                        base = liquid[min(depth, len(liquid) - 1)]
                    else:
                        base = liquid
                    if foam and depth < foam[1]:
                        base = foam[0]
                    c = base
                    if y == liquid_y:
                        c = shade(c, 28)
                    elif x == x0 + 1:
                        c = shade(c, 18)
                    elif x == x1 - 1:
                        c = shade(c, -22)
                    put(img, x, y, c)
                else:
                    put(img, x, y, GLASS_T)


def stem(img, y0, y1, x0=7, x1=8):
    for y in range(y0, y1 + 1):
        put(img, x0, y, GLASS_H)
        put(img, x1, y, GLASS_S)


def base(img, rows):
    for (y, x0, x1) in rows:
        for x in range(x0, x1 + 1):
            c = GLASS_H if x == x0 else (GLASS_S if x == x1 else GLASS_O)
            put(img, x, y, c)


def blit(img, sprite, pal, ox, oy):
    for dy, line in enumerate(sprite):
        for dx, ch in enumerate(line):
            if ch != ".":
                put(img, ox + dx, oy + dy, pal[ch])


def ice(img, x, y):
    for dx in range(2):
        for dy in range(2):
            put(img, x + dx, y + dy, ICE)
    put(img, x, y, ICE_H)


def straw(img, pts):
    for i, (x, y) in enumerate(pts):
        put(img, x, y, (255, 255, 255) if i % 3 == 2 else (225, 55, 75))


# ---------- sprites ----------
LIME_PAL = {"g": (55, 140, 40), "l": (170, 230, 90), "w": (232, 252, 190)}
ORANGE_PAL = {"g": (225, 110, 20), "l": (255, 185, 60), "w": (255, 232, 150)}
LEMON_PAL = {"g": (225, 195, 30), "l": (252, 235, 90), "w": (255, 250, 190)}
WHEEL = [".ggg.", "glwlg", "gwlwg", "glwlg", ".ggg."]
CHERRY_PAL = {"r": (200, 20, 40), "h": (255, 150, 150), "s": (110, 70, 30)}
CHERRY = ["..s.", ".s..", "rrr.", "rhr.", ".r.."]
MINT_PAL = {"d": (30, 120, 40), "l": (95, 205, 85)}
MINT = [".dd.", "dlld", ".dd."]
PINE_PAL = {"G": (60, 165, 50), "y": (250, 215, 55), "Y": (255, 240, 140), "k": (200, 150, 20)}
PINE = ["G.G.G", ".GGG.", "kyyyk", ".kYk."]
UMB_PAL = {"r": (230, 60, 120), "w": (255, 255, 255), "s": (150, 110, 60)}
UMB = [".rwr.", "rwrwr", "..s..", "..s.."]


def high_glass(liquid, liquid_y=4, foam=None):
    img = new()
    rows = [(y, 4, 11) for y in range(2, 12)] + [(12, 5, 10)]
    glass(img, rows, liquid_y, liquid, base_thick=1, foam=foam)
    return img


# ---------- cócteles ----------
def mojito():
    img = high_glass((205, 242, 190, 235))
    ice(img, 5, 6); ice(img, 8, 5); ice(img, 6, 9); ice(img, 9, 8)
    blit(img, MINT, MINT_PAL, 4, 1)
    blit(img, MINT, MINT_PAL, 7, 0)
    blit(img, WHEEL, LIME_PAL, 10, 1)
    straw(img, [(9, 8), (9, 7), (10, 6), (10, 5), (10, 4), (10, 3), (11, 2), (11, 1)])
    return img


def margarita():
    img = new()
    glass(img, [(3, 1, 14), (4, 2, 13), (5, 3, 12), (6, 6, 9), (7, 7, 8)], 4, (205, 235, 110, 240))
    for x in (1, 3, 5, 12, 14):
        put(img, x, 3, (255, 255, 255, 255))
    for x in (2, 13):
        put(img, x, 3, (225, 235, 240, 255))
    stem(img, 8, 11)
    base(img, [(12, 6, 9), (13, 4, 11)])
    blit(img, WHEEL, LIME_PAL, 10, 0)
    return img


def pina_colada():
    img = new()
    rows = [(2, 5, 10), (3, 4, 11), (4, 4, 11), (5, 4, 11), (6, 5, 10), (7, 5, 10), (8, 6, 9), (9, 6, 9)]
    glass(img, rows, 3, [(252, 244, 215, 255), (248, 236, 195, 255), (245, 228, 170, 255)])
    stem(img, 10, 11)
    base(img, [(12, 6, 9), (13, 4, 11)])
    blit(img, PINE, PINE_PAL, 8, 0)
    blit(img, CHERRY, CHERRY_PAL, 4, 0)
    straw(img, [(7, 5), (7, 4), (7, 3), (6, 2), (6, 1), (6, 0)])
    return img


def blue_lagoon():
    img = new()
    rows = [(2, 5, 10), (3, 4, 11), (4, 4, 11), (5, 4, 11), (6, 5, 10), (7, 5, 10), (8, 6, 9), (9, 6, 9)]
    glass(img, rows, 3, [(70, 190, 255, 240), (30, 140, 245, 245), (20, 95, 220, 250)])
    stem(img, 10, 11)
    base(img, [(12, 6, 9), (13, 4, 11)])
    ice(img, 5, 4)
    blit(img, ORANGE_PAL and WHEEL, ORANGE_PAL, 9, 0)
    blit(img, CHERRY, CHERRY_PAL, 5, 0)
    return img


def tequila_sunrise():
    img = high_glass([(255, 205, 60, 245), (255, 170, 40, 245), (250, 125, 25, 245),
                      (240, 80, 25, 245), (215, 40, 30, 245), (190, 25, 30, 245), (175, 20, 30, 245)], 4)
    ice(img, 5, 5); ice(img, 8, 6)
    blit(img, WHEEL, ORANGE_PAL, 9, 0)
    blit(img, CHERRY, CHERRY_PAL, 4, 0)
    straw(img, [(9, 9), (9, 8), (10, 7), (10, 6), (10, 5), (10, 4), (11, 3), (11, 2)])
    return img


def cosmopolitan():
    img = new()
    glass(img, [(3, 2, 13), (4, 3, 12), (5, 4, 11), (6, 5, 10), (7, 6, 9), (8, 7, 8)], 4, (232, 70, 112, 245))
    stem(img, 9, 11)
    base(img, [(12, 6, 9), (13, 4, 11)])
    blit(img, WHEEL, ORANGE_PAL, 10, 0)
    return img


def whisky_sour():
    img = new()
    rows = [(4, 3, 12)] + [(y, 3, 12) for y in range(5, 11)] + [(11, 4, 11), (12, 4, 11)]
    glass(img, rows, 5, [(222, 150, 45, 245), (205, 125, 30, 245)], base_thick=2,
          foam=((252, 238, 195, 255), 2))
    ice(img, 5, 8)
    blit(img, CHERRY, CHERRY_PAL, 8, 0)
    # rodaja de limón en el borde
    blit(img, WHEEL, LEMON_PAL, 9, 3)
    return img


def black_russian():
    img = new()
    rows = [(4, 3, 12)] + [(y, 3, 12) for y in range(5, 11)] + [(11, 4, 11), (12, 4, 11)]
    glass(img, rows, 6, [(75, 40, 25, 250), (50, 25, 15, 250), (35, 15, 10, 250)], base_thick=2)
    ice(img, 4, 7); ice(img, 8, 6); ice(img, 6, 9)
    # chispas de hielo
    put(img, 5, 7, (255, 255, 255, 255))
    return img


def green_fairy():
    img = new()
    glass(img, [(4, 1, 14), (5, 2, 13), (6, 3, 12), (7, 5, 10)], 5, [(150, 235, 70, 240), (100, 200, 50, 245)])
    stem(img, 8, 11)
    base(img, [(12, 6, 9), (13, 4, 11)])
    # cuchara de azúcar
    for x in range(2, 14):
        put(img, x, 4, (195, 200, 212, 255))
    put(img, 13, 3, (170, 175, 190, 255)); put(img, 14, 3, (170, 175, 190, 255))
    put(img, 6, 3, (255, 255, 255, 255)); put(img, 7, 3, (245, 245, 245, 255))
    put(img, 6, 2, (235, 235, 235, 255)); put(img, 7, 2, (255, 255, 255, 255))
    # destellos mágicos
    for (x, y) in ((3, 1), (11, 0), (13, 2)):
        put(img, x, y, (190, 255, 130, 255))
    put(img, 12, 1, (150, 240, 100, 200))
    return img


def mai_tai():
    img = new()
    brown = (150, 88, 44, 255)
    dark = (105, 58, 26, 255)
    light = (186, 118, 62, 255)
    # asa
    for y in range(6, 12):
        put(img, 12, y, dark if y in (6, 11) else brown)
    for y in (6, 11):
        put(img, 13, y, dark)
    put(img, 13, 7, brown); put(img, 13, 10, brown)
    for y in (8, 9):
        put(img, 13, y, brown)
    # cuerpo
    for y in range(4, 14):
        for x in range(3, 12):
            c = brown
            if x == 3:
                c = light
            if x == 11:
                c = dark
            if y == 13:
                c = dark
            put(img, x, y, c)
    # borde y líquido
    for x in range(3, 12):
        put(img, x, 3, dark)
    for x in range(4, 11):
        put(img, x, 4, (255, 160, 40, 255))
    for x in range(4, 11):
        put(img, x, 3, (255, 205, 80, 255)) if x in (5, 6, 7) else None
    # cara tiki
    for (x, y) in ((5, 7), (9, 7)):
        put(img, x, y, (40, 20, 8, 255)); put(img, x + 1 if x == 5 else x - 1, y, (40, 20, 8, 255))
    put(img, 7, 8, dark); put(img, 7, 9, dark)
    for x in range(5, 10):
        put(img, x, 11, (40, 20, 8, 255))
    for x in (6, 8):
        put(img, x, 11, (250, 240, 220, 255))
    blit(img, UMB, UMB_PAL, 8, 0)
    blit(img, PINE_PAL and [("G.G")], {"G": (60, 165, 50)}, 3, 1) if False else None
    for (x, y) in ((4, 2), (5, 1), (3, 1), (6, 2)):
        put(img, x, y, (60, 165, 50, 255))
    put(img, 5, 2, (95, 205, 85, 255))
    return img


def sangria():
    img = new()
    rows = [(2, 4, 11), (3, 4, 11), (4, 4, 11), (5, 5, 10), (6, 5, 10), (7, 6, 9), (8, 7, 8)]
    glass(img, rows, 3, [(170, 28, 52, 245), (135, 18, 42, 250), (105, 12, 35, 250)])
    stem(img, 9, 12)
    base(img, [(13, 5, 10)])
    # fruta flotando
    for (x, y, c) in ((5, 4, (245, 150, 35)), (6, 4, (255, 190, 70)), (8, 3, (250, 230, 70)),
                      (9, 4, (255, 245, 150)), (7, 5, (245, 150, 35)), (6, 6, (200, 30, 40))):
        put(img, x, y, c)
    blit(img, WHEEL, ORANGE_PAL, 9, 0)
    return img


def long_island():
    img = high_glass([(205, 140, 60, 245), (170, 100, 40, 245), (140, 75, 30, 245)], 4)
    ice(img, 5, 5); ice(img, 8, 5); ice(img, 6, 8); ice(img, 9, 8)
    blit(img, WHEEL, LEMON_PAL, 9, 0)
    blit(img, CHERRY, CHERRY_PAL, 4, 0)
    straw(img, [(8, 9), (8, 8), (9, 7), (9, 6), (9, 5), (9, 4), (10, 3), (10, 2)])
    return img


def empty_glass():
    img = new()
    rows = [(y, 4, 11) for y in range(4, 12)] + [(12, 5, 10)]
    glass(img, rows, 99, GLASS_T, base_thick=1)
    # brillo
    put(img, 5, 6, GLASS_H); put(img, 5, 7, GLASS_H)
    return img


# ---------- icono de efecto 18x18 ----------
def tipsy_icon():
    img = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    cx, cy = 8.5, 9.0
    pts = []
    t = 0.0
    while t < 3.7 * math.pi:
        r = 0.42 * t
        pts.append((int(round(cx + r * math.cos(t))), int(round(cy + r * math.sin(t)))))
        t += 0.12
    seen = set()
    for (x, y) in pts:
        if (x, y) in seen:
            continue
        seen.add((x, y))
        put(img, x + 1, y + 1, (90, 30, 110, 255))
    for (x, y) in pts:
        put(img, x, y, (255, 125, 205, 255))
    for (x, y) in pts[::7]:
        put(img, x, y, (255, 235, 245, 255))
    # estrellitas
    for (x, y) in ((1, 2), (15, 1), (16, 14), (2, 15)):
        put(img, x, y, (255, 235, 120, 255))
        put(img, x - 1, y, (255, 200, 60, 255)); put(img, x + 1, y, (255, 200, 60, 255))
        put(img, x, y - 1, (255, 200, 60, 255)); put(img, x, y + 1, (255, 200, 60, 255))
    return img


DRINKS = {
    "mojito": mojito, "margarita": margarita, "pina_colada": pina_colada, "blue_lagoon": blue_lagoon,
    "tequila_sunrise": tequila_sunrise, "cosmopolitan": cosmopolitan, "whisky_sour": whisky_sour,
    "black_russian": black_russian, "green_fairy": green_fairy, "mai_tai": mai_tai,
    "sangria": sangria, "long_island": long_island, "empty_glass": empty_glass,
}

if __name__ == "__main__":
    imgs = []
    for name, fn in DRINKS.items():
        im = fn()
        im.save(os.path.join(ITEM_DIR, name + ".png"))
        imgs.append(im)
    tip = tipsy_icon()
    tip.save(os.path.join(EFFECT_DIR, "tipsy.png"))
    # hoja de vista previa
    S = 10
    cols = 5
    sheet = Image.new("RGBA", (cols * 18 * S + (cols + 1) * 8, 3 * 18 * S + 4 * 8), (58, 58, 66, 255))
    allimgs = imgs + [tip]
    for i, im in enumerate(allimgs):
        big = im.resize((im.width * S, im.height * S), Image.NEAREST)
        x = 8 + (i % cols) * (18 * S + 8)
        y = 8 + (i // cols) * (18 * S + 8)
        sheet.alpha_composite(big, (x, y))
    sheet.save("/home/claude/tools/preview.png")
    print("ok", len(allimgs))
