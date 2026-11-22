package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Duelo de dados: el primero que tira abre una ronda de 10 segundos; todos los que quieran tiran 2 dados
 * (un intento cada uno) y gana la suma más alta. Shift + clic cancela la ronda.
 */
public class DiceSession implements GameSession {
    private static final int WINDOW = 200;

    private final Map<UUID, Integer> totals = new LinkedHashMap<>();
    private final Map<UUID, String> names = new LinkedHashMap<>();
    private long start = -1;

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);
        long now = level.getGameTime();

        if (Msg.isSpecial(player)) {
            totals.clear();
            names.clear();
            start = -1;
            Msg.say(level, pos, "§e" + name + " cancela la ronda de dados.");
            return;
        }
        if (totals.containsKey(id)) {
            Msg.bar(player, "§cYa tiraste en esta ronda.");
            return;
        }
        if (start < 0) {
            start = now;
            Msg.say(level, pos, "§e" + name + " abre una ronda de dados. §7¡Tienen 10 segundos para tirar (clic derecho)!");
        }
        int d1 = 1 + level.getRandom().nextInt(6);
        int d2 = 1 + level.getRandom().nextInt(6);
        totals.put(id, d1 + d2);
        names.put(id, name);
        Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.6F);
        Msg.say(level, pos, "§f" + name + " §7tira: §e" + d1 + " §7y §e" + d2 + " §7= §b" + (d1 + d2) + (d1 == d2 ? " §6(¡dobles!)" : ""));
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (start < 0 || level.getGameTime() - start < WINDOW) {
            return;
        }
        if (totals.size() < 2) {
            Msg.say(level, pos, "§7Nadie aceptó el desafío de dados.");
        } else {
            int best = 0;
            for (int v : totals.values()) {
                best = Math.max(best, v);
            }
            List<String> winners = new ArrayList<>();
            for (Map.Entry<UUID, Integer> e : totals.entrySet()) {
                if (e.getValue() == best) {
                    winners.add(names.get(e.getKey()));
                }
            }
            if (winners.size() == 1) {
                Msg.say(level, pos, "§6§l¡" + winners.get(0) + " gana la ronda de dados con " + best + "!");
            } else {
                Msg.say(level, pos, "§6Empate a " + best + " entre " + String.join(", ", winners) + ".");
            }
        }
        totals.clear();
        names.clear();
        start = -1;
    }
}
