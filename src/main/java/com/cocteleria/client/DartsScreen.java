package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Marcador de dardos: muestra la diana con los dardos clavados, los puntos y los turnos. Los dardos se lanzan en el mundo
 * (clic derecho mantenido con el dardo en la mano).
 */
public class DartsScreen extends GameScreen {
    private static final ResourceLocation BOARD = new ResourceLocation("cocteleria", "textures/gui/dartboard.png");
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
        // diana (textura precalculada: no se dibuja pixel a pixel, así no se laguea)
        circle(g, cx, cy, R + 4, 0xFF111111);
        g.blit(BOARD, cx - R, cy - R, 2 * R, 2 * R, 0, 0, 256, 256, 256, 256);
        // dardos clavados
        int[] hits = data.getIntArray("hits");
        for (int i = 0; i + 2 < hits.length; i += 3) {
            float hx = cx + (hits[i] / 500f - 1) * R;
            float hy = cy + (hits[i + 1] / 500f - 1) * R;
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
