package com.cocteleria.client;

import com.cocteleria.game.BarGame;
import com.cocteleria.net.GameActionPacket;
import com.cocteleria.net.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.UUID;

/** Pantalla base de una mesa de juego: recibe el estado del servidor y le envía acciones. */
public abstract class GameScreen extends Screen {
    public final BlockPos pos;
    public final BarGame game;
    protected CompoundTag data = new CompoundTag();
    protected UUID me = new UUID(0, 0);

    protected GameScreen(String title, BlockPos pos, BarGame game) {
        super(Component.literal(title));
        this.pos = pos;
        this.game = game;
    }

    /** Llega un estado nuevo desde el servidor. */
    public void apply(CompoundTag tag) {
        this.data = tag;
        onData();
    }

    protected void onData() {
    }

    protected void send(int type, int a, int b, float f) {
        ModNetwork.CHANNEL.sendToServer(new GameActionPacket(pos, type, a, b, f));
    }

    @Override
    protected void init() {
        if (Minecraft.getInstance().player != null) {
            me = Minecraft.getInstance().player.getUUID();
        }
        build();
    }

    protected void build() {
    }

    @Override
    public void removed() {
        send(0, 0, 0, 0f); // avisa al servidor de que dejamos de mirar la mesa
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected boolean isMe(String id) {
        return id != null && !id.isEmpty() && id.equals(me.toString());
    }

    protected boolean isMeIn(String commaIds) {
        if (commaIds == null || commaIds.isEmpty()) {
            return false;
        }
        for (String s : commaIds.split(",")) {
            if (isMe(s.trim())) {
                return true;
            }
        }
        return false;
    }

    // ---------- utilidades de dibujo ----------

    protected void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, 0xFF2B2B33);
    }

    protected void text(GuiGraphics g, String s, int x, int y, int color) {
        g.drawString(font, s, x, y, color);
    }

    protected void center(GuiGraphics g, String s, int cx, int y, int color) {
        g.drawCenteredString(font, s, cx, y, color);
    }

    protected void bigCenter(GuiGraphics g, String s, int cx, int y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1.0F);
        g.drawCenteredString(font, s, 0, 0, color);
        g.pose().popPose();
    }

    /** Texto con salto de línea automático; devuelve la altura usada. */
    protected int wrapped(GuiGraphics g, String s, int x, int y, int width, int color) {
        List<FormattedCharSequence> lines = font.split(Component.literal(s), width);
        int yy = y;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, x, yy, color);
            yy += 10;
        }
        return yy - y;
    }

    protected void circle(GuiGraphics g, float cx, float cy, float r, int color) {
        int ir = (int) Math.ceil(r);
        for (int dy = -ir; dy <= ir; dy++) {
            double w2 = r * r - dy * dy;
            if (w2 < 0) {
                continue;
            }
            int w = (int) Math.floor(Math.sqrt(w2));
            int y = Math.round(cy) + dy;
            g.fill(Math.round(cx) - w, y, Math.round(cx) + w + 1, y + 1, color);
        }
    }
}
