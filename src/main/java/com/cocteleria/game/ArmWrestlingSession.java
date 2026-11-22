package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Gallito (pulseada) para dos jugadores. Tras la cuenta atrás, ambos pulsan CLIC DERECHO lo más rápido posible.
 * Acciones: 1 = empujar, 9 = abandonar/reiniciar.
 */
public class ArmWrestlingSession extends AbstractSession {
    private static final int WAITING = 0, COUNTDOWN = 1, PLAYING = 2;
    private static final int LIMIT = 12;

    private int phase = WAITING;
    private UUID a, b;
    private String aName = "", bName = "";
    private int position = 0;
    private int count = 3;
    private long phaseStart, lastAction, lastClickA, lastClickB;
    private String msg = "Siéntate en la mesa para desafiar a alguien.";

    public ArmWrestlingSession() {
        super(BarGame.ARM);
    }

    private void reset(String why) {
        phase = WAITING;
        a = null;
        b = null;
        aName = "";
        bName = "";
        position = 0;
        count = 3;
        msg = why;
    }

    @Override
    protected void onUse(ServerLevel level, BlockPos pos, ServerPlayer player) {
        UUID id = player.getUUID();
        if (id.equals(a) || id.equals(b)) {
            return;
        }
        String name = player.getName().getString();
        if (a == null) {
            a = id;
            aName = name;
            msg = name + " apoya el codo (lado rojo). ¡Falta un rival!";
        } else if (b == null) {
            b = id;
            bName = name;
            phase = COUNTDOWN;
            phaseStart = level.getGameTime();
            position = 0;
            count = 3;
            msg = "¡Prepárense!";
        }
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int x, int y, float f) {
        UUID id = player.getUUID();
        if (type == 9) {
            if (id.equals(a) || id.equals(b)) {
                reset("La pulseada se canceló.");
                sync(level, pos);
            }
            return;
        }
        if (type != 1 || phase != PLAYING || (!id.equals(a) && !id.equals(b))) {
            return;
        }
        long now = level.getGameTime();
        boolean isA = id.equals(a);
        if (now - (isA ? lastClickA : lastClickB) < 2) {
            return;
        }
        if (isA) {
            lastClickA = now;
            position++;
        } else {
            lastClickB = now;
            position--;
        }
        lastAction = now;
        Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.8F + Math.abs(position) * 0.04F);
        if (Math.abs(position) >= LIMIT) {
            boolean aWins = position > 0;
            String winnerName = aWins ? aName : bName;
            ServerPlayer w = playerById(level, aWins ? a : b);
            ServerPlayer l = playerById(level, aWins ? b : a);
            if (w != null) {
                w.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
            }
            if (l != null) {
                l.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0));
            }
            Msg.say(level, pos, "§6§l¡" + winnerName + " gana el gallito!");
            phase = WAITING;
            msg = "¡" + winnerName + " gana el gallito! (Fuerza para el ganador, debilidad para el perdedor)";
            a = null;
            b = null;
            position = 0;
        }
        sync(level, pos);
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        long now = level.getGameTime();
        if (phase == COUNTDOWN) {
            long t = now - phaseStart;
            if (t % 20 == 0 && t <= 60) {
                count = 3 - (int) (t / 20);
                Msg.sound(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8F);
                sync(level, pos);
            } else if (t >= 80) {
                phase = PLAYING;
                count = 0;
                lastAction = now;
                msg = "¡YA! ¡Aprieta CLIC DERECHO lo más rápido que puedas!";
                Msg.sound(level, pos, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F);
                sync(level, pos);
            }
        } else if (phase == PLAYING && now - lastAction > 200) {
            reset("Se cansaron de empujar: sin ganador.");
            sync(level, pos);
        }
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        t.putString("a", aName);
        t.putString("b", bName);
        t.putString("aid", a == null ? "" : a.toString());
        t.putString("bid", b == null ? "" : b.toString());
        t.putInt("phase", phase);
        t.putInt("count", count);
        t.putInt("pos", position);
        t.putInt("limit", LIMIT);
        t.putString("msg", msg);
        return t;
    }
}
