package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Taca-taca: equipo rojo contra azul (hasta 2 jugadores por equipo). Cada clic empuja la pelota hacia el arco rival;
 * al llegar al fondo es gol. El primero en llegar a 5 goles gana. Shift + clic reinicia.
 */
public class FoosballSession implements GameSession {
    private static final int LENGTH = 10;
    private static final int GOALS_TO_WIN = 5;

    private final List<UUID> red = new ArrayList<>();
    private final List<UUID> blue = new ArrayList<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, Long> lastClick = new HashMap<>();
    private boolean started = false;
    private int ball = LENGTH / 2;
    private int redGoals = 0, blueGoals = 0;
    private long lastAction = 0;

    private void reset() {
        red.clear();
        blue.clear();
        names.clear();
        lastClick.clear();
        started = false;
        ball = LENGTH / 2;
        redGoals = 0;
        blueGoals = 0;
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);
        long now = level.getGameTime();

        if (Msg.isSpecial(player)) {
            reset();
            Msg.say(level, pos, "§e" + name + " reinicia el taca-taca.");
            return;
        }

        if (!red.contains(id) && !blue.contains(id)) {
            if (red.size() >= 2 && blue.size() >= 2) {
                Msg.bar(player, "§cMesa llena (2 contra 2).");
                return;
            }
            boolean toRed = red.size() <= blue.size();
            if (toRed) {
                red.add(id);
            } else {
                blue.add(id);
            }
            names.put(id, name);
            Msg.say(level, pos, (toRed ? "§c" : "§9") + name + " se une al equipo " + (toRed ? "ROJO" : "AZUL") + ".");
            if (!started && !red.isEmpty() && !blue.isEmpty()) {
                started = true;
                ball = LENGTH / 2;
                lastAction = now;
                Msg.say(level, pos, "§a§l¡Comienza el taca-taca! §r§7Clic derecho repetido para empujar la pelota. Primero a " + GOALS_TO_WIN + " goles.");
            }
            return;
        }
        if (!started) {
            Msg.bar(player, "§eEsperando rival...");
            return;
        }

        long last = lastClick.getOrDefault(id, -10L);
        if (now - last < 3) {
            return;
        }
        lastClick.put(id, now);
        lastAction = now;

        boolean isRed = red.contains(id);
        int step = level.getRandom().nextFloat() < 0.15F ? 2 : 1;
        ball += isRed ? step : -step;
        Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.3F);

        if (ball >= LENGTH) {
            redGoals++;
            goal(level, pos, "§c§l¡GOOOL del equipo ROJO!");
        } else if (ball <= 0) {
            blueGoals++;
            goal(level, pos, "§9§l¡GOOOL del equipo AZUL!");
        } else {
            showBar(level);
        }
    }

    private void goal(ServerLevel level, BlockPos pos, String text) {
        Msg.sound(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F);
        Msg.say(level, pos, text + " §fRojo " + redGoals + " - " + blueGoals + " Azul");
        if (redGoals >= GOALS_TO_WIN || blueGoals >= GOALS_TO_WIN) {
            Msg.say(level, pos, "§6§l¡Gana el equipo " + (redGoals > blueGoals ? "ROJO" : "AZUL") + "!");
            reset();
            return;
        }
        ball = LENGTH / 2;
        showBar(level);
    }

    private void showBar(ServerLevel level) {
        StringBuilder sb = new StringBuilder("§cRojo " + redGoals + " §7[");
        for (int i = 0; i <= LENGTH; i++) {
            sb.append(i == ball ? "§e●" : "§8·");
        }
        sb.append("§7] §9" + blueGoals + " Azul");
        for (UUID u : names.keySet()) {
            Msg.bar(level.getPlayerByUUID(u), sb.toString());
        }
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (started && level.getGameTime() - lastAction > 600) {
            Msg.say(level, pos, "§7Nadie juega hace rato: se cierra el taca-taca.");
            reset();
        }
    }
}
