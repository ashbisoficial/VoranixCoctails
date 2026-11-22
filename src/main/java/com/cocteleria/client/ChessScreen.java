package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Tablero de ajedrez: clic en tu pieza y luego en la casilla destino. El jugador de negras ve el tablero girado. */
public class ChessScreen extends GameScreen {
    private static final int CELL = 34;
    private static final String LETTERS = " PNBRQK";
    private int bx, by;

    public ChessScreen(BlockPos pos) {
        super("Ajedrez", pos, BarGame.CHESS);
    }

    @Override
    protected void build() {
        bx = (width - CELL * 8 - 200) / 2;
        by = (height - CELL * 8) / 2;
        addRenderableWidget(Button.builder(Component.literal("Reiniciar partida"), b -> send(9, 0, 0, 0))
                .bounds(bx + CELL * 8 + 20, by + CELL * 8 - 20, 130, 20).build());
    }

    private boolean flipped() {
        return isMe(data.getString("bid"));
    }

    private int squareAt(double mx, double my) {
        int col = (int) Math.floor((mx - bx) / CELL), row = (int) Math.floor((my - by) / CELL);
        if (col < 0 || col > 7 || row < 0 || row > 7) {
            return -1;
        }
        int file = flipped() ? 7 - col : col;
        int rank = flipped() ? row : 7 - row;
        return rank * 8 + file;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        int sq = squareAt(mx, my);
        if (sq >= 0) {
            send(1, sq, 0, 0);
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int[] board = data.getIntArray("board");
        int[] targets = data.getIntArray("targets");
        int sel = data.contains("sel") ? data.getInt("sel") : -1;
        int lastFrom = data.getInt("lastFrom"), lastTo = data.getInt("lastTo");
        boolean flipped = flipped();

        g.fill(bx - 6, by - 6, bx + CELL * 8 + 6, by + CELL * 8 + 6, 0xFF3B2A1A);
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int col = flipped ? 7 - file : file, row = flipped ? rank : 7 - rank;
                int x = bx + col * CELL, y = by + row * CELL;
                int sq = rank * 8 + file;
                g.fill(x, y, x + CELL, y + CELL, ((file + rank) % 2 == 0) ? 0xFFB58863 : 0xFFF0D9B5);
                if (sq == lastFrom || sq == lastTo) {
                    g.fill(x, y, x + CELL, y + CELL, 0x66FFEE00);
                }
                if (sq == sel) {
                    g.fill(x, y, x + CELL, y + CELL, 0x9900CC44);
                }
                for (int t : targets) {
                    if (t == sq) {
                        if (board.length == 64 && board[sq] != 0) {
                            g.fill(x, y, x + CELL, y + CELL, 0x88FF3030);
                        } else {
                            circle(g, x + CELL / 2f, y + CELL / 2f, 5, 0xAA1E7A3C);
                        }
                    }
                }
                if (board.length == 64 && board[sq] != 0) {
                    int p = board[sq];
                    boolean white = p > 0;
                    float cx = x + CELL / 2f, cy = y + CELL / 2f;
                    circle(g, cx, cy, 14, white ? 0xFF202020 : 0xFFE8E8E8);
                    circle(g, cx, cy, 12, white ? 0xFFF8F8F8 : 0xFF2A2A2A);
                    center(g, String.valueOf(LETTERS.charAt(Math.abs(p))), (int) cx, (int) cy - 4, white ? 0xFF202020 : 0xFFF0F0F0);
                }
            }
            // coordenadas
            int row = flipped ? rank : 7 - rank;
            text(g, String.valueOf(rank + 1), bx - 5 - 3, by + row * CELL + 12, 0xFFCCCCCC);
        }
        for (int file = 0; file < 8; file++) {
            int col = flipped ? 7 - file : file;
            text(g, String.valueOf((char) ('a' + file)), bx + col * CELL + 14, by + CELL * 8 + 8, 0xFFCCCCCC);
        }

        // panel lateral
        int px = bx + CELL * 8 + 20, py = by;
        panel(g, px, py, 160, 120);
        boolean turnWhite = data.getBoolean("turnWhite");
        String w = data.getString("white").isEmpty() ? "(libre)" : data.getString("white");
        String b = data.getString("black").isEmpty() ? "(libre)" : data.getString("black");
        text(g, (turnWhite ? "► " : "  ") + "Blancas: " + w, px + 6, py + 8, 0xFFFFFFFF);
        text(g, (!turnWhite ? "► " : "  ") + "Negras: " + b, px + 6, py + 22, 0xFFFFD27F);
        if (isMe(data.getString("wid"))) {
            text(g, "Juegas con BLANCAS", px + 6, py + 40, 0xFF7CFC9A);
        } else if (isMe(data.getString("bid"))) {
            text(g, "Juegas con NEGRAS", px + 6, py + 40, 0xFF7CFC9A);
        } else {
            text(g, "Espectador", px + 6, py + 40, 0xFFAAAAAA);
        }
        wrapped(g, data.getString("msg"), px + 6, py + 58, 148, 0xFFE0E0E0);
        super.render(g, mouseX, mouseY, partialTick);
    }
}
