package com.cocteleria.registry;

import com.cocteleria.CocteleriaMod;
import com.cocteleria.game.GameBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CocteleriaMod.MODID);

    public static final RegistryObject<BlockEntityType<GameBlockEntity>> GAME = TYPES.register("game",
            () -> BlockEntityType.Builder.of(GameBlockEntity::new,
                    ModBlocks.GAME_BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
}
