package com.cocteleria.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Partida en curso de una mesa de juego (vive en el BlockEntity del bloque origen). */
public interface GameSession {
    /** Un jugador hace clic derecho en la mesa (servidor): normalmente abre la pantalla del juego. */
    void use(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockHitResult hit);

    /** Acción enviada desde la pantalla del juego (tipo > 0). */
    default void action(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
                        int type, int a, int b, float f) {
    }

    /** El jugador cerró la pantalla de la mesa. */
    default void closed(ServerPlayer player) {
    }

    /** Se llama cada tick en el servidor (cuentas atrás, física, tiempos de espera...). */
    default void tick(ServerLevel level, BlockPos pos, BlockState state) {
    }
}
