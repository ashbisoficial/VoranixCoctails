package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Billar: apunta con el mouse, mantén ESPACIO para cargar la fuerza y suelta para tirar. Con la blanca en mano, clic para colocarla. */
public class PoolScreen extends GameScreen {
    private static final int[] COLORS = {
            0xFFF5F5F0, 0xFFF2C500, 0xFF1D4ED8, 0xFFD62828, 0xFF6B21A8, 0xFFF97316, 0xFF15803D, 0xFF7F1D1D, 0xFF111111,
            0xFFF2C500, 0xFF1D4ED8, 0xFFD62828, 0xFF6B21A8, 0xFFF97316, 0xFF15803D, 0xFF7F1D1D};
    private static final float W = 200, H = 100;

    private float scale;
    private int tx, ty;
    private final float[] sx = new float[16], sy = new float[16];
    private boolean seeded;
    private boolean charging;
    private long chargeStart;

    public PoolScreen(BlockPos pos) {
        super("Billar", pos, BarGame.POOL);
    }

    @Override
    protected void build() {
        scale = Math.min((width - 60) / W, (height - 110) / H);
        tx = (int) ((width - W * scale) / 2);
        ty = 38;
        addRenderableWidget(Button.builder(Component.literal("Reiniciar"), b -> send(9, 0, 0, 0))
                .bounds(width - 90, height - 28, 80, 20).build());
    }

    private int seat() {
        return isMe(data.getString("id0")) ? 0 : isMe(data.getString("id1")) ? 1 : -1;
    }

    private boolean myTurn() {
        return seat() >= 0 && seat() == data.getInt("turn");
    }

    private float cueX() {
        int[] p = data.getIntArray("pos");
        return p.length >= 2 ? p[0] / 10f : 50;
    }

    private float cueY() {
        int[] p = data.getIntArray("pos");
        return p.length >= 2 ? p[1] / 10f : 50;
    }

    private float aimAngle(double mx, double my) {
        float dx = (float) ((mx - tx) / scale) - cueX();
        float dy = (float) ((my - ty) / scale) - cueY();
        return (float) Math.atan2(dy, dx);
    }

