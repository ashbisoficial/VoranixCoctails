#!/usr/bin/env python3
"""Genera texturas, modelos, blockstates, loot, items, lang y ModBlocks.java a partir de decor_spec."""
import json
import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from decor_spec import BLOCKS  # noqa: E402

ROOT = sys.argv[1]  # carpeta del proyecto
RES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RES, "assets/cocteleria")
DATA = os.path.join(RES, "data/cocteleria")
JAVA = os.path.join(ROOT, "src/main/java/com/cocteleria/registry/ModBlocks.java")

FACES = {"north": "n", "south": "s", "east": "e", "west": "w", "up": "u", "down": "d"}


def clamp(v):
    return max(0, min(255, int(round(v))))


def lit(c, f, n=0):
    return (clamp(c[0] * f + n), clamp(c[1] * f + n), clamp(c[2] * f + n))


def paint(kind, cols, rng):
    c1 = cols[0] if cols else (255, 255, 255)
    c2 = cols[1] if len(cols) > 1 else c1
    c3 = cols[2] if len(cols) > 2 else c1
    t = Image.new("RGBA", (8, 8))
    for y in range(8):
        for x in range(8):
            g = 1.14 - 0.035 * x
            n = rng.uniform(-5, 5)
            a = 255
            if kind == "glass":
                c = lit(c1, g, n)
                if x == 1:
                    c = lit(c1, 1.25, 28)
                if x == 7:
                    c = lit(c1, 0.7)
                if (x, y) in ((2, 1), (2, 2), (3, 1)):
                    c = lit(c1, 1.4, 50)
            elif kind == "tglass":
                edge = x in (0, 7) or y in (0, 7)
                c = (214, 238, 250) if edge else (196, 226, 245)
                a = 175 if edge else 70
                if x == 2 and 1 <= y <= 5:
                    c, a = (255, 255, 255), 150
            elif kind == "metal":
                c = lit(c1, g + (0.04 if y % 2 == 0 else 0), n)
                if x in (1, 2):
                    c = lit(c1, 1.15, 28)
                if x == 7:
                    c = lit(c1, 0.72)
            elif kind == "wood":
                c = lit(c1, g, n)
                if y % 3 == 1:
                    c = lit(c1, 0.82 * g)
                if (x, y) in ((3, 2), (4, 2), (4, 3)) and rng.random() < 0.7:
                    c = lit(c1, 0.62)
            elif kind == "label":
                c = lit(c1, 1.05 - 0.02 * x, n * 0.5)
                if y in (0, 1, 6, 7):
                    c = lit(c2, 1.0 - 0.02 * x)
                elif y == 2 and 2 <= x <= 5:
                    c = c3
                elif y == 4 and 1 <= x <= 6:
                    c = c3
                elif y == 5 and 1 <= x <= 4:
                    c = c3
            elif kind == "ice":
                c = lit(c1, 1.0, n)
                if rng.random() < 0.22:
                    c = (240, 252, 255)
                if x in (0, 7) or y in (0, 7):
                    c = (225, 246, 255)
                if (x + y) % 5 == 0:
                    c = lit(c1, 0.88)
            elif kind == "flesh":
                dx, dy = x - 3.5, y - 3.5
                d = math.hypot(dx, dy)
                ang = math.atan2(dy, dx)
                if d > 3.6:
                    c = c2
                elif d > 2.9:
                    c = (246, 246, 226)
                elif d < 0.8 or abs(math.sin(ang * 4)) < 0.18:
                    c = (246, 246, 226)
                else:
                    c = lit(c1, 1.0, n)
            elif kind == "cork":
                c = lit(c1, g, n * 2.5)
            elif kind == "foil":
                c = lit(c1, g, n * 0.5)
                if x == 2:
                    c = lit(c1, 1.5, 40)
                if x == 5:
                    c = lit(c1, 1.2, 15)
            elif kind == "felt":
                c = lit(c1, 1.0, n * 2.2)
            elif kind == "cardback":
                if x in (0, 7) or y in (0, 7):
                    c = (245, 245, 240)
                else:
                    c = lit(c1, 0.8 if (x + y) % 3 == 0 else 1.0)
            elif kind == "cardedge":
                c = (244, 244, 240) if y % 2 == 0 else (206, 206, 204)
            elif kind == "cardface":
                heart = {(1, 1), (2, 1), (5, 1), (6, 1)} | {(i, 2) for i in range(1, 7)} | {(i, 3) for i in range(1, 7)} \
                    | {(i, 4) for i in range(2, 6)} | {(3, 5), (4, 5)}
                if c2 == 1:
                    heart = {(hx, 6 - hy) for (hx, hy) in heart} | {(3, 6), (4, 6)}
                c = c1 if (x, y) in heart else (248, 248, 244)
            elif kind == "die":
                pips = {1: [(3, 3)], 2: [(1, 1), (5, 5)], 3: [(1, 1), (3, 3), (5, 5)],
                        4: [(1, 1), (5, 1), (1, 5), (5, 5)], 5: [(1, 1), (5, 1), (3, 3), (1, 5), (5, 5)],
                        6: [(1, 1), (5, 1), (1, 3), (5, 3), (1, 5), (5, 5)]}[c3]
                inpip = any(px <= x <= px + 1 and py <= y <= py + 1 for px, py in pips)
                c = c2 if inpip else lit(c1, 1.0 - 0.015 * x, n * 0.4)
                if (x in (0, 7) or y in (0, 7)) and not inpip:
                    c = lit(c, 0.93)
            elif kind == "chips":
                c = lit(c1, 1.0 - 0.03 * x, n)
                if y % 2 == 1:
                    c = lit(c1, 0.78)
                if y % 2 == 0 and x in (3, 4):
                    c = c2
            else:  # plain
                c = lit(c1, g, n)
            t.putpixel((x, y), (c[0], c[1], c[2], a))
    return t


