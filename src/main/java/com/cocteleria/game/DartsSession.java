package com.cocteleria.game;

import com.cocteleria.block.DecorBlock;
import com.cocteleria.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dardos 301 por turnos (hasta 8 jugadores, 3 dardos por turno). Los dardos se lanzan en el mundo desde donde estés
 * (mantén clic derecho con un dardo en la mano para cargar y suelta para lanzar). Cada jugador tiene solo 6 dardos:
 * hay que recogerlos de la diana. Acciones de la pantalla: 1 = unirse, 2 = recoger mis dardos, 9 = reiniciar.
 */
public class DartsSession extends AbstractSession {
    public static final int MAX_DARTS = 6;
    private static final int START = 301;
    private static final int[] ORDER = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};

    private record Throw(int points, String label, int hx, int hy) {
    }

    private final List<UUID> order = new ArrayList<>();
    private final Map<UUID, Integer> left = new HashMap<>();
    private final Map<UUID, Integer> out = new HashMap<>(); // dardos clavados en la diana por jugador
    private final Map<UUID, String> names = new HashMap<>();
    private final List<int[]> hits = new ArrayList<>(); // {x, y, índice de jugador}
    private int turn = 0, thrown = 0, turnStart = START;
    private long lastAction = 0;
    private String msg = "Pulsa «Unirme» para jugar. Sostén un dardo y mantén clic derecho para cargar el tiro.";

    public DartsSession() {
        super(BarGame.DARTS);
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        UUID id = player.getUUID();
        long now = level.getGameTime();
        if (type == 9) {
            order.clear();
            left.clear();
            out.clear();
            names.clear();
            hits.clear();
            turn = 0;
            thrown = 0;
            turnStart = START;
            msg = player.getName().getString() + " reinicia la partida.";
        } else if (type == 1) {
            if (left.containsKey(id)) {
                giveDarts(player, id);
            } else if (order.size() >= 8) {
                msg = "La diana está llena (máx. 8 jugadores).";
            } else {
                order.add(id);
                left.put(id, START);
                out.put(id, 0);
                names.put(id, player.getName().getString());
                giveDarts(player, id);
                msg = player.getName().getString() + " se une. ¡Hay que llegar a 0 exacto!";
                if (order.size() == 1) {
                    turn = 0;
                    thrown = 0;
                    turnStart = START;
                    lastAction = now;
                }
            }
        } else if (type == 2) {
            if (left.containsKey(id)) {
                int n = out.getOrDefault(id, 0);
                out.put(id, 0);
                removeHitsOf(id);
                give(player, n);
                msg = player.getName().getString() + " recoge " + n + " dardo(s).";
            }
        }
        sync(level, pos);
    }

    private void removeHitsOf(UUID id) {
        int idx = order.indexOf(id);
        hits.removeIf(h -> h[2] == idx);
    }

    /** Completa el set de 6 dardos del jugador (lo que tiene encima + los clavados no pueden pasar de 6). */
    private void giveDarts(ServerPlayer player, UUID id) {
        int have = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ModItems.DART.get())) {
                have += s.getCount();
            }
        }
        int missing = MAX_DARTS - have - out.getOrDefault(id, 0);
        if (missing > 0) {
            give(player, missing);
        }
    }

    private void give(ServerPlayer player, int n) {
        if (n <= 0) {
            return;
        }
        ItemStack stack = new ItemStack(ModItems.DART.get(), n);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** Un dardo lanzado por {@code shooter} se clavó en la diana en {@code hit}. */
    public void dartHit(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer shooter, Vec3 hit) {
        UUID id = shooter.getUUID();
        long now = level.getGameTime();
        if (!left.containsKey(id)) {
            msg = shooter.getName().getString() + " tiró sin unirse a la partida.";
            shooter.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§cAbre la diana (clic derecho) y pulsa «Unirme» para puntuar."), true);
            sync(level, pos);
            return;
        }
        out.merge(id, 1, Integer::sum);
        Throw t = score(state.getValue(DecorBlock.FACING), hit, pos);
        Msg.sound(level, pos, SoundEvents.ARROW_HIT, 0.9F + level.getRandom().nextFloat() * 0.3F);
        hits.add(new int[]{t.hx(), t.hy(), order.indexOf(id)});
        while (hits.size() > 18) {
            hits.remove(0);
        }

        if (order.size() > 1 && !order.get(turn).equals(id) && now - lastAction > 1200) {
            nextTurn(now);
        }
        if (!order.get(turn).equals(id)) {
            msg = "No era el turno de " + shooter.getName().getString() + " (juega " + names.get(order.get(turn)) + "): el dardo no cuenta.";
            sync(level, pos);
            return;
        }
        String name = names.get(id);
        lastAction = now;
        thrown++;
        int remaining = left.get(id);
        if (t.points() > remaining) {
            left.put(id, turnStart);
            msg = name + " lanza " + t.label() + " (" + t.points() + "): ¡se pasa! Vuelve a " + turnStart + ".";
            nextTurn(now);
        } else {
            remaining -= t.points();
            left.put(id, remaining);
            if (remaining == 0) {
                msg = "¡" + name + " gana la partida de dardos!";
                Msg.say(level, pos, "§6§l" + name + " gana los dardos 301.");
                for (UUID u : order) {
                    left.put(u, START);
                }
                turn = 0;
                thrown = 0;
                turnStart = START;
            } else {
                msg = name + " lanza " + t.label() + " (" + t.points() + "): le quedan " + remaining + ".";
                if (thrown >= 3) {
                    nextTurn(now);
                }
            }
        }
        sync(level, pos);
    }

    private void nextTurn(long now) {
        turn = (turn + 1) % order.size();
        thrown = 0;
        lastAction = now;
        turnStart = left.get(order.get(turn));
    }

    private static Throw score(Direction facing, Vec3 hit, BlockPos pos) {
        double lx = hit.x - pos.getX() - 0.5;
        double lz = hit.z - pos.getZ() - 0.5;
        Direction right = facing.getCounterClockWise();
        double u = lx * right.getStepX() + lz * right.getStepZ();
        double v = hit.y - pos.getY() - 0.5;
        double r = Math.sqrt(u * u + v * v) / 0.4375; // 1.0 = borde del tablero
        int hx = (int) Math.max(0, Math.min(1000, (u / 0.4375 + 1) * 500));
        int hy = (int) Math.max(0, Math.min(1000, (1 - v / 0.4375) * 500));

        if (r > 0.9) {
            return new Throw(0, "fuera de la diana", hx, hy);
        }
        if (r < 0.1125) {
            return new Throw(50, "¡DIANA!", hx, hy);
        }
        if (r < 0.225) {
            return new Throw(25, "bull exterior", hx, hy);
        }
        double theta = Math.toDegrees(Math.atan2(u, v));
        if (theta < 0) {
            theta += 360;
        }
        int number = ORDER[(int) ((theta + 9) / 18) % 20];
        if (r > 0.775) {
            return new Throw(number * 2, "doble " + number, hx, hy);
        }
        if (r > 0.45 && r <= 0.55) {
            return new Throw(number * 3, "triple " + number, hx, hy);
        }
        return new Throw(number, "" + number, hx, hy);
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        ListTag players = new ListTag();
        for (UUID id : order) {
            CompoundTag p = new CompoundTag();
            p.putString("name", names.get(id));
            p.putString("id", id.toString());
            p.putInt("left", left.get(id));
            p.putInt("out", out.getOrDefault(id, 0));
            players.add(p);
        }
        t.put("players", players);
        t.putInt("turn", turn);
        t.putInt("thrown", thrown);
        int[] flat = new int[hits.size() * 3];
        for (int i = 0; i < hits.size(); i++) {
            flat[i * 3] = hits.get(i)[0];
            flat[i * 3 + 1] = hits.get(i)[1];
            flat[i * 3 + 2] = hits.get(i)[2];
        }
        t.putIntArray("hits", flat);
        t.putString("msg", msg);
        return t;
    }
}
