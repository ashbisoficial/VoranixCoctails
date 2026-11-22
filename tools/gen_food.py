"""Genera texturas, modelos, blockstates, loot y lang de la comida (plato + mordiscos, e ítems comestibles)."""
import json, os, random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(ROOT, "assets", "cocteleria")
D = os.path.join(ROOT, "data", "cocteleria")
for p in ("textures/block", "textures/item", "models/block", "models/item", "blockstates"):
    os.makedirs(os.path.join(A, p), exist_ok=True)
os.makedirs(os.path.join(D, "loot_tables", "blocks"), exist_ok=True)

rnd = random.Random(7)

# ---------- texturas de material (16x16, ruido) ----------
MATS = {
    "plate":    ((245, 245, 242), 6, None),
    "plate_in": ((226, 232, 238), 5, ((120, 160, 205), 0.05)),
    "bun":      ((214, 150, 70), 14, ((250, 235, 200), 0.07)),
    "hotbun":   ((230, 190, 120), 10, None),
    "patty":    ((110, 62, 38), 14, ((70, 38, 22), 0.12)),
    "lettuce":  ((86, 170, 62), 16, ((140, 210, 100), 0.10)),
    "tomato":   ((205, 52, 40), 12, ((235, 110, 90), 0.08)),
    "cheese":   ((247, 200, 52), 8, None),
    "fries":    ((236, 190, 70), 14, ((200, 140, 40), 0.10)),
    "sausage":  ((176, 70, 56), 10, ((140, 50, 40), 0.08)),
    "mayo":     ((250, 248, 232), 4, None),
    "avocado":  ((128, 170, 70), 12, ((90, 130, 50), 0.10)),
    "ketchup":  ((190, 30, 30), 6, None),
}

def clamp(v):
    return max(0, min(255, int(v)))

for name, (base, var, spec) in MATS.items():
    im = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-var, var)
            c = tuple(clamp(b + n) for b in base)
            if spec and rnd.random() < spec[1]:
                c = spec[0]
            im.putpixel((x, y), c)
    im.save(os.path.join(A, "textures/block", f"food_{name}.png"))

# ---------- iconos de ítem (16x16) ----------
def icon(name, fn):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    fn(d, im)
    im.save(os.path.join(A, "textures/item", f"{name}.png"))
    return im

OUT = (60, 36, 20, 255)
def rect(d, x0, y0, x1, y1, c):
    d.rectangle([x0, y0, x1, y1], fill=c)

def ic_burger(d, im):
    rect(d, 3, 3, 12, 3, OUT); rect(d, 2, 4, 13, 5, OUT)
    rect(d, 3, 4, 12, 6, (214, 150, 70, 255)); rect(d, 4, 3, 11, 3, (224, 165, 85, 255))
    for x, y in ((5, 4), (8, 4), (10, 5), (6, 5)):
        im.putpixel((x, y), (250, 235, 200, 255))
    rect(d, 2, 7, 13, 7, (86, 170, 62, 255)); rect(d, 3, 8, 12, 8, (205, 52, 40, 255))
    rect(d, 2, 9, 13, 10, (110, 62, 38, 255)); rect(d, 2, 11, 13, 11, (247, 200, 52, 255))
    rect(d, 3, 12, 12, 13, (222, 160, 80, 255)); rect(d, 3, 14, 12, 14, OUT)
    rect(d, 2, 7, 2, 11, OUT); rect(d, 13, 7, 13, 11, OUT)

