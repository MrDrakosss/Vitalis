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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

public class EnergyDrinkItem extends Item {

    public EnergyDrinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 45, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 45, 0));

            SurgeryData.setHeartRate(player, Math.min(180, SurgeryData.getHeartRate(player) + 35));
            SurgeryData.setBloodPressureSystolic(player, SurgeryData.getBloodPressureSystolic(player) + 15);
            SurgeryData.setBloodPressureDiastolic(player, SurgeryData.getBloodPressureDiastolic(player) + 8);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("item.vitalis.energy_drink.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
