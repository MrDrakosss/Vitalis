package me.xavi.vitalis.medical;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.registry.ModParticles;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class MedicalSystem {

    private static final int TICKS_PER_SECOND = 20;

    private static final ResourceLocation SLOW_MODIFIER_ID =
            Vitalis.id("medical_leg_slow");

    private static final double SLOW_ONE_LEG = -0.40D;
    private static final double SLOW_BOTH_LEGS = -0.70D;
    private static final double SLOW_UNCONSCIOUS = -0.90D;

    private MedicalSystem() {
    }

    public static void tick(Player player) {
        applyBleeding(player);
        applyMovementSlow(player);
    }

    private static void applyBleeding(Player player) {
        double mlPerSecond = 0.0D;

        for (BodyPart part : BodyPart.VALUES) {
            InjuryStatus status = SurgeryData.getBodyPartStatus(player, part);
            mlPerSecond += status.getBleedRateMlPerSecond();
        }

        if (mlPerSecond > 0.0D) {
            double mlPerTick = mlPerSecond / TICKS_PER_SECOND;

            SurgeryData.addBloodMl(player, -mlPerTick);

            if (player.tickCount % TICKS_PER_SECOND == 0) {
                for (BodyPart part : BodyPart.VALUES) {
                    if (SurgeryData.getBodyPartStatus(player, part) == InjuryStatus.OPEN_FRACTURE) {
                        SurgeryData.addBodyPartHp(player, part, -1);
                    }
                }
            }

            if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 6 == 0) {
                serverLevel.sendParticles(
                        ModParticles.BLOOD,
                        player.getX(),
                        player.getY() + 1.0D,
                        player.getZ(),
                        1,
                        0.25D,
                        0.35D,
                        0.25D,
                        0.02D
                );
            }
        }

        if (BloodLevel.fromMl(SurgeryData.getBloodMl(player)) == BloodLevel.DEAD) {
            if (player.isAlive() && player.level() instanceof ServerLevel serverLevel) {
                player.hurt(
                        serverLevel.damageSources().magic(),
                        Float.MAX_VALUE
                );
            }
        }
    }

    private static void applyMovementSlow(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed == null) {
            return;
        }

        AttributeModifier existing = speed.getModifier(SLOW_MODIFIER_ID);

        if (existing != null) {
            speed.removeModifier(SLOW_MODIFIER_ID);
        }

        double slow = computeSlowAmount(player);

        if (slow < 0.0D) {
            speed.addTransientModifier(
                    new AttributeModifier(
                            SLOW_MODIFIER_ID,
                            slow,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            );
        }
    }

    private static double computeSlowAmount(Player player) {
        if (BloodLevel.fromMl(SurgeryData.getBloodMl(player)) == BloodLevel.UNCONSCIOUS) {
            return SLOW_UNCONSCIOUS;
        }

        int brokenLegs = 0;

        for (BodyPart leg : new BodyPart[]{
                BodyPart.LEFT_LEG,
                BodyPart.RIGHT_LEG
        }) {
            InjuryStatus status = SurgeryData.getBodyPartStatus(player, leg);

            if (status == InjuryStatus.FRACTURE
                    || status == InjuryStatus.OPEN_FRACTURE) {
                brokenLegs++;
            }
        }

        if (brokenLegs >= 2) {
            return SLOW_BOTH_LEGS;
        }

        if (brokenLegs == 1) {
            return SLOW_ONE_LEG;
        }

        return 0.0D;
    }
}