def ic_fries(d, im):
    for i, (x, h) in enumerate(((3, 6), (5, 4), (7, 3), (9, 5), (11, 4), (12, 7), (4, 5))):
        rect(d, x, h, x + 1, 9, (240, 196, 72, 255)); rect(d, x, h, x, 9, (255, 220, 110, 255))
    rect(d, 2, 9, 13, 9, OUT)
    for y in range(10, 15):
        rect(d, 2 + (y - 10) // 2, y, 13 - (y - 10) // 2, y, (205, 40, 40, 255))
    rect(d, 6, 11, 9, 12, (250, 235, 200, 255))

def ic_completo(d, im):
    rect(d, 1, 8, 14, 12, (232, 192, 124, 255)); rect(d, 1, 8, 14, 8, (200, 150, 80, 255)); rect(d, 1, 12, 14, 12, (190, 140, 70, 255))
    rect(d, 0, 6, 15, 8, (176, 70, 56, 255)); rect(d, 0, 6, 15, 6, (210, 100, 80, 255))
    rect(d, 2, 4, 13, 5, (128, 170, 70, 255))
    for x in (3, 6, 9, 12):
        rect(d, x, 3, x + 1, 4, (205, 52, 40, 255))
    for x in range(2, 14, 2):
        im.putpixel((x, 2), (250, 248, 232, 255))
    rect(d, 2, 3, 13, 3, (250, 248, 232, 255))

def ic_salchipapas(d, im):
    d.ellipse([0, 7, 15, 15], fill=(245, 245, 242, 255), outline=(150, 170, 200, 255))
    for x, y in ((3, 8), (5, 6), (7, 7), (9, 5), (11, 7), (6, 9), (10, 9), (4, 10)):
        rect(d, x, y, x + 1, y + 3, (240, 196, 72, 255))
    for x, y in ((4, 8), (8, 7), (11, 9), (6, 11)):
        rect(d, x, y, x + 2, y + 1, (176, 70, 56, 255))
    for x in range(3, 13):
        im.putpixel((x, 9), (205, 40, 40, 255))
    for x in range(4, 12, 2):
        im.putpixel((x, 10), (250, 248, 232, 255))

def ic_empanada(d, im):
    d.pieslice([0, 1, 15, 15], 180, 360, fill=(214, 140, 60, 255))
    d.pieslice([0, 1, 15, 15], 180, 360, outline=OUT)
    rect(d, 1, 8, 14, 8, OUT)
    rect(d, 2, 6, 13, 7, (222, 152, 70, 255)); rect(d, 4, 4, 11, 5, (235, 170, 85, 255))
    for x in range(1, 15, 2):
        im.putpixel((x, 9), (170, 105, 45, 255))
    rect(d, 1, 9, 14, 9, (196, 124, 52, 255))
    for x in range(1, 15, 2):
        im.putpixel((x, 10), (150, 90, 38, 255))

def sopaipilla_base(d, im):
    d.ellipse([1, 2, 14, 13], fill=(236, 170, 56, 255), outline=(150, 88, 28, 255))
    for x, y in ((4, 5), (8, 4), (10, 7), (5, 9), (9, 10), (7, 7)):
        im.putpixel((x, y), (198, 120, 36, 255))
    for x, y in ((6, 4), (11, 5), (4, 7)):
        im.putpixel((x, y), (252, 214, 110, 255))

def ic_sopaipilla(d, im):
    sopaipilla_base(d, im)

def ic_sopa_pebre(d, im):
    sopaipilla_base(d, im)
    for x, y, c in ((4, 6, (205, 52, 40)), (6, 5, (86, 170, 62)), (8, 6, (205, 52, 40)), (10, 5, (240, 240, 220)),
                    (5, 8, (86, 170, 62)), (7, 8, (205, 52, 40)), (9, 8, (86, 170, 62)), (11, 7, (205, 52, 40)),
                    (6, 10, (205, 52, 40)), (8, 10, (86, 170, 62))):
        im.putpixel((x, y), c + (255,))
        im.putpixel((x + 1, y), c + (255,))

def ic_plate(d, im):
    d.ellipse([0, 5, 15, 14], fill=(245, 245, 242, 255), outline=(150, 170, 200, 255))
    d.ellipse([3, 7, 12, 12], fill=(226, 232, 238, 255), outline=(190, 205, 225, 255))

icon("hamburger", ic_burger); icon("french_fries", ic_fries); icon("completo", ic_completo)
icon("salchipapas", ic_salchipapas); icon("empanada", ic_empanada); icon("sopaipilla", ic_sopaipilla)
icon("sopaipilla_pebre", ic_sopa_pebre); icon("plate", ic_plate)

# ---------- modelos por mordisco ----------
def sticks(n, x0, x1, z0, z1, h0, h1, y0, seed):
    r = random.Random(seed)
    out = []
    for _ in range(n):
        x = r.uniform(x0, x1); z = r.uniform(z0, z1); h = r.uniform(h0, h1)
        tilt = r.choice([0, 0, 22.5, -22.5])
        out.append({"b": [round(x, 2), y0, round(z, 2), round(x + 1.1, 2), round(y0 + h, 2), round(z + 1.1, 2)],
                    "m": "fries", "tilt": tilt})
    return out

FOODS = {
    "hamburger": {"bites": 4, "x": (2.5, 13.5), "els": [
        {"b": [3, 1.2, 3, 13, 3.2, 13], "m": "hotbun"},
        {"b": [2.5, 3.2, 2.5, 13.5, 3.8, 13.5], "m": "lettuce"},
        {"b": [3, 3.8, 3, 13, 5.8, 13], "m": "patty"},
        {"b": [2.5, 5.8, 2.5, 13.5, 6.2, 13.5], "m": "cheese"},
        {"b": [3, 6.2, 3, 13, 7.0, 13], "m": "tomato"},
        {"b": [3, 7.0, 3, 13, 9.8, 13], "m": "bun"},
        {"b": [4, 9.8, 4, 12, 10.6, 12], "m": "bun"}]},
    "french_fries": {"bites": 3, "x": (3, 13),
        "els": sticks(16, 3.2, 11.8, 3.5, 11.5, 4, 7, 1.2, 11)},
    "completo": {"bites": 3, "x": (1.5, 14.5), "els": [
        {"b": [2, 1.2, 5, 14, 3.4, 11], "m": "hotbun"},
        {"b": [1.5, 3.2, 6.5, 14.5, 4.6, 9.5], "m": "sausage"},
        {"b": [2.5, 4.6, 6, 13.5, 5.4, 10], "m": "avocado"},
        {"b": [3, 5.4, 6.3, 13, 6.0, 9.7], "m": "tomato"},
        {"b": [2.5, 6.0, 7, 13.5, 6.5, 9], "m": "mayo"}]},
    "salchipapas": {"bites": 5, "x": (2.5, 13.5), "els":
        sticks(14, 3.2, 11.8, 3.5, 11.5, 2.5, 4.5, 1.2, 23) + [
        {"b": [3, 4.6, 4, 5.6, 5.4, 6.6], "m": "sausage"},
        {"b": [6.3, 4.8, 8, 8.9, 5.6, 10.6], "m": "sausage"},
        {"b": [9.6, 4.6, 4.5, 12.2, 5.4, 7.1], "m": "sausage"},
        {"b": [4.5, 4.8, 9.5, 7.1, 5.6, 12.1], "m": "sausage"},
        {"b": [10, 4.8, 9.5, 12.6, 5.6, 12.1], "m": "sausage"},
        {"b": [3, 5.0, 7.4, 13, 5.5, 8.4], "m": "ketchup"},
        {"b": [3, 5.0, 9.0, 13, 5.5, 9.8], "m": "mayo"}]},
}
PLATE_EL = [
    {"b": [1, 0, 1, 15, 0.8, 15], "m": "plate"},
    {"b": [2, 0.8, 2, 14, 1.2, 14], "m": "plate_in"},
]

def face_set(tex):
    return {f: {"uv": [0, 0, 16, 16], "texture": "#" + tex, "cullface": None} for f in
            ("north", "south", "east", "west", "up", "down")}

def element(e):
    x0, y0, z0, x1, y1, z1 = e["b"]
    el = {"from": [x0, y0, z0], "to": [x1, y1, z1],
          "faces": {f: {"uv": [0, 0, 16, 16], "texture": "#" + e["m"]} for f in ("north", "south", "east", "west", "up", "down")}}
    if e.get("tilt"):
        el["rotation"] = {"origin": [(x0 + x1) / 2, y0, (z0 + z1) / 2], "axis": "z", "angle": e["tilt"]}
    return el

def model(els):
    mats = sorted({e["m"] for e in els})
    return {"textures": dict({"particle": "cocteleria:block/food_plate"},
                             **{m: f"cocteleria:block/food_{m}" for m in mats}),
            "elements": [element(e) for e in els]}

def clip(els, cut):
    out = []
    for e in els:
        b = list(e["b"])
        if e.get("tilt"):
            if (b[0] + b[3]) / 2 >= cut:
                out.append(e)
            continue
        if b[3] - max(b[0], cut) < 0.35:
            continue
        b[0] = max(b[0], cut)
        out.append(dict(e, b=b))
    return out

def write(path, obj):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)

