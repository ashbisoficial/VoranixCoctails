"""Definición de los bloques decorativos (coordenadas 0-16, frente = cara norte)."""


def box(f, t, m, fm=None, shape=True, only=None, rot=None):
    return dict(f=list(f), t=list(t), m=m, fm=fm or {}, shape=shape, only=only, rot=rot)


def bottle(id_, es, en, glass_c, lab, cap, bw, bh, nh, capkind="metal", cap_h=1.3, cap_down=0.0):
    c = 8
    top = bh + 1.5 + nh
    mats = {"glass": ("glass", glass_c), "label": ("label",) + lab, "cap": (capkind, cap)}
    parts = [
        box([c - bw, 0, c - bw], [c + bw, bh, c + bw], "glass"),
        box([c - bw + 1, bh, c - bw + 1], [c + bw - 1, bh + 1.5, c + bw - 1], "glass"),
        box([7, bh + 1.5, 7], [9, top, 9], "glass"),
        box([6.8, top - cap_down, 6.8], [9.2, top + cap_h - cap_down, 9.2], "cap"),
        box([c - bw, 1.5, c - bw - 0.06], [c + bw, bh - 2, c - bw], "label", only=["north"], shape=False),
    ]
    return dict(id=id_, es=es, en=en, sound="GLASS", render="cutout", mats=mats, parts=parts)


METAL = (200, 205, 215)
GOLD = (222, 182, 62)

BLOCKS = []

BLOCKS.append(bottle("whisky_bottle", "Botella de whisky", "Whisky Bottle", (165, 92, 22),
                     ((236, 222, 182), (150, 32, 32), (70, 35, 25)), (115, 74, 42), 3, 8, 3, capkind="cork"))
BLOCKS.append(bottle("rum_bottle", "Botella de ron", "Rum Bottle", (72, 40, 22),
                     ((32, 27, 27), (222, 182, 62), (232, 192, 72)), GOLD, 3.5, 7, 3))
BLOCKS.append(bottle("tequila_bottle", "Botella de tequila", "Tequila Bottle", (226, 216, 150),
                     ((64, 142, 72), (240, 210, 80), (240, 236, 205)), (150, 105, 60), 3.5, 7, 3, capkind="cork"))
BLOCKS.append(bottle("vodka_bottle", "Botella de vodka", "Vodka Bottle", (205, 228, 242),
                     ((246, 246, 250), (60, 110, 200), (30, 60, 130)), METAL, 2.5, 10, 3))
BLOCKS.append(bottle("gin_bottle", "Botella de gin", "Gin Bottle", (52, 152, 122),
                     ((236, 236, 216), (30, 112, 92), (20, 70, 60)), METAL, 3, 8, 3))
BLOCKS.append(bottle("wine_bottle", "Botella de vino", "Wine Bottle", (30, 76, 42),
                     ((236, 228, 200), (130, 22, 42), (100, 20, 35)), (132, 22, 42), 2.5, 8, 5,
                     capkind="foil", cap_h=3, cap_down=2.5))

# ---- coctelera
BLOCKS.append(dict(
    id="shaker", es="Coctelera", en="Cocktail Shaker", sound="METAL", render="cutout",
    mats={"metal": ("metal", METAL), "gold": ("metal", GOLD)},
    parts=[
        box([5, 0, 5], [11, 5, 11], "metal"),
        box([4.8, 4.6, 4.8], [11.2, 5.4, 11.2], "gold"),
        box([5.5, 5.4, 5.5], [10.5, 9, 10.5], "metal"),
        box([6.5, 9, 6.5], [9.5, 11, 9.5], "metal"),
        box([7.2, 11, 7.2], [8.8, 12.2, 8.8], "gold"),
    ]))

