package com.cocteleria.item;

import com.cocteleria.entity.DartEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Dardo: mantén clic derecho para cargar la fuerza y suelta para lanzar. Más fuerza = más rápido, menos caída y
 * menos error de puntería; correr o moverte aumenta el error. Se lleva un set de 6 (máximo del stack).
 */
public class DartItem extends Item {
    public DartItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int charge = getUseDuration(stack) - timeLeft;
        if (charge < 3) {
            return;
        }
        float power = Math.min(charge / 16.0F, 1.0F);
        if (!level.isClientSide) {
            DartEntity dart = new DartEntity(level, player);
            float error = 6.0F - 4.0F * power;                       // ángulo de error: baja con la fuerza
            if (player.isSprinting()) {
                error += 4.0F;
            } else if (player.getDeltaMovement().horizontalDistanceSqr() > 0.0036) {
                error += 1.5F;
            }
            dart.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.7F + 0.8F * power, error);
            level.addFreshEntity(dart);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS, 0.5F, 1.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
