package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Taca-taca: rojo contra azul (hasta 2 por equipo). Cada pulsación de ESPACIO (o clic) empuja la pelota hacia el
 * arco rival; el primero en llegar a 5 goles gana. Acciones: 1 = patear, 9 = reiniciar.
 */
public class FoosballSession extends AbstractSession {
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
    private String msg = "Siéntate para jugar: rojo contra azul.";

    public FoosballSession() {
        super(BarGame.FOOSBALL);
    }

    private void reset(String why) {
        red.clear();
        blue.clear();
        names.clear();
        lastClick.clear();
        started = false;
        ball = LENGTH / 2;
        redGoals = 0;
        blueGoals = 0;
        msg = why;
    }

    @Override
    protected void onUse(ServerLevel level, BlockPos pos, ServerPlayer player) {
        UUID id = player.getUUID();
        if (red.contains(id) || blue.contains(id) || (red.size() >= 2 && blue.size() >= 2)) {
            return;
        }
        boolean toRed = red.size() <= blue.size();
        (toRed ? red : blue).add(id);
        names.put(id, player.getName().getString());
        if (!started && !red.isEmpty() && !blue.isEmpty()) {
            started = true;
            ball = LENGTH / 2;
            lastAction = level.getGameTime();
            msg = "¡Comienza el taca-taca! Aprieta ESPACIO para empujar la pelota. Primero a " + GOALS_TO_WIN + ".";
        } else {
            msg = player.getName().getString() + " se une al equipo " + (toRed ? "ROJO" : "AZUL") + ".";
        }
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        UUID id = player.getUUID();
        boolean isRed = red.contains(id);
        if (type == 9) {
            if (isRed || blue.contains(id)) {
                reset("La mesa se reinició.");
                sync(level, pos);
            }
            return;
        }
        if (type != 1 || !started || (!isRed && !blue.contains(id))) {
            return;
        }
        long now = level.getGameTime();
        if (now - lastClick.getOrDefault(id, -10L) < 3) {
            return;
        }
        lastClick.put(id, now);
        lastAction = now;
        int step = level.getRandom().nextFloat() < 0.15F ? 2 : 1;
        ball += isRed ? step : -step;
        Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.3F);
        if (ball >= LENGTH) {
            redGoals++;
            goal(level, pos, "¡GOOOL del equipo ROJO!");
        } else if (ball <= 0) {
            blueGoals++;
            goal(level, pos, "¡GOOOL del equipo AZUL!");
        }
        sync(level, pos);
    }

    private void goal(ServerLevel level, BlockPos pos, String text) {
        Msg.sound(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F);
        if (redGoals >= GOALS_TO_WIN || blueGoals >= GOALS_TO_WIN) {
            String team = redGoals > blueGoals ? "ROJO" : "AZUL";
            Msg.say(level, pos, "§6§l¡Gana el equipo " + team + " el taca-taca! §r§f" + redGoals + " - " + blueGoals);
            int r = redGoals, b = blueGoals;
            reset("¡Gana el equipo " + team + "! (" + r + " - " + b + ")");
            return;
        }
        ball = LENGTH / 2;
        msg = text;
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (started && level.getGameTime() - lastAction > 600) {
            reset("Nadie jugaba: se cerró la mesa.");
            sync(level, pos);
        }
    }

    private String join(List<UUID> team) {
        StringBuilder sb = new StringBuilder();
        for (UUID u : team) {
            sb.append(sb.length() > 0 ? ", " : "").append(names.get(u));
        }
        return sb.toString();
    }

    private String ids(List<UUID> team) {
        StringBuilder sb = new StringBuilder();
        for (UUID u : team) {
            sb.append(sb.length() > 0 ? "," : "").append(u);
        }
        return sb.toString();
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        t.putString("red", join(red));
        t.putString("blue", join(blue));
        t.putString("redIds", ids(red));
        t.putString("blueIds", ids(blue));
        t.putInt("ball", ball);
        t.putInt("length", LENGTH);
        t.putInt("redGoals", redGoals);
        t.putInt("blueGoals", blueGoals);
        t.putBoolean("started", started);
        t.putString("msg", msg);
        return t;
    }
}
