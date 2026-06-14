package me.xavi.vitalis.item;

import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.registry.ModItems;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloodBagItem extends Item {
    public static final int CAPACITY_ML = 500;

    public BloodBagItem(Properties properties) {
        super(properties.stacksTo(1).durability(CAPACITY_ML));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int remaining = getRemainingBloodMl(stack);

        if (remaining <= 0) {
            player.displayClientMessage(Component.translatable("message.vitalis.blood_bag_empty"), true);
            return InteractionResultHolder.fail(stack);
        }

        double currentBlood = SurgeryData.getBloodMl(player);
        double missingBlood = BloodLevel.MAX_BLOOD_ML - currentBlood;

        if (missingBlood <= 0.0D) {
            player.displayClientMessage(Component.translatable("message.vitalis.blood_full"), true);
            return InteractionResultHolder.fail(stack);
        }

        int restored = Math.min(remaining, (int) Math.ceil(missingBlood));

        SurgeryData.setBloodMl(player, currentBlood + restored);
        int newRemaining = remaining - restored;

        if (newRemaining <= 0) {
            player.setItemInHand(hand, new ItemStack(ModItems.BLOOD_BAG_EMPTY));
        } else {
            setRemainingBloodMl(stack, newRemaining);
        }

        player.displayClientMessage(
                Component.translatable("message.vitalis.blood_restored", restored),
                true
        );

        return InteractionResultHolder.success(stack);
    }

    public static int getRemainingBloodMl(ItemStack stack) {
        return Math.max(0, CAPACITY_ML - stack.getDamageValue());
    }

    private static void setRemainingBloodMl(ItemStack stack, int amount) {
        int clamped = Math.max(0, Math.min(CAPACITY_ML, amount));
        stack.setDamageValue(CAPACITY_ML - clamped);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getRemainingBloodMl(stack) / CAPACITY_ML);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAA0000;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(
                "item.vitalis.blood_bag.remaining",
                getRemainingBloodMl(stack),
                CAPACITY_ML
        ).withStyle(ChatFormatting.RED));
    }
}