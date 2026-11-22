package com.cocteleria.game;

import com.cocteleria.block.DecorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Ajedrez para dos jugadores con todas las reglas. El primero en hacer clic juega con blancas y el segundo con negras.
 * Las blancas están del lado "frontal" del tablero (el lado que mira hacia donde lo colocaste).
 * Clic en una pieza tuya para elegirla y clic en la casilla destino para moverla; el tablero se muestra en el chat.
 */
public class ChessSession implements GameSession {
    private static final String[] GLYPH = {"", "♟", "♞", "♝", "♜", "♛", "♚"};

    private ChessEngine engine = new ChessEngine();
    private UUID white, black;
    private String whiteName = "", blackName = "";
    private int selected = -1;

    private void reset() {
        engine = new ChessEngine();
        white = null;
        black = null;
        selected = -1;
    }

    @Override
    public void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        UUID id = player.getUUID();
        String name = Msg.name(player);

        if (Msg.isSpecial(player)) {
            reset();
            Msg.say(level, pos, "§e" + name + " reinicia la partida de ajedrez.");
            return;
        }

        if (!id.equals(white) && !id.equals(black)) {
            if (white == null) {
                white = id;
                whiteName = name;
                Msg.say(level, pos, "§f" + name + " juega con las BLANCAS §7(lado frontal del tablero). Esperando rival: otro jugador debe hacer clic.");
            } else if (black == null) {
                black = id;
                blackName = name;
                Msg.say(level, pos, "§6" + name + " juega con las NEGRAS. ¡Comienza la partida!");
                sendBoard(level, pos, "Mueven blancas");
            } else {
                Msg.bar(player, "§cMesa ocupada: " + whiteName + " vs " + blackName + " (Shift + clic para reiniciar)");
            }
            return;
        }
        if (black == null) {
            Msg.bar(player, "§eEsperando a un rival...");
            return;
        }

        boolean isWhite = id.equals(white);
        if (isWhite != engine.whiteToMove) {
            Msg.bar(player, "§cNo es tu turno.");
            return;
        }

        int sq = squareFromHit(state.getValue(DecorBlock.FACING), hit.getLocation(), pos);
        if (sq < 0) {
            Msg.bar(player, "§cHaz clic dentro del tablero.");
            return;
        }

        int piece = engine.board[sq];
        if (piece != 0 && (piece > 0) == isWhite) {
            selected = sq;
            List<Integer> targets = engine.targetsFrom(sq);
            StringBuilder sb = new StringBuilder("§aElegiste " + ChessEngine.squareName(sq) + " §7→ ");
            if (targets.isEmpty()) {
                sb.append("sin movimientos");
            }
            for (int t : targets) {
                sb.append(ChessEngine.squareName(t)).append(' ');
            }
            Msg.bar(player, sb.toString());
            Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.2F);
            return;
        }
        if (selected < 0) {
            Msg.bar(player, "§eToca primero una de tus piezas.");
            return;
        }

        int from = selected;
        if (!engine.play(from, sq)) {
            Msg.bar(player, "§cMovimiento ilegal.");
            return;
        }
        selected = -1;
        Msg.sound(level, pos, SoundEvents.WOOD_PLACE, 1.0F);

        String move = ChessEngine.squareName(from) + " → " + ChessEngine.squareName(sq);
        ChessEngine.Status status = engine.status();
        String side = engine.whiteToMove ? "blancas" : "negras";
        switch (status) {
            case CHECKMATE -> {
                sendBoard(level, pos, name + " juega " + move);
                String winner = engine.whiteToMove ? blackName : whiteName;
                Msg.say(level, pos, "§6§l¡JAQUE MATE! Gana " + winner + ".");
                reset();
            }
            case STALEMATE -> {
                sendBoard(level, pos, name + " juega " + move);
                Msg.say(level, pos, "§eTablas por rey ahogado.");
                reset();
            }
            case DRAW -> {
                sendBoard(level, pos, name + " juega " + move);
                Msg.say(level, pos, "§eTablas.");
                reset();
            }
            case CHECK -> sendBoard(level, pos, name + " juega " + move + " §c¡Jaque! §7Mueven " + side);
            default -> sendBoard(level, pos, name + " juega " + move + ". §7Mueven " + side);
        }
    }

    /** Casilla (0-63) bajo el clic, o -1 si cae fuera del tablero. Blancas = lado frontal. */
    private static int squareFromHit(Direction facing, Vec3 hit, BlockPos pos) {
        double lx = hit.x - pos.getX() - 0.5;
        double lz = hit.z - pos.getZ() - 0.5;
        Direction right = facing.getCounterClockWise();
        double u = lx * right.getStepX() + lz * right.getStepZ();
        double f = lx * facing.getStepX() + lz * facing.getStepZ();
        double cell = 13.0 / 16.0 / 8.0;
        double margin = 1.5 / 16.0;
        int file = (int) Math.floor((u + 0.5 - margin) / cell);
        int rank = (int) Math.floor((0.5 - f - margin) / cell);
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            return -1;
        }
        return rank * 8 + file;
    }

    private void sendBoard(ServerLevel level, BlockPos pos, String header) {
        Msg.broadcast(level, pos, Component.literal(Msg.PREFIX + "§e♞ " + header));
        for (int rank = 7; rank >= 0; rank--) {
            StringBuilder sb = new StringBuilder("§7" + (rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int p = engine.board[rank * 8 + file];
                if (p == 0) {
                    sb.append("§8· ");
                } else {
                    sb.append(p > 0 ? "§f" : "§6").append(GLYPH[Math.abs(p)]).append(' ');
                }
            }
            Msg.broadcast(level, pos, Component.literal(sb.toString()));
        }
        Msg.broadcast(level, pos, Component.literal("§7  a b c d e f g h   §f■ blancas  §6■ negras"));
    }
}
