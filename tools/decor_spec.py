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

# Celdas de las mesas grandes: (dx, dz) respecto al bloque origen (modelo mirando al norte).
CELLS_2X1 = [(0, 0), (1, 0)]
CELLS_3X2 = [(0, 0), (-1, 0), (1, 0), (0, 1), (-1, 1), (1, 1)]

# ---- mesa de gallito (pulseada) 2x1
BLOCKS.append(dict(
    id="arm_wrestling_table", es="Mesa de gallito (pulseada)", en="Arm Wrestling Table", sound="WOOD",
    render="cutout", game="ARM", cells=CELLS_2X1,
    mats={"wood": WOOD_L, "dark": WOOD_D, "red": ("plain", (190, 40, 40)), "blue": ("plain", (40, 80, 190)),
          "metal": ("metal", (200, 205, 215))},
    parts=[
        box([3, 11, 2], [29, 13, 14], "wood"),
        box([4, 0, 3], [6.5, 11, 5.5], "dark"), box([25.5, 0, 3], [28, 11, 5.5], "dark"),
        box([4, 0, 10.5], [6.5, 11, 13], "dark"), box([25.5, 0, 10.5], [28, 11, 13], "dark"),
        box([6, 13, 4.5], [13, 13.6, 11.5], "red"),
        box([19, 13, 4.5], [26, 13.6, 11.5], "blue"),
        box([13.5, 13, 5.5], [18.5, 14, 10.5], "dark"),
        box([14.3, 14, 7.3], [15.5, 17, 8.7], "metal"),
        box([16.5, 14, 7.3], [17.7, 17, 8.7], "metal"),
    ]))

# ---- mesa de billar 3x2
_cx = 8  # centro X del bloque origen
_balls = [(1.0, 15.0, "cue"), (17.0, 15.0, "b1"), (18.9, 14.0, "b2"), (18.9, 16.0, "b8"),
          (20.8, 13.0, "b3"), (20.8, 15.0, "b4"), (20.8, 17.0, "b5")]
_pool = [
    # patas
    box([-12.5, 0, 5], [-9.5, 11, 8], "wood"), box([25.5, 0, 5], [28.5, 11, 8], "wood"),
    box([-12.5, 0, 24], [-9.5, 11, 27], "wood"), box([25.5, 0, 24], [28.5, 11, 27], "wood"),
    # faldón
    box([-14, 10, 3], [30, 13, 29], "wood"),
    # bordes
    box([-14, 13, 3], [30, 15, 7], "wood"), box([-14, 13, 25], [30, 15, 29], "wood"),
    box([-14, 13, 7], [-10, 15, 25], "wood"), box([26, 13, 7], [30, 15, 25], "wood"),
    # paño 36x18
    box([-10, 13, 7], [26, 14, 25], "felt"),
    # troneras
    box([-10, 14, 7], [-7, 14.2, 10], "dark", shape=False), box([23, 14, 7], [26, 14.2, 10], "dark", shape=False),
    box([-10, 14, 22], [-7, 14.2, 25], "dark", shape=False), box([23, 14, 22], [26, 14.2, 25], "dark", shape=False),
    box([6.5, 14, 7], [9.5, 14.2, 9.5], "dark", shape=False), box([6.5, 14, 22.5], [9.5, 14.2, 25], "dark", shape=False),
] + [box([x, 14, z], [x + 1.8, 15.8, z + 1.8], m, shape=False) for (x, z, m) in
     [(bx - 1, bz + 2 - 0.0, m) for (bx, bz, m) in _balls]]
for i in range(7):  # diamantes de los bordes largos
    _pool.append(box([-8 + i * 5.3, 15, 4.6], [-7.2 + i * 5.3, 15.2, 5.4], "dia", shape=False))
    _pool.append(box([-8 + i * 5.3, 15, 26.6], [-7.2 + i * 5.3, 15.2, 27.4], "dia", shape=False))
BLOCKS.append(dict(
    id="pool_table", es="Mesa de billar", en="Pool Table", sound="WOOD", render="cutout", game="POOL",
    cells=CELLS_3X2,
    mats={"wood": WOOD_D, "felt": ("felt", (32, 120, 62)), "dark": DARK, "dia": ("metal", (230, 220, 180)),
          "cue": ("plain", (245, 245, 240)), "b1": ("plain", (240, 200, 40)), "b2": ("plain", (40, 80, 200)),
          "b3": ("plain", (210, 40, 40)), "b4": ("plain", (120, 50, 160)), "b5": ("plain", (240, 130, 30)),
          "b8": ("plain", (20, 20, 22))},
    parts=_pool))

# ---- taca-taca 2x1
_f = [
    box([3, 0, 3], [5, 9, 5], "dark"), box([27, 0, 3], [29, 9, 5], "dark"),
    box([3, 0, 11], [5, 9, 13], "dark"), box([27, 0, 11], [29, 9, 13], "dark"),
    box([2, 9, 2], [30, 13, 14], "wood"),
    box([4, 13, 4], [28, 13.3, 12], "felt"),
    box([2, 13, 2], [30, 15, 4], "wood"), box([2, 13, 12], [30, 15, 14], "wood"),
    box([2, 13, 4], [4, 15, 12], "wood"), box([28, 13, 4], [30, 15, 12], "wood"),
]
_rod_team = ["red", "red", "blue", "red", "blue", "red", "blue", "blue"]
_rod_n = [1, 2, 3, 5, 5, 3, 2, 1]
for i in range(8):
    rx = 5.5 + i * 3.0
    team = _rod_team[i]
    _f.append(box([rx - 0.3, 14.2, -1.5], [rx + 0.3, 14.8, 15.5], "metal", shape=False))
    _f.append(box([rx - 0.6, 13.8, -2.5], [rx + 0.6, 15.2, -0.5], "dark", shape=False))
    _f.append(box([rx - 0.6, 13.8, 14.5], [rx + 0.6, 15.2, 16.5], "dark", shape=False))
    n = _rod_n[i]
    gap = {1: 0, 2: 3.2, 3: 2.8, 5: 1.8}[n]
    for j in range(n):
        z = 8 + (j - (n - 1) / 2) * gap
        _f.append(box([rx - 0.5, 13.4, z - 0.45], [rx + 0.5, 14.9, z + 0.45], team, shape=False))
_f.append(box([15.5, 13.3, 7.6], [16.5, 14.1, 8.4], "ball", shape=False))
BLOCKS.append(dict(
    id="foosball_table", es="Taca-taca (metegol)", en="Foosball Table", sound="WOOD", render="cutout",
    game="FOOSBALL", cells=CELLS_2X1,
    mats={"wood": WOOD_L, "dark": ("wood", (80, 50, 30)), "felt": ("felt", (40, 130, 66)),
          "metal": ("metal", (200, 205, 215)), "red": ("plain", (205, 40, 40)),
          "blue": ("plain", (40, 90, 205)), "ball": ("plain", (245, 245, 240))},
    parts=_f))

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
