package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Taca-taca: aprieta ESPACIO (o haz clic) lo más rápido posible para empujar la pelota al arco rival. */
public class FoosballScreen extends GameScreen {
    private float shownBall = 5;
    private int kick;

    public FoosballScreen(BlockPos pos) {
        super("Taca-taca", pos, BarGame.FOOSBALL);
    }

    @Override
    protected void build() {
        addRenderableWidget(Button.builder(Component.literal("Unirme / Patear"), b -> send(1, 0, 0, 0))
                .bounds(width / 2 - 105, height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reiniciar"), b -> send(9, 0, 0, 0))
                .bounds(width / 2 + 5, height - 30, 100, 20).build());
    }

    private void kickNow() {
        send(1, 0, 0, 0);
        kick = 6;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE) {
            kickNow();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        kickNow();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        int len = Math.max(1, data.getInt("length"));
        float target = data.contains("ball") ? data.getInt("ball") : len / 2f;
        shownBall += (target - shownBall) * 0.25f;
        int fw = Math.min(width - 60, 420), fh = 120;
        int fx = (width - fw) / 2, fy = 55;
        g.fill(fx - 8, fy - 8, fx + fw + 8, fy + fh + 8, 0xFF3B2A1A);
        g.fill(fx, fy, fx + fw, fy + fh, 0xFF2E8B3D);
        g.fill(fx + fw / 2 - 1, fy, fx + fw / 2 + 1, fy + fh, 0x66FFFFFF);
        circle(g, fx + fw / 2f, fy + fh / 2f, 18, 0x33FFFFFF);
        circle(g, fx + fw / 2f, fy + fh / 2f, 16, 0xFF2E8B3D);
        g.fill(fx, fy + fh / 2 - 22, fx + 6, fy + fh / 2 + 22, 0xFFFF4136);   // arco rojo (izquierda)
        g.fill(fx + fw - 6, fy + fh / 2 - 22, fx + fw, fy + fh / 2 + 22, 0xFF0074D9); // arco azul (derecha)
        // barras y jugadores
        for (int i = 0; i < 8; i++) {
            int rx = fx + 22 + i * (fw - 44) / 7;
            g.fill(rx - 1, fy - 4, rx + 2, fy + fh + 4, 0xFFBBBBBB);
            boolean redRow = i % 2 == 0;
            for (int j = 0; j < 3; j++) {
                int py = fy + 20 + j * 40 + (kick > 0 && redRow == (i < 4) ? 4 : 0);
                circle(g, rx, py, 6, redRow ? 0xFFFF4136 : 0xFF0074D9);
            }
        }
        float bx = fx + 6 + (fw - 12) * shownBall / len;
        circle(g, bx + 1, fy + fh / 2f + 1, 6, 0x66000000);
        circle(g, bx, fy + fh / 2f, 6, 0xFFFFFFFF);
        if (kick > 0) {
            kick--;
        }
        // marcador
        bigCenter(g, data.getInt("redGoals") + "  -  " + data.getInt("blueGoals"), width / 2, 14, 2.5f, 0xFFFFFFFF);
        String red = data.getString("red").isEmpty() ? "(libre)" : data.getString("red");
        String blue = data.getString("blue").isEmpty() ? "(libre)" : data.getString("blue");
        text(g, "ROJO: " + red, fx, fy + fh + 16, 0xFFFF7F7F);
        text(g, "AZUL: " + blue, fx + fw - font.width("AZUL: " + blue), fy + fh + 16, 0xFF7FB8FF);
        String side = isMeIn(data.getString("redIds")) ? "Juegas con ROJO ►" : isMeIn(data.getString("blueIds")) ? "◄ Juegas con AZUL" : "Espectador";
        center(g, side, width / 2, fy + fh + 30, 0xFF9AE6B4);
        wrapped(g, data.getString("msg"), fx, fy + fh + 46, fw, 0xFFE0E0E0);
        center(g, "¡Aprieta ESPACIO o haz clic rápido!", width / 2, height - 44, 0xFFFFD27F);
        super.render(g, mouseX, mouseY, pt);
    }
}
