package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

/** Ajedrez para dos jugadores (reglas completas). Acciones: 1 = clic en casilla (a), 9 = reiniciar. */
public class ChessSession extends AbstractSession {
    private ChessEngine engine = new ChessEngine();
    private UUID white, black;
    private String whiteName = "", blackName = "";
    private int selected = -1, lastFrom = -1, lastTo = -1;
    private boolean over = false;
    private String msg = "Haz clic para sentarte: el primero juega con blancas.";

    public ChessSession() {
        super(BarGame.CHESS);
    }

    private void reset() {
        engine = new ChessEngine();
        white = null;
        black = null;
        whiteName = "";
        blackName = "";
        selected = -1;
        lastFrom = -1;
        lastTo = -1;
        over = false;
        msg = "Mesa libre: abre la mesa para sentarte.";
    }

    @Override
    protected void onUse(ServerLevel level, BlockPos pos, ServerPlayer player) {
        UUID id = player.getUUID();
        if (over && (id.equals(white) || id.equals(black))) {
            return; // ve el resultado
        }
        if (id.equals(white) || id.equals(black)) {
            return;
        }
        if (over) {
            reset();
        }
        String name = player.getName().getString();
        if (white == null) {
            white = id;
            whiteName = name;
            msg = name + " juega con las blancas. Esperando rival...";
        } else if (black == null) {
            black = id;
            blackName = name;
            msg = "¡Comienza la partida! Mueven las blancas.";
        }
    }

    @Override
    public void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                       int type, int a, int b, float f) {
        UUID id = player.getUUID();
        if (type == 9) {
            if (id.equals(white) || id.equals(black) || over) {
                reset();
                sync(level, pos);
            }
            return;
        }
        if (type != 1 || over || white == null || black == null || a < 0 || a > 63) {
            return;
        }
        boolean isWhite = id.equals(white);
        if (!isWhite && !id.equals(black)) {
            return;
        }
        if (isWhite != engine.whiteToMove) {
            msg = "No es tu turno.";
            sync(level, pos);
            return;
        }
        int piece = engine.board[a];
        if (piece != 0 && (piece > 0) == isWhite) {
            selected = a;
            Msg.sound(level, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.3F);
            sync(level, pos);
            return;
        }
        if (selected < 0) {
            return;
        }
        int from = selected;
        if (!engine.play(from, a)) {
            msg = "Movimiento ilegal.";
            sync(level, pos);
            return;
        }
        selected = -1;
        lastFrom = from;
        lastTo = a;
        Msg.sound(level, pos, SoundEvents.WOOD_PLACE, 1.0F);
        String mover = isWhite ? whiteName : blackName;
        String next = engine.whiteToMove ? "blancas" : "negras";
        switch (engine.status()) {
            case CHECKMATE -> {
                over = true;
                msg = "¡JAQUE MATE! Gana " + mover + ".";
                Msg.say(level, pos, "§6§l" + mover + " gana al ajedrez por jaque mate.");
            }
            case STALEMATE -> {
                over = true;
                msg = "Tablas por rey ahogado.";
            }
            case DRAW -> {
                over = true;
                msg = "Tablas.";
            }
            case CHECK -> msg = "¡Jaque! Mueven las " + next + ".";
            default -> msg = "Mueven las " + next + ".";
        }
        sync(level, pos);
    }

    @Override
    protected CompoundTag view(ServerLevel level) {
        CompoundTag t = new CompoundTag();
        t.putIntArray("board", engine.board.clone());
        t.putString("white", whiteName);
        t.putString("black", blackName);
        t.putString("wid", white == null ? "" : white.toString());
        t.putString("bid", black == null ? "" : black.toString());
        t.putBoolean("turnWhite", engine.whiteToMove);
        t.putInt("sel", selected);
        List<Integer> targets = selected >= 0 ? engine.targetsFrom(selected) : List.of();
        t.putIntArray("targets", targets.stream().mapToInt(Integer::intValue).toArray());
        t.putInt("lastFrom", lastFrom);
        t.putInt("lastTo", lastTo);
        t.putBoolean("over", over);
        t.putString("msg", msg);
        return t;
    }
}
