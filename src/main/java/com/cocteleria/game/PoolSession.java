package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

/**
 * Billar por turnos para dos jugadores (versión simplificada, sin física): cada clic es un tiro.
 * Hay que embocar 7 bolas y luego la bola 8. Si emboca, sigue tirando; si falla, pasa el turno.
 * Una falta (blanca adentro) regala una bola al rival. Shift + clic reinicia.
 */
public class PoolSession implements GameSession {
    private static final int BALLS = 7;

    private final UUID[] players = new UUID[2];
    private final String[] names = {"", ""};
    private final int[] potted = new int[2];
    private int turn = 0;
    private long lastAction = 0;

    private void reset() {
        players[0] = null;
        players[1] = null;
        potted[0] = 0;
        potted[1] = 0;
        turn = 0;
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);
        long now = level.getGameTime();

        if (Msg.isSpecial(player)) {
            reset();
            Msg.say(level, pos, "§e" + name + " reinicia la partida de billar.");
            return;
        }

        int me = id.equals(players[0]) ? 0 : id.equals(players[1]) ? 1 : -1;
        if (me < 0) {
            if (players[0] == null) {
                players[0] = id;
                names[0] = name;
                Msg.say(level, pos, "§a" + name + " arma el triángulo de billar. ¡Falta un rival!");
            } else if (players[1] == null) {
                players[1] = id;
                names[1] = name;
                turn = 0;
                lastAction = now;
                Msg.say(level, pos, "§a" + name + " se une. §eComienza " + names[0] + ". §7(clic derecho = tirar; 7 bolas y luego la 8)");
            } else {
                Msg.bar(player, "§cMesa ocupada: " + names[0] + " vs " + names[1]);
            }
            return;
        }
        if (players[1] == null) {
            Msg.bar(player, "§eEsperando rival...");
            return;
        }
        if (me != turn) {
            Msg.bar(player, "§cNo es tu turno: tira " + names[turn]);
            return;
        }

        lastAction = now;
        RandomSource r = level.getRandom();
        int other = 1 - me;
        Msg.sound(level, pos, SoundEvents.STONE_BUTTON_CLICK_ON, 0.7F + r.nextFloat() * 0.3F);

        if (potted[me] >= BALLS) { // tiro a la bola 8
            if (r.nextFloat() < 0.40F) {
                Msg.say(level, pos, "§6§l¡" + names[me] + " emboca la bola 8 y gana la partida de billar!");
                reset();
            } else {
                Msg.say(level, pos, "§7" + names[me] + " falla la bola 8. Turno de " + names[other] + ".");
                turn = other;
            }
            return;
        }

        float x = r.nextFloat();
        if (x < 0.08F) {
            if (potted[other] < BALLS) {
                potted[other]++;
            }
            Msg.say(level, pos, "§c" + names[me] + " mete la blanca: falta. §7" + names[other] + " recibe una bola. " + score());
            turn = other;
        } else if (x < 0.45F) {
            Msg.say(level, pos, "§7" + names[me] + " no emboca nada. Turno de " + names[other] + ". " + score());
            turn = other;
        } else {
            int n = x < 0.80F ? 1 : x < 0.95F ? 2 : 3;
            potted[me] = Math.min(BALLS, potted[me] + n);
            String extra = potted[me] >= BALLS ? " §e¡Ahora va por la bola 8!" : " §7¡Sigue tirando!";
            Msg.say(level, pos, "§a" + names[me] + " emboca " + n + (n == 1 ? " bola." : " bolas.") + extra + " " + score());
        }
    }

    private String score() {
        return "§f[" + names[0] + " " + potted[0] + "/" + BALLS + " - " + potted[1] + "/" + BALLS + " " + names[1] + "]";
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (players[0] != null && level.getGameTime() - lastAction > 2400) {
            Msg.say(level, pos, "§7La partida de billar se abandonó.");
            reset();
        }
    }
}
