package com.cocteleria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bloque decorativo pequeño con orientación horizontal y forma rotada según el frente. */
public class DecorBlock extends HorizontalDirectionalBlock {
    private final VoxelShape[] shapes = new VoxelShape[4]; // norte, este, sur, oeste

    private final boolean wall;

    public DecorBlock(Properties properties, double[][] boxes, boolean wall) {
        super(properties);
        this.wall = wall;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

        VoxelShape base = Shapes.empty();
        for (double[] b : boxes) {
            base = Shapes.or(base, box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        VoxelShape current = base;
        for (int i = 0; i < 4; i++) {
            shapes[i] = current;
            current = rotateClockwise(current);
        }
    }

    /** Gira la forma 90° en sentido horario visto desde arriba: (x, z) -> (1 - z, x). */
    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                result[0] = Shapes.or(result[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
        return result[0];
    }

    private static int index(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (wall) {
            Direction face = context.getClickedFace();
            if (face.getAxis().isVertical()) {
                return null; // los bloques de pared solo se colocan en paredes
            }
            return this.defaultBlockState().setValue(FACING, face);
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[index(state.getValue(FACING))];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (wall) {
            Direction facing = state.getValue(FACING);
            BlockPos support = pos.relative(facing.getOpposite());
            return level.getBlockState(support).isFaceSturdy(level, support, facing);
        }
        return !level.getBlockState(pos.below()).isAir();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
}
