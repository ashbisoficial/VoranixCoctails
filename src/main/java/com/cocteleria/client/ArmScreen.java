package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Gallito: tras la cuenta atrás, aprieta CLIC DERECHO lo más rápido posible para llevar la mano rival hasta la mesa. */
public class ArmScreen extends GameScreen {
    private float shown = 0;

    public ArmScreen(BlockPos pos) {
        super("Gallito", pos, BarGame.ARM);
    }

    @Override
    protected void build() {
        addRenderableWidget(Button.builder(Component.literal("Abandonar"), b -> send(9, 0, 0, 0))
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 1) { // clic derecho = empujar
            send(1, 0, 0, 0);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        renderBackground(g);
        int limit = Math.max(1, data.getInt("limit"));
        shown += (data.getInt("pos") - shown) * 0.3f;
        int phase = data.getInt("phase");
        int bw = Math.min(width - 80, 400), bx = (width - bw) / 2, by = 100;
        String a = data.getString("a").isEmpty() ? "(libre)" : data.getString("a");
        String b = data.getString("b").isEmpty() ? "(libre)" : data.getString("b");
        text(g, a, bx, 40, 0xFFFF7F7F);
        text(g, b, bx + bw - font.width(b), 40, 0xFF7FB8FF);
        // barra de tira y afloja
        g.fill(bx - 2, by - 2, bx + bw + 2, by + 22, 0xFF000000);
        g.fill(bx, by, bx + bw / 2, by + 20, 0xFF9B2226);
        g.fill(bx + bw / 2, by, bx + bw, by + 20, 0xFF1D4E89);
        float frac = Math.max(-1, Math.min(1, shown / limit));
        int mx = (int) (bx + bw / 2 + frac * bw / 2);
        g.fill(mx - 3, by - 8, mx + 3, by + 28, 0xFFFFFFFF);
        // brazos
        int ay = by + 70;
        g.fill(bx + 20, ay, mx, ay + 10, 0xFFE0AC69);
        g.fill(mx, ay, bx + bw - 20, ay + 10, 0xFFC68642);
        circle(g, mx, ay + 5, 9, 0xFFEFBE8C);
        g.fill(bx, ay + 12, bx + bw, ay + 18, 0xFF5C3A1E); // mesa
        if (phase == 1) {
            bigCenter(g, data.getInt("count") > 0 ? String.valueOf(data.getInt("count")) : "¡YA!", width / 2, by + 110, 4f, 0xFFFFD27F);
        } else if (phase == 2) {
            bigCenter(g, "¡CLIC DERECHO! ¡CLIC DERECHO!", width / 2, by + 110, 2.5f, 0xFF9AE6B4);
        }
        String side = isMe(data.getString("aid")) ? "Eres el lado ROJO: empuja hacia la derecha"
                : isMe(data.getString("bid")) ? "Eres el lado AZUL: empuja hacia la izquierda" : "Espectador";
        center(g, side, width / 2, by + 150, 0xFFAAAAAA);
        wrapped(g, data.getString("msg"), bx, by + 165, bw, 0xFFE0E0E0);
        super.render(g, mouseX, mouseY, pt);
    }
}
