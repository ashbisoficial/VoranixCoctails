#!/usr/bin/env python3
"""Render isométrico (vista NE) de los modelos para revisarlos."""
import json, math, os, sys
import numpy as np
from PIL import Image, ImageDraw

ROOT = sys.argv[1]; OUT = sys.argv[2]
A = os.path.join(ROOT, "src/main/resources/assets/cocteleria")
S = 8.0
W, H = 250, 280
CX, CY = 232, 206
AX = np.array([-0.866, 0.5]); AZ = np.array([-0.866, -0.5]); AY = np.array([0.0, -1.0])

def proj(p):
    return CX + S * (p[0]*AX[0] + p[1]*AY[0] + p[2]*AZ[0]), CY + S * (p[0]*AX[1] + p[1]*AY[1] + p[2]*AZ[1])

def draw_face(canvas, tex, uv, origin, ue, ve, shade):
    x0, y0 = int(round(uv[0]*2)), int(round(uv[1]*2))
    x1, y1 = int(round(uv[2]*2)), int(round(uv[3]*2))
    src = tex.crop((x0, y0, max(x1, x0+1), max(y1, y0+1)))
    src = src.resize((src.width*16, src.height*16), Image.NEAREST)
    po = np.array(proj(origin)); pu = np.array(proj([origin[i]+ue[i] for i in range(3)])) - po
    pv = np.array(proj([origin[i]+ve[i] for i in range(3)])) - po
    M = np.array([[pu[0], pv[0]], [pu[1], pv[1]]])
    if abs(np.linalg.det(M)) < 1e-6:
        return
    inv = np.linalg.inv(M)
    sw, sh = src.size
    co = (sw*inv[0,0], sw*inv[0,1], sw*(-inv[0,0]*po[0]-inv[0,1]*po[1]),
          sh*inv[1,0], sh*inv[1,1], sh*(-inv[1,0]*po[0]-inv[1,1]*po[1]))
    layer = src.transform((W, H), Image.AFFINE, co, resample=Image.NEAREST)
    arr = np.array(layer).astype(float)
    arr[..., :3] *= shade
    layer = Image.fromarray(arr.clip(0, 255).astype("uint8"), "RGBA")
    canvas.alpha_composite(layer)

def render(name):
    model = json.load(open(os.path.join(A, "models/block/%s.json" % name)))
    tex = Image.open(os.path.join(A, "textures/block/%s.png" % name)).convert("RGBA")
    canvas = Image.new("RGBA", (W, H), (70, 74, 84, 255))
    els = sorted(model["elements"], key=lambda e: sum((e["from"][i]+e["to"][i])/2*s for i, s in enumerate((1, 1, -1))))
    for e in els:
        f, t = e["from"], e["to"]
        dx, dy, dz = t[0]-f[0], t[1]-f[1], t[2]-f[2]
        fc = e["faces"]
        rot = e.get("rotation")
        def R(p):
            if not rot: return p
            o = rot["origin"]; a = math.radians(rot["angle"])
            x, z = p[0]-o[0], p[2]-o[2]
            return [o[0] + x*math.cos(a) - z*math.sin(a), p[1], o[2] + x*math.sin(a) + z*math.cos(a)]
        def Rv(v):
            if not rot: return v
            a = math.radians(rot["angle"])
            return [v[0]*math.cos(a) - v[2]*math.sin(a), v[1], v[0]*math.sin(a) + v[2]*math.cos(a)]
        if "up" in fc:
            draw_face(canvas, tex, fc["up"]["uv"], R([f[0], t[1], f[2]]), Rv([dx, 0, 0]), Rv([0, 0, dz]), 1.0)
        if "east" in fc:
            draw_face(canvas, tex, fc["east"]["uv"], R([t[0], t[1], t[2]]), Rv([0, 0, -dz]), Rv([0, -dy, 0]), 0.66)
        if "north" in fc:
            draw_face(canvas, tex, fc["north"]["uv"], R([t[0], t[1], f[2]]), Rv([-dx, 0, 0]), Rv([0, -dy, 0]), 0.84)
    return canvas

names = sorted(f[:-5] for f in os.listdir(os.path.join(A, "models/block")))
cols = 6
rows = math.ceil(len(names) / cols)
sheet = Image.new("RGBA", (cols*W, rows*H), (0, 0, 0, 255))
d = ImageDraw.Draw(sheet)
for i, n in enumerate(names):
    sheet.paste(render(n), ((i % cols)*W, (i // cols)*H))
    d.text(((i % cols)*W + 6, (i // cols)*H + 6), n, fill=(255, 255, 255, 255))
sheet.convert("RGB").save(OUT)
print("ok", len(names))
