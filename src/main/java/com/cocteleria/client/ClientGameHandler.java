package com.cocteleria.client;

import com.cocteleria.net.GameViewPacket;
import net.minecraft.client.Minecraft;

/** Recibe en el cliente el estado de una mesa y abre/actualiza su pantalla. */
public final class ClientGameHandler {
    private ClientGameHandler() {
    }

    public static void handle(GameViewPacket p) {
        Minecraft mc = Minecraft.getInstance();
        GameScreen screen;
        if (mc.screen instanceof GameScreen current && current.pos.equals(p.pos) && current.game == p.game) {
            screen = current;
        } else if (p.open) {
            screen = GameScreens.create(p.game, p.pos);
            mc.setScreen(screen);
        } else {
            return; // actualización de una mesa que ya no tenemos abierta
        }
        screen.apply(p.data);
    }
}
