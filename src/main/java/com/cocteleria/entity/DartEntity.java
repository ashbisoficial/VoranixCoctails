package com.cocteleria.entity;

import com.cocteleria.block.DecorBlock;
import com.cocteleria.block.GameDecorBlock;
import com.cocteleria.game.BarGame;
import com.cocteleria.game.DartsSession;
import com.cocteleria.game.GameBlockEntity;
import com.cocteleria.registry.ModEntities;
import com.cocteleria.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Dardo lanzado: cae por gravedad, puntúa si se clava en el frente de una diana y si no se queda en el suelo. */
public class DartEntity extends ThrowableItemProjectile {
    public DartEntity(EntityType<? extends DartEntity> type, Level level) {
        super(type, level);
    }

    public DartEntity(Level level, LivingEntity owner) {
        super(ModEntities.DART.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DART.get();
    }

    @Override
    protected float getGravity() {
        return 0.04F; // caída más marcada que una bola de nieve (0.03)
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (level().isClientSide) {
            return;
        }
        BlockPos bp = hit.getBlockPos();
        BlockState state = level().getBlockState(bp);
        if (state.getBlock() instanceof GameDecorBlock board && board.game() == BarGame.DARTS
                && hit.getDirection() == state.getValue(DecorBlock.FACING)
                && getOwner() instanceof ServerPlayer shooter
                && level().getBlockEntity(bp) instanceof GameBlockEntity entity
                && entity.session(BarGame.DARTS) instanceof DartsSession session) {
            session.dartHit((ServerLevel) level(), bp, state, shooter, hit.getLocation());
            discard();
            return;
        }
        dropAt(hit.getLocation().add(hit.getDirection().getStepX() * 0.15, hit.getDirection().getStepY() * 0.15,
                hit.getDirection().getStepZ() * 0.15));
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) {
            return;
        }
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), 1.0F);
        dropAt(position());
        discard();
    }

    private void dropAt(Vec3 at) {
        if (getOwner() instanceof ServerPlayer p && p.getAbilities().instabuild) {
            return;
        }
        level().addFreshEntity(new ItemEntity(level(), at.x, at.y, at.z, new ItemStack(ModItems.DART.get())));
    }
}
