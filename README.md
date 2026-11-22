# Cocteleria (Minecraft Forge 1.20.1)

Mod de cócteles: solo las bebidas. **Sin recetas** y **sin generación en el mundo**; los tragos se obtienen
desde la pestaña creativa "Cocteleria" (o con `/give @s cocteleria:mojito`).

- 12 cócteles + vaso vacío (se devuelve al beber).
- Cada trago da efectos de vainilla (mareo/náuseas, lentitud, ceguera, oscuridad, etc.) más el efecto propio
  **Embriagado**, que te hace tropezar con empujones laterales aleatorios.
- Texturas 16x16 hechas por código (`tools/gen_textures.py`).

## Decoración
19 bloques decorativos colocables (se orientan hacia ti): botellas de whisky, ron, tequila, vodka, gin y vino,
coctelera, cubo de hielo, vaso corto, vaso alto, copa de cóctel, copa de vino, jigger, cuchara coctelera,
cuchillo de barman, colador, tabla de cortar (con limas y limones), macerador y posavasos.
Están en la pestaña creativa y sin recetas. Se definen en `tools/decor_spec.py` y se regeneran con
`python3 tools/gen_decor.py .` (texturas, modelos, blockstates, loot y `ModBlocks.java`).

## Juegos de bar (multijugador, se juegan con clic derecho)
Todas las mesas guardan su partida; los mensajes salen en el chat para quienes estén a menos de 24 bloques.
**Shift + clic derecho con las manos vacías** reinicia la mesa (en el 21 sirve para plantarse).

| Bloque | Cómo se juega |
|---|---|
| **Diana de dardos** (se pone en una pared) | Dardos 301 por turnos, hasta 8 jugadores, 3 dardos por turno. El dardo cae donde haces clic (dobles, triples, bull y diana de 50). Hay que llegar a 0 exacto; si te pasas, vuelves a tu puntaje inicial del turno. |
| **Tablero de ajedrez** | 2 jugadores, reglas completas (enroque, al paso, coronación, jaque, mate, tablas). El 1.º en hacer clic juega con blancas (lado frontal) y el 2.º con negras. Clic en tu pieza y luego en la casilla destino; el tablero aparece en el chat. |
| **Mesa de gallito** | 2 jugadores. Cuenta atrás 3-2-1 y ¡YA!: gana quien haga más clics rápido. Ganador: Fuerza; perdedor: Debilidad. |
| **Taca-taca** | Rojo vs azul (hasta 2 por equipo). Cada clic empuja la pelota hacia el arco rival; gana el primero a 5 goles. |
| **Mesa de billar** | 2 jugadores por turnos (versión simplificada, sin física): 7 bolas y luego la 8. |
| **Dados** | Duelo: el primero que tira abre una ronda de 10 s; gana la suma más alta de 2 dados. |
| **Mazo de cartas (21)** | Blackjack contra el crupier, varios jugadores: clic = unirse/pedir carta, Shift+clic = plantarse. |
| **Fichas de póker** | Solo decoración. |

El motor de ajedrez está probado con tests de perft estándar. Las partidas viven en memoria (se reinician al recargar el mundo).

## Obtener el .jar (1 paso)
Instala JDK 17 y haz doble clic en `compilar.bat` (Windows) o ejecuta `./compilar.sh` (Linux/Mac).
La primera vez descarga Forge y tarda unos minutos. El mod queda en `build/libs/cocteleria-1.0.0.jar`;
copialo a la carpeta `mods` de Minecraft 1.20.1 con Forge 47.x.

## Compilar (otras opciones)
Requiere JDK 17. Abre la carpeta en IntelliJ IDEA / Eclipse (importa como proyecto Gradle) o copia
`gradlew`, `gradlew.bat` y la carpeta `gradle/` del MDK oficial de Forge 1.20.1 (47.3.0) a esta carpeta y ejecuta:

    ./gradlew build        # el .jar queda en build/libs/
    ./gradlew runClient    # probar en el juego

IDs: mojito, margarita, pina_colada, blue_lagoon, tequila_sunrise, cosmopolitan, whisky_sour,
black_russian, green_fairy, mai_tai, sangria, long_island, empty_glass.


## Juegos de bar (v2): interfaz, mesas grandes y dinámicas

- **Interfaz propia** para cada juego (clic derecho en la mesa): ajedrez, billar, taca-taca, gallito, dados, veintiuno y marcador de dardos.
- **Mesas realistas** de varios bloques: billar (3x2), taca-taca (2x1) y mesa de gallito (2x1).
- **Controles**: clic del mouse (ajedrez, mesas), **ESPACIO** (taca-taca, gallito, dados), billar: apuntas con el mouse, mantienes ESPACIO para cargar la fuerza y sueltas para tirar.
- **Dardos**: el item *Dardo* se lleva en la mano, máximo **6**. Mantén clic derecho para cargar y suelta para lanzar; hay **ángulo de error** (menor con más fuerza, mayor si corres) y **caída** por gravedad. Recógelos desde la pantalla de la diana.

## Comida chilena

- **En plato** (se colocan como bloque y se comen por mordiscos con clic derecho, como la torta; al terminar queda el plato vacío): Hamburguesa, Papas fritas, Completo, Salchipapas. También hay un **Plato** decorativo.
- **En la mano** (se comen como cualquier comida): Empanada, Sopaipilla, Sopaipilla con pebre.
- Todo está en la pestaña creativa *Cocteleria* (sin recetas).

## Blackjack con crupier (staff)

Un jugador toma el rol de **crupier** (staff) y controla la mesa: reparte la ronda, cierra las jugadas, pide carta o se planta y cierra la mesa. De **1 a 3 jugadores** se unen y piden carta o se plantan. La carta oculta del crupier se muestra cuando juega.
