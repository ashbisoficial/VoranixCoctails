package com.cocteleria.registry;

import com.cocteleria.block.FoodBlock;
import com.cocteleria.block.PlateBlock;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

/**
 * Comida. Con plato (se coloca como bloque y se come por mordiscos como la torta): hamburguesa, papas fritas, completo y
 * salchipapas. En la mano (se comen como cualquier comida): empanada, sopaipilla y sopaipilla con pebre.
 */
public class ModFoods {
    /** Llamar desde el constructor del mod para que los registros existan antes del evento de registro. */
    public static void init() {
    }

    private static BlockBehaviour.Properties props(SoundType sound) {
        return BlockBehaviour.Properties.of().strength(0.4F).sound(sound).noOcclusion();
    }

    public static final RegistryObject<Block> PLATE = ModBlocks.BLOCKS.register("plate", () -> new PlateBlock(props(SoundType.STONE)));
    static {
        ModItems.ITEMS.register("plate", () -> new BlockItem(PLATE.get(), new Item.Properties()));
    }

    public static final RegistryObject<Block> HAMBURGER = plated("hamburger", 4, 2, 0.3F, 10.6);
    public static final RegistryObject<Block> FRENCH_FRIES = plated("french_fries", 3, 2, 0.3F, 8.3);
    public static final RegistryObject<Block> COMPLETO = plated("completo", 3, 3, 0.3F, 6.6);
    public static final RegistryObject<Block> SALCHIPAPAS = plated("salchipapas", 5, 2, 0.3F, 6.0);

    public static final RegistryObject<Item> EMPANADA = handheld("empanada", 6, 0.6F);
    public static final RegistryObject<Item> SOPAIPILLA = handheld("sopaipilla", 4, 0.4F);
    public static final RegistryObject<Item> SOPAIPILLA_PEBRE = handheld("sopaipilla_pebre", 5, 0.5F);

    private static RegistryObject<Block> plated(String name, int bites, int nutrition, float saturation, double height) {
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name,
                () -> new FoodBlock(props(SoundType.WOOL), bites, nutrition, saturation, height));
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static RegistryObject<Item> handheld(String name, int nutrition, float saturation) {
        return ModItems.ITEMS.register(name, () -> new Item(new Item.Properties().food(
                new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build())));
    }
}