# ---- cubo de hielo
BLOCKS.append(dict(
    id="ice_bucket", es="Cubo de hielo", en="Ice Bucket", sound="METAL", render="cutout",
    mats={"metal": ("metal", METAL), "gold": ("metal", GOLD), "ice": ("ice", (190, 232, 250))},
    parts=[
        box([4, 0, 4], [12, 1, 12], "metal"),
        box([4, 1, 4], [12, 8, 4.6], "metal"),
        box([4, 1, 11.4], [12, 8, 12], "metal"),
        box([4, 1, 4.6], [4.6, 8, 11.4], "metal"),
        box([11.4, 1, 4.6], [12, 8, 11.4], "metal"),
        box([3.8, 8, 3.8], [12.2, 8.6, 4.8], "gold"),
        box([3.8, 8, 11.2], [12.2, 8.6, 12.2], "gold"),
        box([3.8, 8, 4.8], [4.8, 8.6, 11.2], "gold"),
        box([11.2, 8, 4.8], [12.2, 8.6, 11.2], "gold"),
        box([4.6, 1, 4.6], [11.4, 6.5, 11.4], "ice"),
        box([5, 6.5, 5.4], [8, 9, 8.2], "ice"),
        box([8.2, 6.5, 7.6], [11, 8.6, 10.6], "ice"),
        box([5.4, 6.5, 8.6], [7.6, 8.2, 10.6], "ice"),
        box([3, 4, 7], [4, 6, 9], "gold"),
        box([12, 4, 7], [13, 6, 9], "gold"),
        box([9, 6.5, 5.2], [9.8, 11.5, 6], "metal"),
    ],
    shape=[[3, 0, 4, 13, 9, 12]]))

# ---- vasos y copas (translucidos)
G = ("tglass", (205, 232, 248))


def shell(x0, x1, z0, z1, y0, y1, t=0.5):
    return [
        box([x0, y0, z0], [x1, y1, z0 + t], "glass"),
        box([x0, y0, z1 - t], [x1, y1, z1], "glass"),
        box([x0, y0, z0 + t], [x0 + t, y1, z1 - t], "glass"),
        box([x1 - t, y0, z0 + t], [x1, y1, z1 - t], "glass"),
    ]


BLOCKS.append(dict(
    id="short_glass", es="Vaso corto", en="Short Glass", sound="GLASS", render="translucent",
    mats={"glass": G},
    parts=[box([5, 0, 5], [11, 1.4, 11], "glass")] + shell(5, 11, 5, 11, 1.4, 6),
    shape=[[5, 0, 5, 11, 6, 11]]))

BLOCKS.append(dict(
    id="tall_glass", es="Vaso alto", en="Highball Glass", sound="GLASS", render="translucent",
    mats={"glass": G},
    parts=[box([5.5, 0, 5.5], [10.5, 1, 10.5], "glass")] + shell(5.5, 10.5, 5.5, 10.5, 1, 9),
    shape=[[5.5, 0, 5.5, 10.5, 9, 10.5]]))

BLOCKS.append(dict(
    id="cocktail_glass", es="Copa de cóctel", en="Cocktail Glass", sound="GLASS", render="translucent",
    mats={"glass": G},
    parts=[
        box([5.5, 0, 5.5], [10.5, 0.6, 10.5], "glass"),
        box([7.5, 0.6, 7.5], [8.5, 5.2, 8.5], "glass"),
        box([6.5, 5.2, 6.5], [9.5, 6.2, 9.5], "glass"),
        box([5.5, 6.2, 5.5], [10.5, 7.2, 10.5], "glass"),
        box([4.5, 7.2, 4.5], [11.5, 8.2, 11.5], "glass"),
        box([3.5, 8.2, 3.5], [12.5, 9.2, 12.5], "glass"),
    ],
    shape=[[5.5, 0, 5.5, 10.5, 0.6, 10.5], [7.5, 0.6, 7.5, 8.5, 5.2, 8.5], [3.5, 5.2, 3.5, 12.5, 9.2, 12.5]]))