write(os.path.join(A, "models/block/plate.json"), model(PLATE_EL))
for name, spec in FOODS.items():
    x0, x1 = spec["x"]
    n = spec["bites"]
    for i in range(n):
        cut = x0 + (x1 - x0) * i / n
        write(os.path.join(A, f"models/block/{name}_{i}.json"), model(PLATE_EL + clip(spec["els"], cut)))
    variants = {f"bites={i}": {"model": f"cocteleria:block/{name}_{min(i, n - 1)}"} for i in range(5)}
    write(os.path.join(A, f"blockstates/{name}.json"), {"variants": variants})
    write(os.path.join(A, f"models/item/{name}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"cocteleria:item/{name}"}})
    write(os.path.join(D, f"loot_tables/blocks/{name}.json"), {
        "type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": f"cocteleria:{name}"}],
        "conditions": [{"condition": "minecraft:block_state_property", "block": f"cocteleria:{name}", "properties": {"bites": "0"}},
                       {"condition": "minecraft:survives_explosion"}]}]})

write(os.path.join(A, "blockstates/plate.json"), {"variants": {"": {"model": "cocteleria:block/plate"}}})
write(os.path.join(A, "models/item/plate.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "cocteleria:item/plate"}})
write(os.path.join(D, "loot_tables/blocks/plate.json"), {
    "type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": "cocteleria:plate"}],
    "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
for n in ("empanada", "sopaipilla", "sopaipilla_pebre"):
    write(os.path.join(A, f"models/item/{n}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"cocteleria:item/{n}"}})

# ---------- lang ----------
NAMES = {
    "es": {"hamburger": "Hamburguesa", "french_fries": "Papas fritas", "completo": "Completo", "salchipapas": "Salchipapas",
           "plate": "Plato", "empanada": "Empanada", "sopaipilla": "Sopaipilla", "sopaipilla_pebre": "Sopaipilla con pebre"},
    "en": {"hamburger": "Hamburger", "french_fries": "French Fries", "completo": "Completo (Chilean Hot Dog)", "salchipapas": "Salchipapas",
           "plate": "Plate", "empanada": "Empanada", "sopaipilla": "Sopaipilla", "sopaipilla_pebre": "Sopaipilla with Pebre"},
}
BLOCKS = ("hamburger", "french_fries", "completo", "salchipapas", "plate")
for lang, key in (("es_cl", "es"), ("es_es", "es"), ("en_us", "en")):
    path = os.path.join(A, "lang", f"{lang}.json")
    data = json.load(open(path, encoding="utf-8"))
    for n, text in NAMES[key].items():
        data[("block.cocteleria." if n in BLOCKS else "item.cocteleria.") + n] = text
    write(path, data)
print("comida generada")