BIG = {"target", "chess"}


def paint_big(kind, cols, rng):
    t = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if kind == "target":
                dx, dy = x + 0.5 - 8, y + 0.5 - 8
                d = math.hypot(dx, dy)
                if d > 8:
                    continue
                theta = math.degrees(math.atan2(dx, -dy)) % 360
                wedge = int(((theta + 9) % 360) / 18) % 2
                if d > 7.2:
                    c = (28, 28, 30)
                elif d > 6.2:
                    c = (200, 40, 40) if wedge else (40, 140, 60)
                elif d > 4.4:
                    c = (30, 30, 32) if wedge else (240, 228, 190)
                elif d > 3.6:
                    c = (200, 40, 40) if wedge else (40, 140, 60)
                elif d > 1.8:
                    c = (30, 30, 32) if wedge else (240, 228, 190)
                elif d > 0.9:
                    c = (40, 140, 60)
                else:
                    c = (205, 35, 35)
            else:
                dark = ((x // 2) + (y // 2)) % 2
                c = (118, 72, 44) if dark else (232, 206, 160)
                c = lit(c, 1.0, rng.uniform(-4, 4))
            t.putpixel((x, y), (c[0], c[1], c[2], 255))
    return t


def build_block(b):
    rng = random.Random(b["id"])
    mats = list(b["mats"].keys())
    tex = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    uv = {}
    used = [[False] * 4 for _ in range(4)]
    for name in [m for m in mats if b["mats"][m][0] in BIG]:
        for (col, row) in ((0, 0), (2, 0), (0, 2), (2, 2)):
            if not any(used[r][c] for r in (row, row + 1) for c in (col, col + 1)):
                for r in (row, row + 1):
                    for c in (col, col + 1):
                        used[r][c] = True
                spec = b["mats"][name]
                tex.paste(paint_big(spec[0], spec[1:], rng), (col * 8, row * 8))
                uv[name] = [4 * col + 0.1, 4 * row + 0.1, 4 * col + 7.9, 4 * row + 7.9]
                break
    for name in [m for m in mats if b["mats"][m][0] not in BIG]:
        slot = next((r, c) for r in range(4) for c in range(4) if not used[r][c])
        row, col = slot
        used[row][col] = True
        spec = b["mats"][name]
        tex.paste(paint(spec[0], spec[1:], rng), (col * 8, row * 8))
        uv[name] = [4 * col + 0.1, 4 * row + 0.1, 4 * col + 3.9, 4 * row + 3.9]
    os.makedirs(os.path.join(ASSETS, "textures/block"), exist_ok=True)
    tex.save(os.path.join(ASSETS, "textures/block/%s.png" % b["id"]))

    elements = []
    for p in b["parts"]:
        faces = {}
        for fname in FACES:
            if p["only"] and fname not in p["only"]:
                continue
            if fname == "down" and p["f"][1] == 0:
                continue
            m = p["fm"].get(fname, p["fm"].get(fname[0], p["m"]))
            faces[fname] = {"uv": uv[m], "texture": "#0"}
        el = {"from": p["f"], "to": p["t"], "faces": faces}
        if p.get("rot"):
            el["rotation"] = {"origin": p["rot"][0], "axis": "y", "angle": p["rot"][1]}
        elements.append(el)
    cells = b.get("cells")
    minx = min(e["from"][0] for e in elements); maxx = max(e["to"][0] for e in elements)
    minz = min(e["from"][2] for e in elements); maxz = max(e["to"][2] for e in elements)
    maxy = max(e["to"][1] for e in elements)
    span = max(maxx - minx, maxz - minz, maxy) / 16.0
    k = 1.0 if span <= 1.0 else 1.0 / span

    def display(scale):
        return {
            "gui": {"rotation": [30, 225, 0], "translation": [0, -1, 0], "scale": [0.85 * scale] * 3},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.5 * scale] * 3},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, -1, 0], "scale": [0.7 * scale] * 3},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.4 * scale] * 3},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 3, 0], "scale": [0.45 * scale] * 3},
        }

    tex_ref = {"0": "cocteleria:block/" + b["id"], "particle": "cocteleria:block/" + b["id"]}
    model = {"parent": "minecraft:block/block", "render_type": "minecraft:" + b["render"],
             "textures": tex_ref, "elements": elements, "display": display(k)}
    for sub in ("models/block", "models/item", "blockstates"):
        os.makedirs(os.path.join(ASSETS, sub), exist_ok=True)
    with open(os.path.join(ASSETS, "models/block/%s.json" % b["id"]), "w") as f:
        json.dump(model, f, indent=1)

    if cells:
        # modelo de item independiente, centrado en el bloque y reducido
        dx, dz = 8 - (minx + maxx) / 2, 8 - (minz + maxz) / 2
        shifted = json.loads(json.dumps(elements))
        for e in shifted:
            e["from"] = [e["from"][0] + dx, e["from"][1], e["from"][2] + dz]
            e["to"] = [e["to"][0] + dx, e["to"][1], e["to"][2] + dz]
            if "rotation" in e:
                o = e["rotation"]["origin"]
                e["rotation"]["origin"] = [o[0] + dx, o[1], o[2] + dz]
        item = dict(model, elements=shifted)
        with open(os.path.join(ASSETS, "models/item/%s.json" % b["id"]), "w") as f:
            json.dump(item, f, indent=1)
    else:
        with open(os.path.join(ASSETS, "models/item/%s.json" % b["id"]), "w") as f:
            json.dump({"parent": "cocteleria:block/" + b["id"]}, f, indent=1)

    mp = "cocteleria:block/" + b["id"]
    empty = "cocteleria:block/empty"
    rots = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for facing, y in rots.items():
        for part in range(len(cells) if cells else 1):
            key = "facing=%s,part=%d" % (facing, part) if cells else "facing=%s" % facing
            if cells and part > 0:
                variants[key] = {"model": empty}
            else:
                v = {"model": mp}
                if y:
                    v["y"] = y
                variants[key] = v
    with open(os.path.join(ASSETS, "blockstates/%s.json" % b["id"]), "w") as f:
        json.dump({"variants": variants}, f, indent=1)
    if not cells:
        loot = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:item", "name": "cocteleria:" + b["id"]}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
        os.makedirs(os.path.join(DATA, "loot_tables/blocks"), exist_ok=True)
        with open(os.path.join(DATA, "loot_tables/blocks/%s.json" % b["id"]), "w") as f:
            json.dump(loot, f, indent=1)