BLOCKS.append(dict(
    id="wine_glass", es="Copa de vino", en="Wine Glass", sound="GLASS", render="translucent",
    mats={"glass": G},
    parts=[
        box([6, 0, 6], [10, 0.6, 10], "glass"),
        box([7.5, 0.6, 7.5], [8.5, 5, 8.5], "glass"),
        box([6.5, 5, 6.5], [9.5, 6.5, 9.5], "glass"),
        box([5.5, 6.5, 5.5], [10.5, 7.2, 10.5], "glass"),
    ] + shell(5.5, 10.5, 5.5, 10.5, 7.2, 11.5),
    shape=[[6, 0, 6, 10, 0.6, 10], [7.5, 0.6, 7.5, 8.5, 5, 8.5], [5.5, 5, 5.5, 10.5, 11.5, 10.5]]))

# ---- jigger
BLOCKS.append(dict(
    id="jigger", es="Medidor (jigger)", en="Jigger", sound="METAL", render="cutout",
    mats={"gold": ("metal", GOLD), "metal": ("metal", METAL)},
    parts=[
        box([6, 0, 6], [10, 3, 10], "gold"),
        box([7.25, 3, 7.25], [8.75, 4, 8.75], "metal"),
        box([5.5, 4, 5.5], [10.5, 8, 10.5], "gold"),
        box([5.3, 7.6, 5.3], [10.7, 8, 10.7], "metal"),
    ]))

# ---- cuchara coctelera
BLOCKS.append(dict(
    id="bar_spoon", es="Cuchara coctelera", en="Bar Spoon", sound="METAL", render="cutout",
    mats={"metal": ("metal", METAL), "gold": ("metal", GOLD)},
    parts=[
        box([7.6, 0, 1.5], [8.4, 0.8, 12], "metal"),
        box([7, 0, 0.8], [9, 0.8, 1.6], "gold"),
        box([6.9, 0, 12], [9.1, 1.2, 14.8], "metal"),
        box([7.5, 0.8, 4], [8.5, 1, 4.6], "gold", shape=False),
        box([7.5, 0.8, 7], [8.5, 1, 7.6], "gold", shape=False),
    ],
    shape=[[6.9, 0, 0.8, 9.1, 1.2, 14.8]]))

# ---- cuchillo
BLOCKS.append(dict(
    id="bar_knife", es="Cuchillo de barman", en="Bar Knife", sound="METAL", render="cutout",
    mats={"metal": ("metal", (214, 218, 228)), "wood": ("wood", (92, 58, 36)), "gold": ("metal", GOLD)},
    parts=[
        box([7.3, 0, 1.5], [8.7, 0.4, 9], "metal"),
        box([7, 0, 9], [9, 0.8, 9.6], "gold"),
        box([7.3, 0, 9.6], [8.7, 1, 14.5], "wood"),
    ],
    shape=[[7, 0, 1.5, 9, 1, 14.5]]))

# ---- colador
BLOCKS.append(dict(
    id="strainer", es="Colador", en="Strainer", sound="METAL", render="cutout",
    mats={"metal": ("metal", METAL), "dark": ("wood", (58, 40, 32)), "gold": ("metal", GOLD)},
    parts=[
        box([4, 0, 4], [12, 0.7, 12], "metal"),
        box([5, 0.7, 5], [11, 1, 11], "dark"),
        box([11.5, 0, 7.4], [15.5, 0.7, 8.6], "dark"),
        box([4, 0, 6.5], [5, 1.4, 7.6], "gold"),
        box([4, 0, 8.4], [5, 1.4, 9.5], "gold"),
    ],
    shape=[[4, 0, 4, 15.5, 1.4, 12]]))

# ---- tabla de cortar
BLOCKS.append(dict(
    id="cutting_board", es="Tabla de cortar", en="Cutting Board", sound="WOOD", render="cutout",
    mats={"wood": ("wood", (166, 112, 62)),
          "lime": ("flesh", (176, 228, 96), (58, 140, 40)),
          "limerind": ("plain", (62, 150, 44)),
          "lemon": ("flesh", (252, 232, 96), (226, 192, 36)),
          "lemonrind": ("plain", (238, 206, 52))},
    parts=[
        box([2, 0, 3], [14, 1, 13], "wood"),
        box([3.5, 1, 4.5], [7.2, 3, 8.2], "limerind", fm={"up": "lime"}),
        box([8.8, 1, 5], [12.5, 3, 8.7], "lemonrind", fm={"up": "lemon"}),
        box([4, 1, 9.2], [7.4, 1.6, 12.2], "limerind", fm={"up": "lime"}),
        box([9, 1, 9.6], [12.2, 1.6, 12.4], "lemonrind", fm={"up": "lemon"}),
    ],
    shape=[[2, 0, 3, 14, 3, 13]]))

