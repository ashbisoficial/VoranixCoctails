package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Partida en curso de una mesa de juego (vive en el BlockEntity). */
public interface GameSession {
    /** Un jugador hace clic derecho en la mesa (solo se llama en el servidor). */
    void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit);

    /** Se llama cada tick en el servidor (cuentas atrás, tiempos de espera...). */
    default void tick(ServerLevel level, BlockPos pos, BlockState state) {
    }
}
