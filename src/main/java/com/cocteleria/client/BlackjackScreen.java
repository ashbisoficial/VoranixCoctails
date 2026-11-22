package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

/** Veintiuno contra el crupier: Repartir / Pedir carta / Plantarse. */
public class BlackjackScreen extends GameScreen {
    private static final String[] RANK = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUIT = {"♥", "♦", "♣", "♠"};

    public BlackjackScreen(BlockPos pos) {
        super("Veintiuno", pos, BarGame.BLACKJACK);
    }

    @Override
    protected void build() {
        int y = height - 28;
        addRenderableWidget(Button.builder(Component.literal("Repartir / Unirme"), b -> send(1, 0, 0, 0))
                .bounds(width / 2 - 155, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Pedir carta"), b -> send(2, 0, 0, 0))
                .bounds(width / 2 - 50, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Plantarme"), b -> send(3, 0, 0, 0))
                .bounds(width / 2 + 55, y, 100, 20).build());
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
        g.fill(20, 24, width - 20, height - 40, 0xFF0F5132);
        boolean playing = data.getBoolean("playing");
        int[] dealer = data.getIntArray("dealer");
        text(g, "Crupier", 34, 30, 0xFFFFFFFF);
        for (int i = 0; i < dealer.length; i++) {
            card(g, 34 + i * 34, 42, dealer[i], playing && i == 1);
        }
        ListTag hands = data.getList("hands", 10);
        int y = 100;
        for (int i = 0; i < hands.size(); i++) {
            CompoundTag h = hands.getCompound(i);
            boolean mine = isMe(h.getString("id"));
            String label = h.getString("name") + " · " + h.getInt("value") + (h.getBoolean("done") && playing ? " (plantado)" : "")
                    + (h.getString("result").isEmpty() ? "" : " · " + h.getString("result"));
            text(g, label, 34, y, mine ? 0xFF7CFC9A : 0xFFFFFFFF);
            int[] cards = h.getIntArray("cards");
            for (int k = 0; k < cards.length; k++) {
                card(g, 34 + k * 34, y + 11, cards[k], false);
            }
            y += 62;
            if (y > height - 110) {
                break;
            }
        }
        wrapped(g, data.getString("msg"), width / 2 + 20, 34, width / 2 - 60, 0xFFFFFFFF);
        super.render(g, mouseX, mouseY, pt);
    }
}
