package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

/**
 * Gallito (pulseada) para dos jugadores: ambos hacen clic derecho lo más rápido posible tras la cuenta atrás
 * para empujar el brazo del rival. Gana quien llegue al extremo: fuerza para el ganador, debilidad para el perdedor.
 */
public class ArmWrestlingSession implements GameSession {
    private enum Phase { WAITING, COUNTDOWN, PLAYING }

    private static final int LIMIT = 10;

    private Phase phase = Phase.WAITING;
    private UUID a, b;
    private String aName = "", bName = "";
    private int position = 0; // + empuja A, - empuja B
    private long phaseStart, lastAction, lastClickA, lastClickB;

    private void reset() {
        phase = Phase.WAITING;
        a = null;
        b = null;
        position = 0;
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);
        long now = level.getGameTime();

        if (Msg.isSpecial(player)) {
            reset();
            Msg.say(level, pos, "§e" + name + " cancela el gallito.");
            return;
        }

        if (!id.equals(a) && !id.equals(b)) {
            if (a == null) {
                a = id;
                aName = name;
                Msg.say(level, pos, "§c" + name + " apoya el codo en la mesa (lado rojo). ¡Falta un rival!");
            } else if (b == null) {
                b = id;
                bName = name;
                Msg.say(level, pos, "§9" + name + " acepta el reto (lado azul).");
                phase = Phase.COUNTDOWN;
                phaseStart = now;
                position = 0;
                Msg.say(level, pos, "§eGallito: §c" + aName + " §evs §9" + bName + " §e— ¡prepárense!");
            } else {
                Msg.bar(player, "§cMesa ocupada: " + aName + " vs " + bName);
            }
            return;
        }

        switch (phase) {
            case WAITING -> Msg.bar(player, "§eEsperando rival...");
            case COUNTDOWN -> Msg.bar(player, "§c¡Espera la señal!");
            case PLAYING -> {
                boolean isA = id.equals(a);
                long last = isA ? lastClickA : lastClickB;
                if (now - last < 2) {
                    return; // límite de velocidad para evitar autoclickers extremos
                }
                if (isA) {
                    lastClickA = now;
                    position++;
                } else {
                    lastClickB = now;
                    position--;
                }
                lastAction = now;
                Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.8F + Math.abs(position) * 0.05F);
                if (Math.abs(position) >= LIMIT) {
                    finish(level, pos);
                } else {
                    showBar(level);
                }
            }
        }
    }

    private void finish(ServerLevel level, BlockPos pos) {
        boolean aWins = position > 0;
        UUID winner = aWins ? a : b;
        UUID loser = aWins ? b : a;
        String winnerName = aWins ? aName : bName;
        Msg.say(level, pos, "§6§l¡" + winnerName + " gana el gallito! §r§7(fuerza para el ganador, debilidad para el perdedor)");
        Player w = level.getPlayerByUUID(winner);
        Player l = level.getPlayerByUUID(loser);
        if (w != null) {
            w.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
        }
        if (l != null) {
            l.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0));
        }
        reset();
    }

    private void showBar(ServerLevel level) {
        StringBuilder sb = new StringBuilder("§c" + aName + " ");
        int marker = position + LIMIT;
        for (int i = 0; i <= LIMIT * 2; i++) {
            if (i == marker) {
                sb.append("§e●");
            } else if (i == LIMIT) {
                sb.append("§7|");
            } else {
                sb.append(i < LIMIT ? "§c·" : "§9·");
            }
        }
        sb.append(" §9").append(bName);
        Msg.bar(level.getPlayerByUUID(a), sb.toString());
        Msg.bar(level.getPlayerByUUID(b), sb.toString());
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        long now = level.getGameTime();
        if (phase == Phase.COUNTDOWN) {
            long t = now - phaseStart;
            if (t == 20 || t == 40 || t == 60) {
                String n = String.valueOf(3 - (int) (t / 20) + 1);
                Msg.bar(level.getPlayerByUUID(a), "§e" + n);
                Msg.bar(level.getPlayerByUUID(b), "§e" + n);
                Msg.sound(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8F);
            } else if (t >= 80) {
                phase = Phase.PLAYING;
                lastAction = now;
                Msg.say(level, pos, "§a§l¡YA! §r§7¡Clic derecho lo más rápido que puedas!");
                Msg.sound(level, pos, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F);
            }
        } else if (phase == Phase.PLAYING && now - lastAction > 200) {
            Msg.say(level, pos, "§7Se cansaron de empujar: el gallito termina sin ganador.");
            reset();
        }
    }
}
