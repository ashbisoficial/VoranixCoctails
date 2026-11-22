package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

/**
 * Marcador de dardos: muestra la diana con los dardos clavados, los puntos y los turnos. Los dardos se lanzan en el mundo
 * (clic derecho mantenido con el dardo en la mano).
 */
public class DartsScreen extends GameScreen {
    private static final int[] ORDER = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};
    private static final int[] PCOL = {0xFFFF4136, 0xFF2ECC40, 0xFF0074D9, 0xFFFFDC00, 0xFFF012BE, 0xFF7FDBFF, 0xFFFF851B, 0xFFFFFFFF};

    public DartsScreen(BlockPos pos) {
        super("Dardos 301", pos, BarGame.DARTS);
    }

    @Override
    protected void build() {
        int y = height - 28;
        addRenderableWidget(Button.builder(Component.literal("Unirme / Recargar 6 dardos"), b -> send(1, 0, 0, 0))
                .bounds(width / 2 - 215, y, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Recoger mis dardos"), b -> send(2, 0, 0, 0))
                .bounds(width / 2 - 70, y, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reiniciar"), b -> send(9, 0, 0, 0))
                .bounds(width / 2 + 75, y, 140, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        int R = Math.min((height - 100) / 2, 90);
        int cx = 30 + R, cy = 40 + R;
        // diana
        circle(g, cx, cy, R + 4, 0xFF111111);
        for (int i = 0; i < 20; i++) {
            double a0 = Math.toRadians(i * 18 - 9 - 90), a1 = Math.toRadians(i * 18 + 9 - 90);
            boolean dark = i % 2 == 0;
            for (int ring = 0; ring < 4; ring++) {
                double r0 = new double[]{0.225, 0.45, 0.55, 0.775}[ring] * R;
                double r1 = new double[]{0.45, 0.55, 0.775, 0.9}[ring] * R;
                int col = (ring % 2 == 1) ? (dark ? 0xFFC0392B : 0xFF1E8449) : (dark ? 0xFF1A1A1A : 0xFFE8D8A8);
                for (double r = r0; r < r1; r += 0.7) {
                    for (double a = a0; a < a1; a += 1.0 / Math.max(r, 1)) {
                        int px = (int) (cx + Math.cos(a) * r), py = (int) (cy + Math.sin(a) * r);
                        g.fill(px, py, px + 1, py + 1, col);
                    }
                }
            }
            double am = Math.toRadians(i * 18 - 90);
            center(g, String.valueOf(ORDER[i]), (int) (cx + Math.cos(am) * (R * 0.96)), (int) (cy + Math.sin(am) * (R * 0.96)) - 4, 0xFFFFFFFF);
        }
        circle(g, cx, cy, R * 0.225f, 0xFF1E8449);
        circle(g, cx, cy, R * 0.1125f, 0xFFC0392B);
        // dardos clavados
        int[] hits = data.getIntArray("hits");
        for (int i = 0; i + 2 < hits.length; i += 3) {
            float hx = cx + (hits[i] / 500f - 1) * R * 0.4375f / 0.4375f * 0.9f / 0.9f;
            float hy = cy + (hits[i + 1] / 500f - 1) * R;
            hx = cx + (hits[i] / 500f - 1) * R;
            circle(g, hx, hy, 3, 0xFF000000);
            circle(g, hx, hy, 2, PCOL[Math.floorMod(hits[i + 2], PCOL.length)]);
        }
        // jugadores
        ListTag players = data.getList("players", 10);
        int px = cx + R + 40, y = 36;
        text(g, "Jugadores (301 → 0 exacto)", px, y - 14, 0xFFFFD27F);
        int turn = data.getInt("turn");
        for (int i = 0; i < players.size(); i++) {
            CompoundTag p = players.getCompound(i);
            boolean mine = isMe(p.getString("id"));
            panel(g, px, y, Math.max(160, width - px - 20), 22);
            circle(g, px + 8, y + 11, 4, PCOL[i % PCOL.length]);
            String line = (i == turn ? "► " : "") + p.getString("name") + " — " + p.getInt("left")
                    + "   (en diana: " + p.getInt("out") + ")";
            text(g, line, px + 18, y + 7, mine ? 0xFF7CFC9A : 0xFFFFFFFF);
            y += 26;
            if (y > height - 90) {
                break;
            }
        }
        if (players.size() > 0) {
            text(g, "Dardos lanzados este turno: " + data.getInt("thrown") + "/3", px, y + 4, 0xFFAAAAAA);
        }
        wrapped(g, data.getString("msg"), px, height - 80, width - px - 20, 0xFFE0E0E0);
        text(g, "Sostén un dardo y mantén clic derecho; suelta para lanzar.", 30, height - 44, 0xFF9AE6B4);
        super.render(g, mouseX, mouseY, pt);
    }
}
