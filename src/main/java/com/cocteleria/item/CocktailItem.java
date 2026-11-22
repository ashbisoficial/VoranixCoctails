package com.cocteleria.item;

import com.cocteleria.registry.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/** Un trago: se bebe como una poción, aplica sus efectos y devuelve un vaso vacío. */
public class CocktailItem extends Item {
    private static final int DRINK_TIME = 32;

    private final List<Supplier<MobEffectInstance>> effects;

    public CocktailItem(Properties properties, List<Supplier<MobEffectInstance>> effects) {
        super(properties);
        this.effects = effects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player p ? p : null;

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!level.isClientSide) {
            for (Supplier<MobEffectInstance> effect : effects) {
                entity.addEffect(effect.get());
            }
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        boolean creative = player != null && player.getAbilities().instabuild;
        if (!creative) {
            stack.shrink(1);
            ItemStack glass = new ItemStack(ModItems.EMPTY_GLASS.get());
            if (stack.isEmpty()) {
                entity.gameEvent(GameEvent.DRINK);
                return glass;
            }
            if (player != null && !player.getInventory().add(glass)) {
                player.drop(glass, false);
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_TIME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
