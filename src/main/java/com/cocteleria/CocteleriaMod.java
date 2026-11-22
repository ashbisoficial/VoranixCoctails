package com.cocteleria;

import com.cocteleria.registry.ModBlockEntities;
import com.cocteleria.registry.ModBlocks;
import com.cocteleria.registry.ModEffects;
import com.cocteleria.registry.ModItems;
import com.cocteleria.registry.ModTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CocteleriaMod.MODID)
public class CocteleriaMod {
    public static final String MODID = "cocteleria";

    public CocteleriaMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEffects.EFFECTS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.TYPES.register(bus);
        ModItems.ITEMS.register(bus);
        ModTabs.TABS.register(bus);
    }
}