def shape_boxes(b):
    if "shape" in b:
        return b["shape"]
    return [p["f"] + p["t"] for p in b["parts"] if p["shape"]]


def num(v):
    return ("%g" % v)


def write_java():
    lines = []
    lines.append("""package com.cocteleria.registry;

import com.cocteleria.CocteleriaMod;
import com.cocteleria.block.DecorBlock;
import com.cocteleria.block.GameDecorBlock;
import com.cocteleria.game.BarGame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Bloques decorativos y de juegos. Generado desde tools/decor_spec.py. */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CocteleriaMod.MODID);

    /** Bloques que tienen una partida (usan el BlockEntity de juego). */
    public static final List<RegistryObject<Block>> GAME_BLOCKS = new ArrayList<>();
""")
    for b in BLOCKS:
        boxes = ", ".join("{" + ", ".join(num(v) for v in bx) + "}" for bx in shape_boxes(b))
        game = "BarGame.%s" % b["game"] if b.get("game") else "null"
        cells = "new int[][]{%s}" % ", ".join("{%d, %d}" % c for c in b["cells"]) if b.get("cells") else "null"
        lines.append("    public static final RegistryObject<Block> %s = decor(\"%s\", SoundType.%s, %s, %s,\n            new double[][]{%s},\n            %s);\n"
                     % (b["id"].upper(), b["id"], b["sound"], "true" if b.get("wall") else "false", game, boxes, cells))
    lines.append("""    private static RegistryObject<Block> decor(String name, SoundType sound, boolean wall, BarGame game,
                                               double[][] boxes, int[][] cells) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of().strength(0.3F).sound(sound).noOcclusion();
            return game == null ? new DecorBlock(props, boxes, wall, cells) : new GameDecorBlock(props, boxes, wall, cells, game);
        });
        if (game != null) {
            GAME_BLOCKS.add(block);
        }
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
""")
    with open(JAVA, "w") as f:
        f.write("\n".join(lines))


def update_lang():
    for lang, key in (("es_es", "es"), ("es_cl", "es"), ("en_us", "en")):
        path = os.path.join(ASSETS, "lang/%s.json" % lang)
        with open(path, encoding="utf-8") as f:
            d = json.load(f)
        for b in BLOCKS:
            d["block.cocteleria.%s" % b["id"]] = b[key]
        with open(path, "w", encoding="utf-8") as f:
            json.dump(d, f, indent=2, ensure_ascii=False)


if __name__ == "__main__":
    os.makedirs(os.path.join(ASSETS, "models/block"), exist_ok=True)
    with open(os.path.join(ASSETS, "models/block/empty.json"), "w") as f:
        json.dump({"textures": {"particle": "minecraft:block/oak_planks"}}, f)
    for b in BLOCKS:
        build_block(b)
    write_java()
    update_lang()
    print("bloques:", len(BLOCKS))
