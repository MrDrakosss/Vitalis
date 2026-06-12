package me.xavi.vitalis.item;

import me.xavi.vitalis.medical.DownedManager;
import me.xavi.vitalis.util.DownedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DefibrillatorItem extends Item {

    public DefibrillatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof ServerPlayer targetPlayer)) {
            return InteractionResult.PASS;
        }

        if (!DownedData.isDowned(targetPlayer)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide) {
            DownedManager.revive(targetPlayer);

            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }

            player.displayClientMessage(Component.translatable("message.vitalis.player_revived"), true);
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("item.vitalis.defibrillator.tooltip").withStyle(ChatFormatting.GRAY));
    }
}