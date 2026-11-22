package com.cocteleria.registry;

import com.cocteleria.CocteleriaMod;
import com.cocteleria.item.CocktailItem;
import com.cocteleria.item.DartItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CocteleriaMod.MODID);

    /** Dardo para la diana: máximo 6 por jugador (un set). */
    public static final RegistryObject<Item> DART =
            ITEMS.register("dart", () -> new DartItem(new Item.Properties().stacksTo(6)));

    public static final RegistryObject<Item> EMPTY_GLASS =
            ITEMS.register("empty_glass", () -> new Item(new Item.Properties()));

    // Duraciones en ticks (20 ticks = 1 segundo).
    public static final RegistryObject<Item> MOJITO = cocktail("mojito",
            fx(MobEffects.MOVEMENT_SPEED, 1200, 0),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> MARGARITA = cocktail("margarita",
            fx(MobEffects.JUMP, 900, 1),
            fx(MobEffects.CONFUSION, 300, 0),
            tipsy(900, 0));

    public static final RegistryObject<Item> PINA_COLADA = cocktail("pina_colada",
            fx(MobEffects.REGENERATION, 200, 0),
            fx(MobEffects.MOVEMENT_SLOWDOWN, 400, 0),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> BLUE_LAGOON = cocktail("blue_lagoon",
            fx(MobEffects.WATER_BREATHING, 2400, 0),
            fx(MobEffects.DOLPHINS_GRACE, 600, 0),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> TEQUILA_SUNRISE = cocktail("tequila_sunrise",
            fx(MobEffects.FIRE_RESISTANCE, 2400, 0),
            fx(MobEffects.DAMAGE_BOOST, 400, 0),
            fx(MobEffects.CONFUSION, 300, 0),
            tipsy(900, 0));

    public static final RegistryObject<Item> COSMOPOLITAN = cocktail("cosmopolitan",
            fx(MobEffects.LUCK, 3600, 0),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> WHISKY_SOUR = cocktail("whisky_sour",
            fx(MobEffects.DAMAGE_RESISTANCE, 600, 0),
            fx(MobEffects.MOVEMENT_SLOWDOWN, 300, 0),
            fx(MobEffects.CONFUSION, 300, 0),
            tipsy(900, 0));

    public static final RegistryObject<Item> BLACK_RUSSIAN = cocktail("black_russian",
            fx(MobEffects.DAMAGE_BOOST, 900, 0),
            fx(MobEffects.DARKNESS, 200, 0),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(900, 0));

    public static final RegistryObject<Item> GREEN_FAIRY = cocktail("green_fairy",
            fx(MobEffects.NIGHT_VISION, 1800, 0),
            fx(MobEffects.GLOWING, 600, 0),
            fx(MobEffects.LEVITATION, 60, 0),
            fx(MobEffects.CONFUSION, 500, 0),
            tipsy(1200, 0));

    public static final RegistryObject<Item> MAI_TAI = cocktail("mai_tai",
            fx(MobEffects.SLOW_FALLING, 1200, 0),
            fx(MobEffects.CONFUSION, 300, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> SANGRIA = cocktail("sangria",
            fx(MobEffects.ABSORPTION, 1200, 1),
            fx(MobEffects.CONFUSION, 200, 0),
            tipsy(600, 0));

    public static final RegistryObject<Item> LONG_ISLAND = cocktail("long_island",
            fx(MobEffects.MOVEMENT_SPEED, 400, 1),
            fx(MobEffects.BLINDNESS, 100, 0),
            fx(MobEffects.CONFUSION, 800, 0),
            tipsy(1800, 1));

    @SafeVarargs
    private static RegistryObject<Item> cocktail(String name, Supplier<MobEffectInstance>... effects) {
        return ITEMS.register(name, () -> new CocktailItem(new Item.Properties().stacksTo(16), List.of(effects)));
    }

    private static Supplier<MobEffectInstance> fx(net.minecraft.world.effect.MobEffect effect, int ticks, int amplifier) {
        return () -> new MobEffectInstance(effect, ticks, amplifier);
    }

    private static Supplier<MobEffectInstance> tipsy(int ticks, int amplifier) {
        return () -> new MobEffectInstance(ModEffects.TIPSY.get(), ticks, amplifier);
    }
}