# ---- macerador
BLOCKS.append(dict(
    id="muddler", es="Macerador", en="Muddler", sound="WOOD", render="cutout",
    mats={"wood": ("wood", (176, 124, 70)), "dark": ("wood", (96, 60, 36))},
    parts=[
        box([6.5, 0, 6.5], [9.5, 2, 9.5], "dark"),
        box([7.25, 2, 7.25], [8.75, 11, 8.75], "wood"),
        box([6.75, 11, 6.75], [9.25, 12.5, 9.25], "dark"),
    ]))

# ---- posavasos
BLOCKS.append(dict(
    id="coaster", es="Posavasos", en="Coaster", sound="WOOD", render="cutout",
    mats={"wood": ("wood", (150, 100, 58)),
          "top": ("label", (222, 192, 130), (150, 32, 32), (80, 45, 25))},
    parts=[
        box([4.5, 0, 4.5], [11.5, 0.6, 11.5], "wood", fm={"up": "top"}),
    ]))


# =====================  JUEGOS DE BAR  =====================
WOOD_D = ("wood", (104, 64, 36))
WOOD_L = ("wood", (160, 108, 60))
DARK = ("plain", (30, 30, 34))


def die_mats(prefix):
    return {prefix + str(n): ("die", (252, 250, 242), (200, 30, 30) if n == 1 else (28, 28, 30), n)
            for n in range(1, 7)}


# ---- diana de dardos (de pared, interactiva)
BLOCKS.append(dict(
    id="dartboard", es="Diana de dardos", en="Dartboard", sound="WOOD", render="cutout",
    wall=True, game="DARTS",
    mats={"wood": WOOD_D, "target": ("target",), "metal": ("metal", (210, 214, 222)),
          "red": ("plain", (205, 40, 40)), "blue": ("plain", (40, 90, 205))},
    parts=[
        box([0.5, 0.5, 15], [15.5, 15.5, 16], "wood"),
        box([1, 1, 14.2], [15, 15, 15], "wood", fm={"north": "target"}),
        box([9.5, 9.5, 12.2], [10.1, 10.1, 14.2], "metal", shape=False),
        box([9.2, 9.2, 11.8], [10.4, 10.4, 12.4], "red", shape=False),
        box([5.2, 6.2, 12.2], [5.8, 6.8, 14.2], "metal", shape=False),
        box([4.9, 5.9, 11.8], [6.1, 7.1, 12.4], "blue", shape=False),
        box([11.2, 4.6, 12.2], [11.8, 5.2, 14.2], "metal", shape=False),
        box([10.9, 4.3, 11.8], [12.1, 5.5, 12.4], "red", shape=False),
    ],
    shape=[[0.5, 0.5, 14, 15.5, 15.5, 16]]))

# ---- tablero de ajedrez (reglas completas, se juega por clic)
BLOCKS.append(dict(
    id="chess_board", es="Tablero de ajedrez", en="Chess Board", sound="WOOD", render="cutout",
    game="CHESS",
    mats={"wood": WOOD_D, "chess": ("chess",)},
    parts=[
        box([0.5, 0, 0.5], [15.5, 1, 15.5], "wood"),
        box([1.5, 1, 1.5], [14.5, 1.3, 14.5], "wood", fm={"up": "chess"}),
    ],
    shape=[[0.5, 0, 0.5, 15.5, 1.3, 15.5]]))

