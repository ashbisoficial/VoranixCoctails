package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Duelo de dados: ESPACIO o el botón para tirar tus dos dados. */
public class DiceScreen extends GameScreen {
    private static final int[][] PIPS = {
            {}, {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};

    public DiceScreen(BlockPos pos) {
        super("Dados", pos, BarGame.DICE);
    }

    @Override
    protected void build() {
        addRenderableWidget(Button.builder(Component.literal("Tirar dados (ESPACIO)"), b -> send(1, 0, 0, 0))
                .bounds(width / 2 - 80, height - 30, 160, 20).build());
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE) {
            send(1, 0, 0, 0);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private void die(GuiGraphics g, int x, int y, int n) {
        g.fill(x - 1, y - 1, x + 33, y + 33, 0xFF000000);
        g.fill(x, y, x + 32, y + 32, 0xFFF5F5F0);
        if (n < 1 || n > 6) {
            return;
        }
        for (int cell : PIPS[n]) {
            int cx = x + 6 + (cell % 3) * 10, cy = y + 6 + (cell / 3) * 10;
            g.fill(cx, cy, cx + 5, cy + 5, 0xFF111111);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        center(g, data.getBoolean("open") ? "Ronda abierta: " + data.getInt("left") + " s" : "Sin ronda abierta", width / 2, 18, 0xFFFFD27F);
        ListTag rolls = data.getList("rolls", 10);
        int y = 40;
        for (int i = 0; i < rolls.size(); i++) {
            CompoundTag r = rolls.getCompound(i);
            int x = width / 2 - 110;
            panel(g, x - 8, y - 4, 230, 40);
            die(g, x, y, r.getInt("d1"));
            die(g, x + 38, y, r.getInt("d2"));
            String line = r.getString("name") + " = " + (r.getInt("d1") + r.getInt("d2"));
            text(g, line, x + 84, y + 12, isMe(r.getString("id")) ? 0xFF7CFC9A : 0xFFFFFFFF);
            y += 46;
            if (y > height - 90) {
                break;
            }
        }
        wrapped(g, data.getString("msg"), width / 2 - 110, height - 70, 230, 0xFFE0E0E0);
        super.render(g, mouseX, mouseY, pt);
    }
}
