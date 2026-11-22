package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Veintiuno con crupier humano (staff): los botones cambian según seas crupier, jugador o espectador. */
public class BlackjackScreen extends GameScreen {
    private static final int LOBBY = 0, PLAYING = 1, DEALER = 2, RESULT = 3;
    private static final String[] RANK = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUIT = {"♥", "♦", "♣", "♠"};

    public BlackjackScreen(BlockPos pos) {
        super("Blackjack", pos, BarGame.BLACKJACK);
    }

    @Override
    protected void onData() {
        rebuildWidgets(); // los botones dependen del rol y de la fase
    }

    private record Act(String label, int type) {
    }

    @Override
    protected void build() {
        int phase = data.getInt("phase");
        boolean dealer = isMe(data.getString("did"));
        boolean hasDealer = !data.getString("did").isEmpty();
        ListTag hands = data.getList("hands", 10);
        boolean player = false, mineDone = true;
        for (int i = 0; i < hands.size(); i++) {
            if (isMe(hands.getCompound(i).getString("id"))) {
                player = true;
                mineDone = hands.getCompound(i).getBoolean("done");
            }
        }
        List<Act> acts = new ArrayList<>();
        if (dealer) {
            if (phase == LOBBY || phase == RESULT) {
                acts.add(new Act("Repartir ronda", 3));
            } else if (phase == PLAYING) {
                acts.add(new Act("Cerrar jugadas", 8));
            } else if (phase == DEALER) {
                acts.add(new Act("Pedir carta", 6));
                acts.add(new Act("Plantarme y resolver", 7));
            }
            acts.add(new Act("Cerrar mesa", 9));
        } else if (player) {
            if (phase == PLAYING && !mineDone) {
                acts.add(new Act("Pedir carta", 4));
                acts.add(new Act("Plantarme", 5));
            }
            acts.add(new Act("Salir", 9));
        } else {
            if (!hasDealer) {
                acts.add(new Act("Ser crupier (staff)", 1));
            }
            if ((phase == LOBBY || phase == RESULT) && hands.size() < data.getInt("max")) {
                acts.add(new Act("Unirme como jugador", 2));
            }
        }
        int bw = 118, gap = 6;
        int total = acts.size() * bw + (acts.size() - 1) * gap;
        int x = (width - total) / 2;
        for (Act a : acts) {
            addRenderableWidget(Button.builder(Component.literal(a.label()), b -> send(a.type(), 0, 0, 0))
                    .bounds(x, height - 28, bw, 20).build());
            x += bw + gap;
        }
    }

    private void card(GuiGraphics g, int x, int y, int c, boolean hidden) {
        g.fill(x - 1, y - 1, x + 31, y + 45, 0xFF000000);
        if (hidden) {
            g.fill(x, y, x + 30, y + 44, 0xFF9B2226);
            g.fill(x + 3, y + 3, x + 27, y + 41, 0xFFD94A4F);
            return;
        }
        g.fill(x, y, x + 30, y + 44, 0xFFF8F8F2);
        int suit = c / 13;
        int col = suit < 2 ? 0xFFC0152B : 0xFF111111;
        g.drawString(font, RANK[c % 13], x + 3, y + 3, col);
        g.drawString(font, SUIT[suit], x + 11, y + 18, col);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        int phase = data.getInt("phase");
        g.fill(14, 14, width - 14, height - 40, 0xFF0F5132);
        // crupier
        String dn = data.getString("dealer").isEmpty() ? "(sin crupier)" : data.getString("dealer");
        boolean showAll = phase >= DEALER;
        String dv = showAll && data.getIntArray("dealerCards").length > 0 ? " · " + data.getInt("dealerValue") : "";
        text(g, "Crupier (staff): " + dn + dv, 26, 22, isMe(data.getString("did")) ? 0xFF7CFC9A : 0xFFFFD27F);
        int[] dc = data.getIntArray("dealerCards");
        for (int i = 0; i < dc.length; i++) {
            card(g, 26 + i * 34, 34, dc[i], !showAll && i == 1);
        }
        // jugadores en columnas (hasta 3)
        ListTag hands = data.getList("hands", 10);
        int colW = Math.max(150, (width - 52) / 3);
        for (int i = 0; i < hands.size(); i++) {
            CompoundTag h = hands.getCompound(i);
            int x = 26 + i * colW, y = 100;
            boolean mine = isMe(h.getString("id"));
            int[] cards = h.getIntArray("cards");
            String status = cards.length == 0 ? "" : " · " + h.getInt("value") + (h.getInt("value") > 21 ? " (se pasa)" : h.getBoolean("done") && phase == PLAYING ? " (plantado)" : "");
            text(g, h.getString("name") + status, x, y, mine ? 0xFF7CFC9A : 0xFFFFFFFF);
            for (int k = 0; k < cards.length; k++) {
                card(g, x + (k % 4) * 34, y + 12 + (k / 4) * 48, cards[k], false);
            }
            if (!h.getString("result").isEmpty()) {
                String r = h.getString("result");
                text(g, r, x, y + 112, r.equals("GANA") || r.equals("BLACKJACK") ? 0xFFFFDC00 : r.equals("empata") ? 0xFFAAAAAA : 0xFFFF7F7F);
            }
        }
        text(g, "Jugadores " + hands.size() + "/" + data.getInt("max"), 26, 90 - 12, 0xFFCCCCCC);
        wrapped(g, data.getString("msg"), 26, height - 76, width - 60, 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, pt);
    }
}