# ---- mesa de gallito (pulseada)
BLOCKS.append(dict(
    id="arm_wrestling_table", es="Mesa de gallito (pulseada)", en="Arm Wrestling Table", sound="WOOD",
    render="cutout", game="ARM",
    mats={"wood": WOOD_L, "dark": WOOD_D, "red": ("plain", (190, 40, 40)), "blue": ("plain", (40, 80, 190)),
          "metal": ("metal", (200, 205, 215))},
    parts=[
        box([0, 11, 2], [16, 13, 14], "wood"),
        box([1, 0, 3], [3, 11, 5], "dark"), box([13, 0, 3], [15, 11, 5], "dark"),
        box([1, 0, 11], [3, 11, 13], "dark"), box([13, 0, 11], [15, 11, 13], "dark"),
        box([2, 13, 5.5], [5.5, 13.6, 10.5], "red"),
        box([10.5, 13, 5.5], [14, 13.6, 10.5], "blue"),
        box([6.5, 13, 6.5], [9.5, 13.5, 9.5], "dark"),
        box([7.3, 13.5, 7.3], [8.7, 15.2, 8.7], "metal"),
    ],
    shape=[[0, 0, 2, 16, 15.2, 14]]))

# ---- mesa de billar
_balls = [(7.45, 4.5, "b1"), (6.8, 5.6, "b2"), (8.1, 5.6, "b8"), (6.15, 6.7, "b3"), (7.45, 6.7, "b4"),
          (8.75, 6.7, "b5"), (7.45, 10.8, "cue")]
BLOCKS.append(dict(
    id="pool_table", es="Mesa de billar", en="Pool Table", sound="WOOD", render="cutout", game="POOL",
    mats={"wood": WOOD_D, "felt": ("felt", (32, 120, 62)), "dark": DARK,
          "cue": ("plain", (245, 245, 240)), "b1": ("plain", (240, 200, 40)), "b2": ("plain", (40, 80, 200)),
          "b3": ("plain", (210, 40, 40)), "b4": ("plain", (120, 50, 160)), "b5": ("plain", (240, 130, 30)),
          "b8": ("plain", (20, 20, 22))},
    parts=[
        box([0.5, 0, 2], [2.5, 10, 4], "wood"), box([13.5, 0, 2], [15.5, 10, 4], "wood"),
        box([0.5, 0, 12], [2.5, 10, 14], "wood"), box([13.5, 0, 12], [15.5, 10, 14], "wood"),
        box([0, 10, 1], [16, 13, 15], "wood"),
        box([0, 13, 1], [16, 15, 2.2], "wood"), box([0, 13, 13.8], [16, 15, 15], "wood"),
        box([0, 13, 2.2], [1.2, 15, 13.8], "wood"), box([14.8, 13, 2.2], [16, 15, 13.8], "wood"),
        box([1.2, 13, 2.2], [14.8, 14, 13.8], "felt"),
        box([1.2, 14, 2.2], [2.4, 14.2, 3.4], "dark", shape=False), box([13.6, 14, 2.2], [14.8, 14.2, 3.4], "dark", shape=False),
        box([1.2, 14, 12.6], [2.4, 14.2, 13.8], "dark", shape=False), box([13.6, 14, 12.6], [14.8, 14.2, 13.8], "dark", shape=False),
        box([7.4, 14, 2.2], [8.6, 14.2, 3.4], "dark", shape=False), box([7.4, 14, 12.6], [8.6, 14.2, 13.8], "dark", shape=False),
    ] + [box([x, 14, z], [x + 1.1, 15.1, z + 1.1], m, shape=False) for (x, z, m) in _balls],
    shape=[[0, 0, 1, 16, 15, 15]]))

# ---- taca-taca (metegol)
_rods = [(5, "red"), (7, "blue"), (9, "red"), (11, "blue")]
_fparts = [
    box([1, 0, 4], [3, 5, 6], "dark"), box([13, 0, 4], [15, 5, 6], "dark"),
    box([1, 0, 10], [3, 5, 12], "dark"), box([13, 0, 10], [15, 5, 12], "dark"),
    box([1, 5, 3], [15, 10, 13], "wood"),
    box([2, 10, 4], [14, 10.4, 12], "felt"),
    box([1, 10, 3], [15, 11.5, 4], "wood"), box([1, 10, 12], [15, 11.5, 13], "wood"),
    box([1, 10, 4], [2, 11.5, 12], "wood"), box([14, 10, 4], [15, 11.5, 12], "wood"),
]
for (z, team) in _rods:
    _fparts.append(box([0, 11, z - 0.3], [16, 11.6, z + 0.3], "metal", shape=False))
    _fparts.append(box([0, 10.6, z - 0.5], [1.2, 12, z + 0.5], "dark", shape=False))
    _fparts.append(box([14.8, 10.6, z - 0.5], [16, 12, z + 0.5], "dark", shape=False))
    for x in (4.5, 8, 11.5):
        _fparts.append(box([x, 10.4, z - 0.4], [x + 0.8, 11.6, z + 0.4], team, shape=False))
