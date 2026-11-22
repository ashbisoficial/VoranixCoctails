package com.cocteleria.block;

import com.cocteleria.game.BarGame;
import com.cocteleria.game.GameBlockEntity;
import com.cocteleria.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Bloque decorativo con una partida de juego asociada (clic derecho para abrir la mesa). */
public class GameDecorBlock extends DecorBlock implements EntityBlock {
    private final BarGame game;

    public GameDecorBlock(Properties properties, double[][] boxes, boolean wall, int[][] cells, BarGame game) {
        super(properties, boxes, wall, cells);
        this.game = game;
    }

    public BarGame game() {
        return game;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == 0 ? new GameBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.GAME.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> GameBlockEntity.serverTick(lvl, pos, st, (GameBlockEntity) be);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockPos origin = originOf(state, pos);
        BlockState originState = level.getBlockState(origin);
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(origin) instanceof GameBlockEntity entity) {
            entity.session(game).use(serverLevel, origin, originState, player, hit);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
