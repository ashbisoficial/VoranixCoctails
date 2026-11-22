package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Billar americano (8 bolas) para dos jugadores con física real. Apuntas con el mouse, mantienes ESPACIO para cargar
 * la fuerza y sueltas para tirar. Acciones: 1 = tiro (f = ángulo, a = fuerza 0-1000), 2 = colocar la blanca (a, b = x, y * 10),
 * 9 = reiniciar.
 */
public class PoolSession extends AbstractSession {
    private static final int WAIT = 0, AIM = 1, ROLLING = 2, PLACE = 3, OVER = 4;

    private final PoolTable table = new PoolTable();
    private final UUID[] ids = new UUID[2];
    private final String[] names = {"", ""};
    private final int[] group = {-1, -1}; // 0 = lisas, 1 = rayadas
    private int phase = WAIT;
    private int turn = 0;
    private long lastAction = 0;
    private String msg = "Siéntate en la mesa para jugar.";

    public PoolSession() {
        super(BarGame.POOL);
    }

    private void reset(String why) {
        table.rack();
        ids[0] = null;
        ids[1] = null;
        names[0] = "";
        names[1] = "";
        group[0] = -1;
        group[1] = -1;
        phase = WAIT;
        turn = 0;
        msg = why;
    }

    private int seat(UUID id) {
        return id.equals(ids[0]) ? 0 : id.equals(ids[1]) ? 1 : -1;
    }

    @Override
    protected void onUse(ServerLevel level, BlockPos pos, ServerPlayer player) {
        UUID id = player.getUUID();
        if (seat(id) >= 0) {
            return;
        }
        if (phase == OVER) {
            reset("Mesa libre.");
        }
        String name = player.getName().getString();
        if (ids[0] == null) {
            ids[0] = id;
            names[0] = name;
            msg = name + " arma el triángulo. ¡Falta un rival!";
        } else if (ids[1] == null) {
            ids[1] = id;
            names[1] = name;
            phase = AIM;
            turn = 0;
            lastAction = level.getGameTime();
            msg = "Rompe " + names[0] + ".";
        }
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        int me = seat(player.getUUID());
        if (type == 9) {
            if (me >= 0 || phase == OVER) {
                reset("La mesa se reinició.");
                sync(level, pos);
            }
            return;
        }
        if (me < 0 || me != turn) {
            return;
        }
        long now = level.getGameTime();
        if (type == 1 && phase == AIM && !Float.isNaN(f)) {
            float p01 = Math.max(0.0F, Math.min(1.0F, a / 1000.0F));
            float power = 2.5F + p01 * 10.5F; // 2.5 (suave) a 13 (rotura) unidades por tick
            table.shoot(f, power);
            phase = ROLLING;
            lastAction = now;
            Msg.sound(level, pos, SoundEvents.STONE_BUTTON_CLICK_ON, 0.6F + a / 2000.0F);
            sync(level, pos);
        } else if (type == 2 && phase == PLACE) {
            float px = a / 10.0F, py = b / 10.0F;
            if (table.isFree(px, py)) {
                table.placeCue(px, py);
                phase = AIM;
                lastAction = now;
                msg = names[turn] + " tiene la blanca en mano. ¡Apunta y tira!";
                sync(level, pos);
            }
        }
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        long now = level.getGameTime();
        if (phase == ROLLING) {
            table.step();
            if (!table.moving()) {
                endShot(level, pos);
            }
            sync(level, pos);
        } else if (phase != WAIT && now - lastAction > 6000) {
            reset("Partida abandonada.");
            sync(level, pos);
        }
    }

    private static int groupOf(int ball) {
        return ball == 8 ? -2 : ball < 8 ? 0 : 1;
    }

    private int ownLeft(int g, List<Integer> pottedNow) {
        int left = 0;
        for (int i = 1; i < 16; i++) {
            if (i != 8 && groupOf(i) == g && (!table.potted[i] || pottedNow.contains(i))) {
                left++;
            }
        }
        return left;
    }

    private void endShot(ServerLevel level, BlockPos pos) {
        int me = turn, other = 1 - me;
        List<Integer> pots = new ArrayList<>(table.pottedThisShot);
        boolean foul = table.cuePotted;
        String why = foul ? "metió la blanca" : "";

        if (group[me] < 0) {
            for (int bl : pots) {
                if (bl != 8) {
                    group[me] = groupOf(bl);
                    group[other] = 1 - group[me];
                    break;
                }
            }
        }
        boolean onEight = group[me] >= 0 && ownLeft(group[me], pots) == 0;

        if (!foul) {
            int first = table.firstContact;
            if (first < 0) {
                foul = true;
                why = "no tocó ninguna bola";
            } else if (onEight) {
                if (first != 8) {
                    foul = true;
                    why = "debía tocar primero la 8";
                }
            } else if (first == 8 || (group[me] >= 0 && groupOf(first) != group[me])) {
                foul = true;
                why = "tocó primero una bola equivocada";
            }
        }

        if (pots.contains(8)) {
            boolean win = onEight && !foul;
            int winner = win ? me : other;
            phase = OVER;
            msg = win ? "¡" + names[me] + " emboca la 8 y gana la partida!"
                    : names[me] + " emboca la 8 " + (onEight ? "con falta" : "antes de tiempo") + ": gana " + names[other] + ".";
            Msg.say(level, pos, "§6§l" + names[winner] + " gana el billar.");
            return;
        }

        boolean ownPotted = false;
        for (int bl : pots) {
            if (group[me] >= 0 && groupOf(bl) == group[me]) {
                ownPotted = true;
            }
        }
        lastAction = level.getGameTime();
        if (foul) {
            if (table.cuePotted) {
                table.placeCue(PoolTable.W * 0.25F, PoolTable.H / 2);
            }
            turn = other;
            phase = PLACE;
            msg = "Falta de " + names[me] + " (" + why + "). " + names[other] + " tiene la blanca en mano: haz clic en la mesa para colocarla.";
        } else if (ownPotted) {
            phase = AIM;
            msg = names[me] + " embocó y sigue tirando.";
        } else {
            turn = other;
            phase = AIM;
            msg = "Turno de " + names[other] + ".";
        }
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        int[] p = new int[32];
        int mask = 0;
        for (int i = 0; i < 16; i++) {
            p[i * 2] = Math.round(table.x[i] * 10);
            p[i * 2 + 1] = Math.round(table.y[i] * 10);
            if (table.potted[i]) {
                mask |= 1 << i;
            }
        }
        t.putIntArray("pos", p);
        t.putInt("mask", mask);
        t.putInt("turn", turn);
        t.putInt("phase", phase);
        t.putString("n0", names[0]);
        t.putString("n1", names[1]);
        t.putString("id0", ids[0] == null ? "" : ids[0].toString());
        t.putString("id1", ids[1] == null ? "" : ids[1].toString());
        t.putInt("g0", group[0]);
        t.putInt("g1", group[1]);
        t.putString("msg", msg);
        return t;
    }
}
