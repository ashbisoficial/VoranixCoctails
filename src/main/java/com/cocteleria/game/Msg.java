package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Utilidades de mensajes y sonidos para las mesas de juego. */
final class Msg {
    private Msg() {
    }

    static final String PREFIX = "§6[Bar] §r";

    /** Mensaje de chat para todos los jugadores cerca de la mesa. */
    static void say(ServerLevel level, BlockPos pos, String text) {
        broadcast(level, pos, Component.literal(PREFIX + text));
    }

    static void broadcast(ServerLevel level, BlockPos pos, Component component) {
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(24))) {
            p.sendSystemMessage(component);
        }
    }

    /** Mensaje en la barra de acción de un jugador. */
    static void bar(Player player, String text) {
        if (player != null) {
            player.displayClientMessage(Component.literal(text), true);
        }
    }

    static void sound(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.9F, pitch);
    }

    /** Shift + clic derecho con las manos vacías = acción especial (reiniciar / plantarse). */
    static boolean isSpecial(Player player) {
        return player.isShiftKeyDown() && player.getMainHandItem().isEmpty();
    }

    static String name(Player player) {
        return player.getName().getString();
    }
}
