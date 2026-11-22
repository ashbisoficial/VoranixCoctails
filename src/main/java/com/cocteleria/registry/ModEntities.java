package com.cocteleria.registry;

import com.cocteleria.CocteleriaMod;
import com.cocteleria.entity.DartEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CocteleriaMod.MODID);

    public static final RegistryObject<EntityType<DartEntity>> DART = ENTITIES.register("dart",
            () -> EntityType.Builder.<DartEntity>of(DartEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build("dart"));
}
