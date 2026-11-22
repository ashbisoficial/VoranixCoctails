import json, os, sys
R = sys.argv[1]
A = os.path.join(R, "assets", "cocteleria")
os.makedirs(os.path.join(A, "models", "item"), exist_ok=True)
os.makedirs(os.path.join(A, "lang"), exist_ok=True)

items = ["mojito","margarita","pina_colada","blue_lagoon","tequila_sunrise","cosmopolitan",
         "whisky_sour","black_russian","green_fairy","mai_tai","sangria","long_island","empty_glass"]
for i in items:
    with open(os.path.join(A, "models", "item", i + ".json"), "w") as f:
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"cocteleria:item/{i}"}}, f, indent=2)

es = {
 "itemGroup.cocteleria": "Cocteleria",
 "effect.cocteleria.tipsy": "Embriagado",
 "item.cocteleria.empty_glass": "Vaso vacío",
 "item.cocteleria.mojito": "Mojito",
 "item.cocteleria.mojito.desc": "Fresco y burbujeante. Velocidad... y un poco de mareo.",
 "item.cocteleria.margarita": "Margarita",
 "item.cocteleria.margarita.desc": "Borde de sal y saltos de gigante. El suelo se mueve.",
 "item.cocteleria.pina_colada": "Piña Colada",
 "item.cocteleria.pina_colada.desc": "Dulce y cremosa. Te cura, pero te vuelve lento y mareado.",
 "item.cocteleria.blue_lagoon": "Blue Lagoon",
 "item.cocteleria.blue_lagoon.desc": "Respira bajo el agua... si no se te nubla la vista.",
 "item.cocteleria.tequila_sunrise": "Tequila Sunrise",
 "item.cocteleria.tequila_sunrise.desc": "Fuego por dentro, inmune al de afuera. Con mareo incluido.",
 "item.cocteleria.cosmopolitan": "Cosmopolitan",
 "item.cocteleria.cosmopolitan.desc": "Elegante y con suerte. El mareo viene de regalo.",
 "item.cocteleria.whisky_sour": "Whisky Sour",
 "item.cocteleria.whisky_sour.desc": "Piel dura, piernas pesadas y la cabeza dando vueltas.",
 "item.cocteleria.black_russian": "Ruso Negro",
 "item.cocteleria.black_russian.desc": "Fuerza bruta, pero el mundo se oscurece y se tambalea.",
 "item.cocteleria.green_fairy": "Hada Verde",
 "item.cocteleria.green_fairy.desc": "Ves en la oscuridad, brillas y flotas un instante. Mareo fuerte.",
 "item.cocteleria.mai_tai": "Mai Tai",
 "item.cocteleria.mai_tai.desc": "Cae como una pluma, aunque camines como un pato.",
 "item.cocteleria.sangria": "Sangría",
 "item.cocteleria.sangria.desc": "Fruta, vino y una buena dosis de absorción. Y mareo.",
 "item.cocteleria.long_island": "Long Island",
 "item.cocteleria.long_island.desc": "Muy rápido, muy ciego, muy mareado. Con prudencia.",
}
en = {
 "itemGroup.cocteleria": "Cocteleria",
 "effect.cocteleria.tipsy": "Tipsy",
 "item.cocteleria.empty_glass": "Empty Glass",
 "item.cocteleria.mojito": "Mojito",
 "item.cocteleria.mojito.desc": "Fresh and fizzy. Speed... and a bit of dizziness.",
 "item.cocteleria.margarita": "Margarita",
 "item.cocteleria.margarita.desc": "Salted rim and giant leaps. The ground is moving.",
 "item.cocteleria.pina_colada": "Pina Colada",
 "item.cocteleria.pina_colada.desc": "Sweet and creamy. Heals you, but slows and dizzies you.",
 "item.cocteleria.blue_lagoon": "Blue Lagoon",
 "item.cocteleria.blue_lagoon.desc": "Breathe underwater... if your vision holds up.",
 "item.cocteleria.tequila_sunrise": "Tequila Sunrise",
 "item.cocteleria.tequila_sunrise.desc": "Fire inside, immune to fire outside. Dizziness included.",
 "item.cocteleria.cosmopolitan": "Cosmopolitan",
 "item.cocteleria.cosmopolitan.desc": "Elegant and lucky. Dizziness comes free.",
 "item.cocteleria.whisky_sour": "Whisky Sour",
 "item.cocteleria.whisky_sour.desc": "Tough skin, heavy legs and a spinning head.",
 "item.cocteleria.black_russian": "Black Russian",
 "item.cocteleria.black_russian.desc": "Brute strength, but the world darkens and sways.",
 "item.cocteleria.green_fairy": "Green Fairy",
 "item.cocteleria.green_fairy.desc": "See in the dark, glow and float briefly. Heavy dizziness.",
 "item.cocteleria.mai_tai": "Mai Tai",
 "item.cocteleria.mai_tai.desc": "Land like a feather, even if you walk like a duck.",
 "item.cocteleria.sangria": "Sangria",
 "item.cocteleria.sangria.desc": "Fruit, wine and a good dose of absorption. And dizziness.",
 "item.cocteleria.long_island": "Long Island",
 "item.cocteleria.long_island.desc": "Very fast, very blind, very dizzy. Be careful.",
}
for name, d in (("es_es", es), ("es_cl", es), ("en_us", en)):
    with open(os.path.join(A, "lang", name + ".json"), "w", encoding="utf-8") as f:
        json.dump(d, f, indent=2, ensure_ascii=False)
print("ok")