    private float power() {
        if (!charging) {
            return 0;
        }
        return Math.min(1f, (System.currentTimeMillis() - chargeStart) / 1000f); // sube en 1 s y se queda al máximo
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE && myTurn() && data.getInt("phase") == 1) {
            if (!charging) {
                charging = true;
                chargeStart = System.currentTimeMillis();
            }
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean keyReleased(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_SPACE && charging) {
            charging = false;
            var mc = net.minecraft.client.Minecraft.getInstance();
            double mx = mc.mouseHandler.xpos() * width / mc.getWindow().getScreenWidth();
            double my = mc.mouseHandler.ypos() * height / mc.getWindow().getScreenHeight();
            if (myTurn() && data.getInt("phase") == 1) {
                send(1, Math.round(power() * 1000), 0, aimAngle(mx, my));
            }
            return true;
        }
        return super.keyReleased(key, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        if (myTurn() && data.getInt("phase") == 3) {
            float px = (float) ((mx - tx) / scale), py = (float) ((my - ty) / scale);
            if (px > 3 && px < W - 3 && py > 3 && py < H - 3) {
                send(2, Math.round(px * 10), Math.round(py * 10), 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        int phase = data.getInt("phase");
        int w = (int) (W * scale), h = (int) (H * scale);
        // marco, paño y troneras
        g.fill(tx - 10, ty - 10, tx + w + 10, ty + h + 10, 0xFF4A2C14);
        g.fill(tx, ty, tx + w, ty + h, 0xFF1F7A3A);
        float[][] pockets = {{0, 0}, {W / 2, 0}, {W, 0}, {0, H}, {W / 2, H}, {W, H}};
        for (float[] pk : pockets) {
            circle(g, tx + pk[0] * scale, ty + pk[1] * scale, 5.5f * scale * 0.9f, 0xFF050505);
        }
        int[] p = data.getIntArray("pos");
        int mask = data.getInt("mask");
        if (p.length == 32) {
            for (int i = 0; i < 16; i++) { // suaviza el movimiento entre actualizaciones del servidor (20/s)
                float tx2 = p[i * 2] / 10f, ty2 = p[i * 2 + 1] / 10f;
                if (!seeded || Math.abs(tx2 - sx[i]) > 40 || Math.abs(ty2 - sy[i]) > 40) {
                    sx[i] = tx2;
                    sy[i] = ty2;
                } else {
                    sx[i] += (tx2 - sx[i]) * 0.5f;
                    sy[i] += (ty2 - sy[i]) * 0.5f;
                }
            }
            seeded = true;
            // línea de puntería
            if (myTurn() && phase == 1) {
                float ang = aimAngle(mouseX, mouseY);
                float cx = tx + sx[0] * scale, cy = ty + sy[0] * scale;
                for (int i = 1; i < 28; i++) {
                    float d = i * 5 * scale * 0.5f + 6;
                    g.fill((int) (cx + Math.cos(ang) * d), (int) (cy + Math.sin(ang) * d),
                            (int) (cx + Math.cos(ang) * d) + 2, (int) (cy + Math.sin(ang) * d) + 2, 0xBBFFFFFF);
                }
                // taco (en el lado contrario), retrocede según la fuerza
                float back = 8 + power() * 30;
                for (int i = 0; i < 40; i++) {
                    float d = back + i * 2.2f;
                    int x = (int) (cx - Math.cos(ang) * d), y = (int) (cy - Math.sin(ang) * d);
                    g.fill(x, y, x + 3, y + 3, i < 6 ? 0xFFE9D8A6 : 0xFF8B5A2B);
                }
            }
            for (int i = 15; i >= 0; i--) {
                if ((mask & (1 << i)) != 0) {
                    continue;
                }
                float bx = tx + sx[i] * scale, by = ty + sy[i] * scale;
                float r = 2.25f * scale;
                circle(g, bx + 1, by + 1, r, 0x66000000);
                circle(g, bx, by, r, i >= 9 ? 0xFFF5F5F0 : COLORS[i]);
                if (i >= 9) {
                    g.fill((int) (bx - r + 1), (int) (by - r * 0.4f), (int) (bx + r - 1), (int) (by + r * 0.4f), COLORS[i]);
                }
                if (i > 0 && r > 5) {
                    circle(g, bx, by, r * 0.5f, 0xFFF5F5F0);
                    center(g, String.valueOf(i), (int) bx, (int) by - 3, 0xFF000000);
                }
            }
        }
        // barra de fuerza
        if (myTurn() && phase == 1) {
            int bx = tx - 6 - 14, bh = h;
            g.fill(bx - 1, ty - 1, bx + 11, ty + bh + 1, 0xFF000000);
            int fill = (int) (bh * power());
            int col = power() < 0.5f ? 0xFF2ECC40 : power() < 0.8f ? 0xFFFFDC00 : 0xFFFF4136;
            g.fill(bx, ty + bh - fill, bx + 10, ty + bh, col);
            text(g, "FUERZA", bx - 10, ty + bh + 8, 0xFFFFFFFF);
        }
        // cabecera
        String n0 = data.getString("n0").isEmpty() ? "(libre)" : data.getString("n0");
        String n1 = data.getString("n1").isEmpty() ? "(libre)" : data.getString("n1");
        int turn = data.getInt("turn");
        String g0 = grp(data.getInt("g0")), g1 = grp(data.getInt("g1"));
        text(g, (turn == 0 && phase != 0 ? "► " : "  ") + n0 + g0, 14, 8, 0xFFFFFFFF);
        text(g, (turn == 1 && phase != 0 ? "► " : "  ") + n1 + g1, width / 2 + 10, 8, 0xFFFFD27F);
        wrapped(g, data.getString("msg"), 14, ty + h + 18, width - 120, 0xFFE0E0E0);
        String help = myTurn() ? (phase == 3 ? "Haz clic en la mesa para colocar la blanca"
                : phase == 1 ? "Apunta con el mouse · mantén ESPACIO y suelta para tirar" : "")
                : seat() >= 0 ? "Espera tu turno" : "Espectador";
        text(g, help, 14, 22, 0xFF9AE6B4);
        super.render(g, mouseX, mouseY, pt);
    }

    private static String grp(int v) {
        return v == 0 ? " (lisas)" : v == 1 ? " (rayadas)" : "";
    }
}
