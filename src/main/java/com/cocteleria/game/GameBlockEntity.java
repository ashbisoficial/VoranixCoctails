package com.cocteleria.game;

import com.cocteleria.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Guarda la partida en curso de una mesa de juego (solo en memoria: se reinicia al recargar el mundo). */
public class GameBlockEntity extends BlockEntity {
    private GameSession session;

    public GameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAME.get(), pos, state);
    }

    public GameSession session(BarGame game) {
        if (session == null) {
            session = game.create();
        }
        return session;
    }

    /** Sesión existente o null (no la crea). */
    public GameSession existingSession() {
        return session;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GameBlockEntity entity) {
        if (entity.session != null && level instanceof ServerLevel serverLevel) {
            entity.session.tick(serverLevel, pos, state);
        }
    }
}
