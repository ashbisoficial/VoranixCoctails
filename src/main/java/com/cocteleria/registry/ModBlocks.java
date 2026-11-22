package com.cocteleria.registry;

import com.cocteleria.CocteleriaMod;
import com.cocteleria.block.DecorBlock;
import com.cocteleria.block.GameDecorBlock;
import com.cocteleria.game.BarGame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Bloques decorativos y de juegos. Generado desde tools/decor_spec.py. */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CocteleriaMod.MODID);

    /** Bloques que tienen una partida (usan el BlockEntity de juego). */
    public static final List<RegistryObject<Block>> GAME_BLOCKS = new ArrayList<>();

    public static final RegistryObject<Block> WHISKY_BOTTLE = decor("whisky_bottle", SoundType.GLASS, false, null,
            new double[][]{{5, 0, 5, 11, 8, 11}, {6, 8, 6, 10, 9.5, 10}, {7, 9.5, 7, 9, 12.5, 9}, {6.8, 12.5, 6.8, 9.2, 13.8, 9.2}});

    public static final RegistryObject<Block> RUM_BOTTLE = decor("rum_bottle", SoundType.GLASS, false, null,
            new double[][]{{4.5, 0, 4.5, 11.5, 7, 11.5}, {5.5, 7, 5.5, 10.5, 8.5, 10.5}, {7, 8.5, 7, 9, 11.5, 9}, {6.8, 11.5, 6.8, 9.2, 12.8, 9.2}});

    public static final RegistryObject<Block> TEQUILA_BOTTLE = decor("tequila_bottle", SoundType.GLASS, false, null,
            new double[][]{{4.5, 0, 4.5, 11.5, 7, 11.5}, {5.5, 7, 5.5, 10.5, 8.5, 10.5}, {7, 8.5, 7, 9, 11.5, 9}, {6.8, 11.5, 6.8, 9.2, 12.8, 9.2}});

    public static final RegistryObject<Block> VODKA_BOTTLE = decor("vodka_bottle", SoundType.GLASS, false, null,
            new double[][]{{5.5, 0, 5.5, 10.5, 10, 10.5}, {6.5, 10, 6.5, 9.5, 11.5, 9.5}, {7, 11.5, 7, 9, 14.5, 9}, {6.8, 14.5, 6.8, 9.2, 15.8, 9.2}});

    public static final RegistryObject<Block> GIN_BOTTLE = decor("gin_bottle", SoundType.GLASS, false, null,
            new double[][]{{5, 0, 5, 11, 8, 11}, {6, 8, 6, 10, 9.5, 10}, {7, 9.5, 7, 9, 12.5, 9}, {6.8, 12.5, 6.8, 9.2, 13.8, 9.2}});

    public static final RegistryObject<Block> WINE_BOTTLE = decor("wine_bottle", SoundType.GLASS, false, null,
            new double[][]{{5.5, 0, 5.5, 10.5, 8, 10.5}, {6.5, 8, 6.5, 9.5, 9.5, 9.5}, {7, 9.5, 7, 9, 14.5, 9}, {6.8, 12, 6.8, 9.2, 15, 9.2}});

    public static final RegistryObject<Block> SHAKER = decor("shaker", SoundType.METAL, false, null,
            new double[][]{{5, 0, 5, 11, 5, 11}, {4.8, 4.6, 4.8, 11.2, 5.4, 11.2}, {5.5, 5.4, 5.5, 10.5, 9, 10.5}, {6.5, 9, 6.5, 9.5, 11, 9.5}, {7.2, 11, 7.2, 8.8, 12.2, 8.8}});

    public static final RegistryObject<Block> ICE_BUCKET = decor("ice_bucket", SoundType.METAL, false, null,
            new double[][]{{3, 0, 4, 13, 9, 12}});

    public static final RegistryObject<Block> SHORT_GLASS = decor("short_glass", SoundType.GLASS, false, null,
            new double[][]{{5, 0, 5, 11, 6, 11}});

    public static final RegistryObject<Block> TALL_GLASS = decor("tall_glass", SoundType.GLASS, false, null,
            new double[][]{{5.5, 0, 5.5, 10.5, 9, 10.5}});

    public static final RegistryObject<Block> COCKTAIL_GLASS = decor("cocktail_glass", SoundType.GLASS, false, null,
            new double[][]{{5.5, 0, 5.5, 10.5, 0.6, 10.5}, {7.5, 0.6, 7.5, 8.5, 5.2, 8.5}, {3.5, 5.2, 3.5, 12.5, 9.2, 12.5}});

    public static final RegistryObject<Block> WINE_GLASS = decor("wine_glass", SoundType.GLASS, false, null,
            new double[][]{{6, 0, 6, 10, 0.6, 10}, {7.5, 0.6, 7.5, 8.5, 5, 8.5}, {5.5, 5, 5.5, 10.5, 11.5, 10.5}});

    public static final RegistryObject<Block> JIGGER = decor("jigger", SoundType.METAL, false, null,
            new double[][]{{6, 0, 6, 10, 3, 10}, {7.25, 3, 7.25, 8.75, 4, 8.75}, {5.5, 4, 5.5, 10.5, 8, 10.5}, {5.3, 7.6, 5.3, 10.7, 8, 10.7}});

    public static final RegistryObject<Block> BAR_SPOON = decor("bar_spoon", SoundType.METAL, false, null,
            new double[][]{{6.9, 0, 0.8, 9.1, 1.2, 14.8}});

    public static final RegistryObject<Block> BAR_KNIFE = decor("bar_knife", SoundType.METAL, false, null,
            new double[][]{{7, 0, 1.5, 9, 1, 14.5}});

    public static final RegistryObject<Block> STRAINER = decor("strainer", SoundType.METAL, false, null,
            new double[][]{{4, 0, 4, 15.5, 1.4, 12}});

    public static final RegistryObject<Block> CUTTING_BOARD = decor("cutting_board", SoundType.WOOD, false, null,
            new double[][]{{2, 0, 3, 14, 3, 13}});

    public static final RegistryObject<Block> MUDDLER = decor("muddler", SoundType.WOOD, false, null,
            new double[][]{{6.5, 0, 6.5, 9.5, 2, 9.5}, {7.25, 2, 7.25, 8.75, 11, 8.75}, {6.75, 11, 6.75, 9.25, 12.5, 9.25}});

    public static final RegistryObject<Block> COASTER = decor("coaster", SoundType.WOOD, false, null,
            new double[][]{{4.5, 0, 4.5, 11.5, 0.6, 11.5}});

    public static final RegistryObject<Block> DARTBOARD = decor("dartboard", SoundType.WOOD, true, BarGame.DARTS,
            new double[][]{{0.5, 0.5, 14, 15.5, 15.5, 16}});

    public static final RegistryObject<Block> CHESS_BOARD = decor("chess_board", SoundType.WOOD, false, BarGame.CHESS,
            new double[][]{{0.5, 0, 0.5, 15.5, 1.3, 15.5}});

    public static final RegistryObject<Block> ARM_WRESTLING_TABLE = decor("arm_wrestling_table", SoundType.WOOD, false, BarGame.ARM,
            new double[][]{{0, 0, 2, 16, 15.2, 14}});

    public static final RegistryObject<Block> POOL_TABLE = decor("pool_table", SoundType.WOOD, false, BarGame.POOL,
            new double[][]{{0, 0, 1, 16, 15, 15}});

    public static final RegistryObject<Block> FOOSBALL_TABLE = decor("foosball_table", SoundType.WOOD, false, BarGame.FOOSBALL,
            new double[][]{{0, 0, 3, 16, 12, 13}});

    public static final RegistryObject<Block> DICE = decor("dice", SoundType.WOOD, false, BarGame.DICE,
            new double[][]{{4, 0, 5, 11.7, 3.5, 10.5}});

    public static final RegistryObject<Block> CARD_DECK = decor("card_deck", SoundType.WOOD, false, BarGame.BLACKJACK,
            new double[][]{{2, 0, 3, 14, 2.4, 14}});

    public static final RegistryObject<Block> POKER_CHIPS = decor("poker_chips", SoundType.WOOD, false, null,
            new double[][]{{3, 0, 3, 13, 4, 12.5}});

    private static RegistryObject<Block> decor(String name, SoundType sound, boolean wall, BarGame game, double[][] boxes) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of().strength(0.3F).sound(sound).noOcclusion();
            return game == null ? new DecorBlock(props, boxes, wall) : new GameDecorBlock(props, boxes, wall, game);
        });
        if (game != null) {
            GAME_BLOCKS.add(block);
        }
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
