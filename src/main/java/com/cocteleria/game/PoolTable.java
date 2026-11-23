package com.cocteleria.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Física de billar en 2D (mesa 200 x 100, bolas de radio 2.25). Bola 0 = blanca, 1-7 lisas, 8 negra, 9-15 rayadas.
 * Las troneras están en las 4 esquinas y en el centro de los lados largos.
 */
public class PoolTable {
    public static final float W = 200F, H = 100F, R = 2.25F;
    private static final float CORNER_POCKET = 6.0F, SIDE_POCKET = 5.5F;
    private static final int SUBSTEPS = 14;

    public final float[] x = new float[16], y = new float[16], vx = new float[16], vy = new float[16];
    public final boolean[] potted = new boolean[16];

    /** Primera bola que tocó la blanca en el tiro actual (-1 = ninguna). */
    public int firstContact = -1;
    public boolean cuePotted;
    public final List<Integer> pottedThisShot = new ArrayList<>();

    public PoolTable() {
        rack();
    }

    public void rack() {
        int[][] rows = {{1}, {9, 2}, {10, 8, 3}, {11, 4, 12, 5}, {6, 13, 14, 7, 15}};
        float apexX = W * 0.70F;
        float dx = 2 * R * 0.866F + 0.02F;
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                int n = rows[r][c];
                x[n] = apexX + r * dx;
                y[n] = H / 2 + (c - r / 2.0F) * (2 * R + 0.02F);
            }
        }
        placeCue(W * 0.25F, H / 2);
        for (int i = 0; i < 16; i++) {
            potted[i] = false;
            if (i > 0) {
                vx[i] = 0;
                vy[i] = 0;
            }
        }
        firstContact = -1;
        cuePotted = false;
        pottedThisShot.clear();
    }

    public void placeCue(float px, float py) {
        x[0] = px;
        y[0] = py;
        vx[0] = 0;
        vy[0] = 0;
        potted[0] = false;
    }

    public boolean isFree(float px, float py) {
        if (px < R || px > W - R || py < R || py > H - R) {
            return false;
        }
        for (int i = 1; i < 16; i++) {
            if (!potted[i] && Math.hypot(x[i] - px, y[i] - py) < 2 * R + 0.05) {
                return false;
            }
        }
        return true;
    }

    public void shoot(float angle, float power) {
        vx[0] = (float) Math.cos(angle) * power;
        vy[0] = (float) Math.sin(angle) * power;
        firstContact = -1;
        cuePotted = false;
        pottedThisShot.clear();
    }

    public boolean moving() {
        for (int i = 0; i < 16; i++) {
            if (!potted[i] && (vx[i] != 0 || vy[i] != 0)) {
                return true;
            }
        }
        return false;
    }

    /** Avanza un tick de juego. */
    public void step() {
        float dt = 1.0F / SUBSTEPS;
        for (int s = 0; s < SUBSTEPS; s++) {
            for (int i = 0; i < 16; i++) {
                if (potted[i]) {
                    continue;
                }
                x[i] += vx[i] * dt;
                y[i] += vy[i] * dt;
                checkPockets(i);
                if (potted[i]) {
                    continue;
                }
                if (x[i] < R) {
                    x[i] = R;
                    vx[i] = -vx[i] * 0.78F;
                } else if (x[i] > W - R) {
                    x[i] = W - R;
                    vx[i] = -vx[i] * 0.78F;
                }
                if (y[i] < R) {
                    y[i] = R;
                    vy[i] = -vy[i] * 0.78F;
                } else if (y[i] > H - R) {
                    y[i] = H - R;
                    vy[i] = -vy[i] * 0.78F;
                }
            }
            collide();
        }
        // rozamiento
        for (int i = 0; i < 16; i++) {
            if (potted[i]) {
                continue;
            }
            float speed = (float) Math.hypot(vx[i], vy[i]);
            if (speed == 0) {
                continue;
            }
            float ns = speed * 0.986F - 0.025F;
            if (ns < 0.03F) {
                vx[i] = 0;
                vy[i] = 0;
            } else {
                vx[i] *= ns / speed;
                vy[i] *= ns / speed;
            }
        }
    }

    private void checkPockets(int i) {
        float[][] pockets = {{0, 0, CORNER_POCKET}, {W, 0, CORNER_POCKET}, {0, H, CORNER_POCKET},
                {W, H, CORNER_POCKET}, {W / 2, -1.5F, SIDE_POCKET}, {W / 2, H + 1.5F, SIDE_POCKET}};
        for (float[] p : pockets) {
            if (Math.hypot(x[i] - p[0], y[i] - p[1]) < p[2]) {
                potted[i] = true;
                vx[i] = 0;
                vy[i] = 0;
                if (i == 0) {
                    cuePotted = true;
                } else {
                    pottedThisShot.add(i);
                }
                return;
            }
        }
    }

    private void collide() {
        for (int i = 0; i < 16; i++) {
            if (potted[i]) {
                continue;
            }
            for (int j = i + 1; j < 16; j++) {
                if (potted[j]) {
                    continue;
                }
                float dx = x[j] - x[i], dy = y[j] - y[i];
                float d2 = dx * dx + dy * dy;
                float min = 2 * R;
                if (d2 >= min * min || d2 == 0) {
                    continue;
                }
                float d = (float) Math.sqrt(d2);
                float nx = dx / d, ny = dy / d;
                float overlap = (min - d) / 2;
                x[i] -= nx * overlap;
                y[i] -= ny * overlap;
                x[j] += nx * overlap;
                y[j] += ny * overlap;
                float vi = vx[i] * nx + vy[i] * ny;
                float vj = vx[j] * nx + vy[j] * ny;
                float diff = vj - vi;
                if (diff < 0) { // se acercan
                    vx[i] += diff * nx * 0.97F;
                    vy[i] += diff * ny * 0.97F;
                    vx[j] -= diff * nx * 0.97F;
                    vy[j] -= diff * ny * 0.97F;
                    if (firstContact < 0 && (i == 0 || j == 0)) {
                        firstContact = i == 0 ? j : i;
                    }
                }
            }
        }
    }
}
