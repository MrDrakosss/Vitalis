package me.xavi.vitalis.item;

import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AdrenalineItem extends Item {

    public AdrenalineItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 25, 1));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 15, 0));

            SurgeryData.setHeartRate(serverPlayer, Math.min(190, SurgeryData.getHeartRate(serverPlayer) + 45));
            SurgeryData.setBloodPressureSystolic(serverPlayer, SurgeryData.getBloodPressureSystolic(serverPlayer) + 20);
            SurgeryData.setBloodPressureDiastolic(serverPlayer, SurgeryData.getBloodPressureDiastolic(serverPlayer) + 10);

            if (!serverPlayer.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("item.vitalis.adrenaline.tooltip").withStyle(ChatFormatting.GRAY));
    }
}