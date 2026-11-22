package com.cocteleria.effect;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * "Embriagado": cada segundo hay una probabilidad de que la víctima tropiece
 * y reciba un empujón lateral aleatorio. Más nivel = más tropiezos y más fuertes.
 */
public class TipsyEffect extends MobEffect {
    public TipsyEffect() {
        super(MobEffectCategory.HARMFUL, 0xD94FA8);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }
        RandomSource random = entity.getRandom();
        if (random.nextFloat() < 0.35F + 0.15F * amplifier) {
            double strength = 0.25D + 0.10D * amplifier;
            entity.push((random.nextDouble() - 0.5D) * 2.0D * strength,
                    0.0D,
                    (random.nextDouble() - 0.5D) * 2.0D * strength);
            entity.hurtMarked = true; // sincroniza el empujón con el cliente del jugador
        }
    }
}
