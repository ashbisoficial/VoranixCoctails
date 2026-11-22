package com.cocteleria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bloque decorativo con orientación horizontal. Puede ser de pared (diana) o multibloque (mesas grandes):
 * en ese caso el bloque origen (PART = 0) dibuja todo el modelo y los demás bloques solo aportan colisión.
 */
public class DecorBlock extends HorizontalDirectionalBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 5);

    private final boolean wall;
    private final int[][] cells; // null = bloque simple; si no, desplazamientos (dx, dz) con el origen en el índice 0
    private final VoxelShape[][] shapes; // [parte][norte, este, sur, oeste]

    public DecorBlock(Properties properties, double[][] boxes, boolean wall, int[][] cells) {
        super(properties);
        this.wall = wall;
        this.cells = cells;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0));

        int parts = cells == null ? 1 : cells.length;
        shapes = new VoxelShape[parts][4];
        for (int p = 0; p < parts; p++) {
            int dx = cells == null ? 0 : cells[p][0];
            int dz = cells == null ? 0 : cells[p][1];
            VoxelShape base = Shapes.empty();
            for (double[] b : boxes) {
                double x1 = Math.max(0, b[0] - 16 * dx), x2 = Math.min(16, b[3] - 16 * dx);
                double z1 = Math.max(0, b[2] - 16 * dz), z2 = Math.min(16, b[5] - 16 * dz);
                if (x2 - x1 > 1.0E-6 && z2 - z1 > 1.0E-6) {
                    base = Shapes.or(base, box(x1, b[1], z1, x2, b[4], z2));
                }
            }
            VoxelShape current = base;
            for (int i = 0; i < 4; i++) {
                shapes[p][i] = current;
                current = rotateClockwise(current);
            }
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

    /** Desplazamiento (dx, dz) de una celda del modelo según hacia dónde mira el bloque. */
    protected static int[] rotateOffset(int dx, int dz, Direction facing) {
        return switch (facing) {
            case EAST -> new int[]{-dz, dx};
            case SOUTH -> new int[]{-dx, -dz};
            case WEST -> new int[]{dz, -dx};
            default -> new int[]{dx, dz};
        };
    }

    protected boolean isMulti() {
        return cells != null;
    }

    /** Posición del bloque origen al que pertenece el bloque en {@code pos}. */
    public BlockPos originOf(BlockState state, BlockPos pos) {
        if (cells == null) {
            return pos;
        }
        int part = state.getValue(PART);
        if (part == 0) {
            return pos;
        }
        int[] r = rotateOffset(cells[part][0], cells[part][1], state.getValue(FACING));
        return pos.offset(-r[0], 0, -r[1]);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (wall) {
            Direction face = context.getClickedFace();
            if (face.getAxis().isVertical()) {
                return null; // los bloques de pared solo se colocan en paredes
            }
            return this.defaultBlockState().setValue(FACING, face);
        }
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (cells != null) {
            BlockPos origin = context.getClickedPos();
            for (int i = 1; i < cells.length; i++) {
                int[] r = rotateOffset(cells[i][0], cells[i][1], facing);
                BlockPos p = origin.offset(r[0], 0, r[1]);
                if (!context.getLevel().getBlockState(p).canBeReplaced(context)
                        || context.getLevel().getBlockState(p.below()).isAir()) {
                    return null; // no hay espacio para la mesa completa
                }
            }
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (cells != null && !level.isClientSide) {
            Direction facing = state.getValue(FACING);
            for (int i = 1; i < cells.length; i++) {
                int[] r = rotateOffset(cells[i][0], cells[i][1], facing);
                level.setBlock(pos.offset(r[0], 0, r[1]), state.setValue(PART, i), 3);
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (cells != null && !state.is(newState.getBlock())) {
            BlockPos origin = originOf(state, pos);
            Direction facing = state.getValue(FACING);
            for (int[] c : cells) {
                int[] r = rotateOffset(c[0], c[1], facing);
                BlockPos p = origin.offset(r[0], 0, r[1]);
                if (!p.equals(pos) && level.getBlockState(p).is(this)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (cells != null && !level.isClientSide && !player.isCreative()) {
            popResource(level, originOf(state, pos), new ItemStack(this));
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == 0 ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[Math.min(state.getValue(PART), shapes.length - 1)][index(state.getValue(FACING))];
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
