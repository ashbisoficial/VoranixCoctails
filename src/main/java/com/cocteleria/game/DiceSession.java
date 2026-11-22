package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Duelo de dados: ronda de 10 s, cada jugador tira 2 dados una vez; gana la suma más alta. Acción 1 = tirar. */
public class DiceSession extends AbstractSession {
    private static final int WINDOW = 200;

    private static final class Roll {
        final String name;
        final int d1, d2;

        Roll(String name, int d1, int d2) {
            this.name = name;
            this.d1 = d1;
            this.d2 = d2;
        }
    }

    private final Map<UUID, Roll> rolls = new LinkedHashMap<>();
    private boolean open = false;
    private long start = 0;
    private String msg = "Tira los dados: el primero abre una ronda de 10 segundos.";

    public DiceSession() {
        super(BarGame.DICE);
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        if (type != 1) {
            return;
        }
        UUID id = player.getUUID();
        long now = level.getGameTime();
        if (!open) {
            rolls.clear();
            open = true;
            start = now;
            msg = player.getName().getString() + " abre una ronda: ¡tienen 10 segundos para tirar!";
        }
        if (rolls.containsKey(id)) {
            return;
        }
        int d1 = 1 + level.getRandom().nextInt(6);
        int d2 = 1 + level.getRandom().nextInt(6);
        rolls.put(id, new Roll(player.getName().getString(), d1, d2));
        Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.6F);
        sync(level, pos);
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (!open) {
            return;
        }
        long now = level.getGameTime();
        if ((now - start) % 20 == 0 && now - start < WINDOW) {
            sync(level, pos);
        }
        if (now - start < WINDOW) {
            return;
        }
        open = false;
        if (rolls.size() < 2) {
            msg = "Nadie aceptó el desafío de dados.";
        } else {
            int best = 0;
            for (Roll r : rolls.values()) {
                best = Math.max(best, r.d1 + r.d2);
            }
            List<String> winners = new ArrayList<>();
            for (Roll r : rolls.values()) {
                if (r.d1 + r.d2 == best) {
                    winners.add(r.name);
                }
            }
            if (winners.size() == 1) {
                msg = "¡" + winners.get(0) + " gana la ronda con " + best + "!";
                Msg.say(level, pos, "§6§l" + winners.get(0) + " gana la ronda de dados con " + best + ".");
            } else {
                msg = "Empate a " + best + " entre " + String.join(", ", winners) + ".";
            }
        }
        sync(level, pos);
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Roll> e : rolls.entrySet()) {
            CompoundTag r = new CompoundTag();
            r.putString("name", e.getValue().name);
            r.putString("id", e.getKey().toString());
            r.putInt("d1", e.getValue().d1);
            r.putInt("d2", e.getValue().d2);
            list.add(r);
        }
        t.put("rolls", list);
        t.putBoolean("open", open);
        t.putInt("left", open ? Math.max(0, (int) ((WINDOW - (level.getGameTime() - start)) / 20)) : 0);
        t.putString("msg", msg);
        return t;
    }
}
