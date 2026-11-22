#!/usr/bin/env python3
"""Textura del dardo (item) y de la diana (GUI)."""
import math, os, sys
from PIL import Image, ImageDraw, ImageFont

OUT = sys.argv[1]  # carpeta assets/cocteleria/textures
os.makedirs(os.path.join(OUT, "item"), exist_ok=True)
os.makedirs(os.path.join(OUT, "gui"), exist_ok=True)

# ---- dardo 16x16 (diagonal, punta abajo-izquierda)
d = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
def put(x, y, c): d.putpixel((x, y), c)
for i in range(0, 8):                       # cuerpo
    put(2 + i, 13 - i, (150, 154, 165, 255))
    put(3 + i, 13 - i, (95, 98, 110, 255))
put(1, 14, (230, 232, 240, 255)); put(0, 15, (245, 246, 250, 255))   # punta
for i in range(3, 6):                       # agarre
    put(2 + i, 13 - i, (40, 40, 48, 255)); put(3 + i, 13 - i, (30, 30, 36, 255))
for (x, y, c) in ((10, 3, (210, 40, 45, 255)), (11, 2, (230, 60, 60, 255)), (12, 1, (210, 40, 45, 255)),
                  (9, 3, (170, 30, 38, 255)), (10, 2, (210, 40, 45, 255)), (11, 1, (170, 30, 38, 255)),
                  (11, 4, (230, 60, 60, 255)), (12, 3, (210, 40, 45, 255)), (13, 2, (170, 30, 38, 255))):
    put(x, y, c)
d.save(os.path.join(OUT, "item/dart.png"))

# ---- diana para la pantalla (256x256)
N = 256
img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
dr = ImageDraw.Draw(img)
R = N / 2 - 2
c = N / 2
ORDER = [20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5]
BLACK, CREAM, RED, GREEN = (28, 28, 30), (240, 228, 190), (200, 40, 40), (40, 140, 60)

def ring(r0, r1, colors):
    for i in range(20):
        a0 = -90 + i * 18 - 9
        a1 = a0 + 18
        col = colors[i % 2]
        dr.pieslice([c - R * r1, c - R * r1, c + R * r1, c + R * r1], a0, a1, fill=col)
        # recorte interior
    if r0 > 0:
        pass

dr.ellipse([c - R, c - R, c + R, c + R], fill=BLACK)
for (r1, cols) in ((0.9, (RED, GREEN)), (0.775, (BLACK, CREAM)), (0.55, (RED, GREEN)), (0.45, (BLACK, CREAM))):
    ring(0, r1, cols)
# volver a pintar anillos interiores para que no tapen: se dibuja de mayor a menor radio con círculos
dr.ellipse([c - R * 0.9, c - R * 0.9, c + R * 0.9, c + R * 0.9], fill=BLACK)
def band(r_out, r_in, col_even, col_odd):
    for i in range(20):
        a0 = -90 + i * 18 - 9
        col = col_even if i % 2 == 0 else col_odd
        dr.pieslice([c - R * r_out, c - R * r_out, c + R * r_out, c + R * r_out], a0, a0 + 18, fill=col)
    dr.ellipse([c - R * r_in, c - R * r_in, c + R * r_in, c + R * r_in], fill=(0, 0, 0, 0))
# se arma capa a capa con una imagen auxiliar por anillo
img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
def draw_band(r_out, r_in, e, o):
    layer = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    ld = ImageDraw.Draw(layer)
    for i in range(20):
        a0 = -90 + i * 18 - 9
        ld.pieslice([c - R * r_out, c - R * r_out, c + R * r_out, c + R * r_out], a0, a0 + 18, fill=e if i % 2 == 0 else o)
    mask = Image.new("L", (N, N), 0)
    md = ImageDraw.Draw(mask)
    md.ellipse([c - R * r_in, c - R * r_in, c + R * r_in, c + R * r_in], fill=255)
    layer.paste((0, 0, 0, 0), (0, 0), mask)
    img.alpha_composite(layer)
ImageDraw.Draw(img).ellipse([c - R, c - R, c + R, c + R], fill=BLACK)
draw_band(0.9, 0.775, (RED), (GREEN))
draw_band(0.775, 0.55, BLACK, CREAM)
draw_band(0.55, 0.45, RED, GREEN)
draw_band(0.45, 0.225, BLACK, CREAM)
dd = ImageDraw.Draw(img)
dd.ellipse([c - R * 0.225, c - R * 0.225, c + R * 0.225, c + R * 0.225], fill=GREEN)
dd.ellipse([c - R * 0.1125, c - R * 0.1125, c + R * 0.1125, c + R * 0.1125], fill=RED)
try:
    font = ImageFont.load_default(size=15)
except TypeError:
    font = ImageFont.load_default()
for i, n in enumerate(ORDER):
    ang = math.radians(i * 18)
    r = R * 0.95
    x, y = c + r * math.sin(ang), c - r * math.cos(ang)
    dd.text((x, y), str(n), fill=(255, 255, 255, 255), font=font, anchor="mm")
img.save(os.path.join(OUT, "gui/dartboard.png"))
print("ok")