_fparts.append(box([7.6, 10.4, 7.6], [8.4, 11.1, 8.4], "ball", shape=False))
BLOCKS.append(dict(
    id="foosball_table", es="Taca-taca (metegol)", en="Foosball Table", sound="WOOD", render="cutout",
    game="FOOSBALL",
    mats={"wood": WOOD_L, "dark": ("wood", (80, 50, 30)), "felt": ("felt", (40, 130, 66)),
          "metal": ("metal", (200, 205, 215)), "red": ("plain", (205, 40, 40)),
          "blue": ("plain", (40, 90, 205)), "ball": ("plain", (245, 245, 240))},
    parts=_fparts, shape=[[0, 0, 3, 16, 12, 13]]))

# ---- dados
_dm = die_mats("d")
BLOCKS.append(dict(
    id="dice", es="Dados", en="Dice", sound="WOOD", render="cutout", game="DICE",
    mats=_dm,
    parts=[
        box([4, 0, 5], [7.5, 3.5, 8.5], "d1",
            fm={"up": "d5", "north": "d3", "east": "d2", "south": "d4", "west": "d1", "down": "d6"}),
        box([8.2, 0, 7], [11.7, 3.5, 10.5], "d1",
            fm={"up": "d6", "north": "d1", "east": "d4", "south": "d2", "west": "d5", "down": "d3"},
            rot=([9.95, 0, 8.75], 22.5)),
    ],
    shape=[[4, 0, 5, 11.7, 3.5, 10.5]]))

# ---- mazo de cartas (21 / blackjack)
BLOCKS.append(dict(
    id="card_deck", es="Mazo de cartas (21)", en="Deck of Cards (Blackjack)", sound="WOOD", render="cutout",
    game="BLACKJACK",
    mats={"edge": ("cardedge",), "back": ("cardback", (200, 40, 52)),
          "heart": ("cardface", (210, 30, 40), 0), "spade": ("cardface", (30, 30, 34), 1)},
    parts=[
        box([8, 0, 3], [14, 2.4, 12], "edge", fm={"up": "back"}),
        box([2, 0, 3], [6.5, 0.15, 10], "edge", fm={"up": "heart"}, rot=([4.25, 0, 6.5], 22.5)),
        box([3, 0.15, 7], [7.5, 0.3, 14], "edge", fm={"up": "spade"}, rot=([5.25, 0, 10.5], -22.5)),
    ],
    shape=[[2, 0, 3, 14, 2.4, 14]]))

# ---- fichas de póker (decoración)
_chip = lambda c: {"chips_" + c[0]: ("chips", c[1], (240, 240, 235)), "top_" + c[0]: ("plain", c[1])}
_cm = {}
for _c in (("red", (200, 40, 40)), ("blue", (45, 90, 200)), ("black", (35, 35, 40)), ("green", (40, 150, 70))):
    _cm.update(_chip(_c))


def _stack(x, z, h, c):
    return box([x, 0, z], [x + 3, h, z + 3], "chips_" + c, fm={"up": "top_" + c})


BLOCKS.append(dict(
    id="poker_chips", es="Fichas de póker", en="Poker Chips", sound="WOOD", render="cutout",
    mats=_cm,
    parts=[_stack(3, 3, 3, "red"), _stack(8, 4, 2, "blue"), _stack(4.5, 9, 4, "black"), _stack(10, 9.5, 1.5, "green")],
    shape=[[3, 0, 3, 13, 4, 12.5]]))
