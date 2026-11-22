package com.cocteleria.game;

import com.cocteleria.block.DecorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dardos 301 por turnos (hasta 8 jugadores, 3 dardos por turno). El dardo cae donde haces clic en la diana:
 * hay que llegar a 0 exacto; si te pasas, pierdes el turno y vuelves al puntaje con el que empezaste.
 */
public class DartsSession implements GameSession {
    private static final int START = 301;
    private static final int[] ORDER = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};

    private record Throw(int points, String label) {
    }

    private final List<UUID> order = new ArrayList<>();
    private final Map<UUID, Integer> left = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private int turn = 0;
    private int thrown = 0;
    private int turnStart = START;
    private long lastAction = 0;

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        long now = level.getGameTime();
        String name = Msg.name(player);
        UUID id = player.getUUID();

        if (Msg.isSpecial(player)) {
            order.clear();
            left.clear();
            names.clear();
            turn = 0;
            thrown = 0;
            Msg.say(level, pos, "§e" + name + " reinicia la partida de dardos.");
            return;
        }

        if (!left.containsKey(id)) {
            if (order.size() >= 8) {
                Msg.bar(player, "§cLa diana está llena (máx. 8 jugadores).");
                return;
            }
            order.add(id);
            left.put(id, START);
            names.put(id, name);
            Msg.say(level, pos, "§a" + name + " se une a los dardos §7(" + START + " puntos, hay que llegar a 0 exacto)");
            if (order.size() == 1) {
                turn = 0;
                thrown = 0;
                turnStart = START;
                lastAction = now;
            }
        }

        if (order.size() > 1 && !order.get(turn).equals(id) && now - lastAction > 1200) {
            Msg.say(level, pos, "§7" + names.get(order.get(turn)) + " tarda demasiado: se salta su turno.");
            nextTurn(level, pos, now);
        }
        if (!order.get(turn).equals(id)) {
            Msg.bar(player, "§cNo es tu turno: juega " + names.get(order.get(turn)));
            return;
        }

        Throw t = score(state.getValue(DecorBlock.FACING), hit.getLocation(), pos, level.getRandom());
        int remaining = left.get(id);
        lastAction = now;
        thrown++;
        Msg.sound(level, pos, SoundEvents.ARROW_HIT, 0.9F + level.getRandom().nextFloat() * 0.3F);

        if (t.points() > remaining) {
            left.put(id, turnStart);
            Msg.say(level, pos, "§c" + name + " lanza " + t.label() + " (" + t.points() + "): ¡se pasa! Vuelve a " + turnStart + ".");
            nextTurn(level, pos, now);
            return;
        }
        remaining -= t.points();
        left.put(id, remaining);
        if (remaining == 0) {
            Msg.say(level, pos, "§6§l¡" + name + " gana la partida de dardos! §r§7(Shift + clic para reiniciar)");
            for (UUID u : order) {
                left.put(u, START);
            }
            turn = 0;
            thrown = 0;
            turnStart = START;
            return;
        }
        Msg.say(level, pos, "§f" + name + " §7lanza §e" + t.label() + " §7(" + t.points() + ") → le quedan §b" + remaining);
        if (thrown >= 3) {
            nextTurn(level, pos, now);
        }
    }

    private void nextTurn(ServerLevel level, BlockPos pos, long now) {
        turn = (turn + 1) % order.size();
        thrown = 0;
        lastAction = now;
        UUID next = order.get(turn);
        turnStart = left.get(next);
        if (order.size() > 1) {
            Msg.say(level, pos, "§eTurno de " + names.get(next) + " §7(le quedan " + turnStart + ")");
        }
    }

    private static Throw score(Direction facing, Vec3 hit, BlockPos pos, RandomSource random) {
        double lx = hit.x - pos.getX() - 0.5;
        double lz = hit.z - pos.getZ() - 0.5;
        Direction right = facing.getCounterClockWise();
        double u = lx * right.getStepX() + lz * right.getStepZ();
        double v = hit.y - pos.getY() - 0.5;
        u += (random.nextDouble() - 0.5) * 0.03; // pulso: un poco de dispersión
        v += (random.nextDouble() - 0.5) * 0.03;
        double r = Math.sqrt(u * u + v * v) / 0.4375; // 1.0 = borde del tablero

        if (r > 0.9) {
            return new Throw(0, "fuera de la diana");
        }
        if (r < 0.1125) {
            return new Throw(50, "¡DIANA!");
        }
        if (r < 0.225) {
            return new Throw(25, "bull exterior");
        }
        double theta = Math.toDegrees(Math.atan2(u, v));
        if (theta < 0) {
            theta += 360;
        }
        int number = ORDER[(int) ((theta + 9) / 18) % 20];
        if (r > 0.775) {
            return new Throw(number * 2, "doble " + number);
        }
        if (r > 0.45 && r <= 0.55) {
            return new Throw(number * 3, "triple " + number);
        }
        return new Throw(number, "" + number);
    }
}
