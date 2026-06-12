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
import net.minecraft.world.phys.Vec3;

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
        applyBrokenLegJumpLimit(player);
        applyNaturalBloodRegen(player);
        updateVitals(player);
    }

    private static final double BROKEN_LEG_MAX_JUMP_VELOCITY = 0.16D;

    private static void applyBrokenLegJumpLimit(Player player) {
        int brokenLegs = brokenLegCount(player);

        if (brokenLegs <= 0) {
            return;
        }

        Vec3 movement = player.getDeltaMovement();

        if (brokenLegs >= 2) {
            if (movement.y > 0.0D) {
                player.setDeltaMovement(
                        movement.x,
                        0.0D,
                        movement.z
                );

                player.hurtMarked = true;
            }

            return;
        }

        if (movement.y > 0.16D) {
            player.setDeltaMovement(
                    movement.x,
                    0.16D,
                    movement.z
            );

            player.hurtMarked = true;
        }
    }

    private static int brokenLegCount(Player player) {
        int count = 0;

        InjuryStatus leftLeg = SurgeryData.getBodyPartStatus(player, BodyPart.LEFT_LEG);
        InjuryStatus rightLeg = SurgeryData.getBodyPartStatus(player, BodyPart.RIGHT_LEG);

        if (leftLeg == InjuryStatus.FRACTURE || leftLeg == InjuryStatus.OPEN_FRACTURE) {
            count++;
        }

        if (rightLeg == InjuryStatus.FRACTURE || rightLeg == InjuryStatus.OPEN_FRACTURE) {
            count++;
        }

        return count;
    }

    private static boolean hasBrokenLeg(Player player) {
        InjuryStatus leftLeg = SurgeryData.getBodyPartStatus(player, BodyPart.LEFT_LEG);
        InjuryStatus rightLeg = SurgeryData.getBodyPartStatus(player, BodyPart.RIGHT_LEG);

        return leftLeg == InjuryStatus.FRACTURE
                || leftLeg == InjuryStatus.OPEN_FRACTURE
                || rightLeg == InjuryStatus.FRACTURE
                || rightLeg == InjuryStatus.OPEN_FRACTURE;
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

    private static void updateVitals(Player player) {
        double blood = SurgeryData.getBloodMl(player);
        double bloodRatio = blood / BloodLevel.MAX_BLOOD_ML;
        bloodRatio = Math.max(0.0D, Math.min(1.0D, bloodRatio));

        int injuryPenalty = 0;

        for (BodyPart part : BodyPart.VALUES) {
            int hp = SurgeryData.getBodyPartHp(player, part);
            InjuryStatus status = SurgeryData.getBodyPartStatus(player, part);

            double partRatio = (double) hp / (double) part.getMaxHp();

            if (partRatio <= 0.35D) {
                injuryPenalty += 12;
            } else if (partRatio <= 0.70D) {
                injuryPenalty += 6;
            }

            if (status == InjuryStatus.OPEN_FRACTURE || status == InjuryStatus.BULLET_WOUND) {
                injuryPenalty += 12;
            } else if (status != InjuryStatus.NONE) {
                injuryPenalty += 5;
            }
        }

        int heartRate;
        int systolic;
        int diastolic;

        if (bloodRatio >= 0.85D) {
            heartRate = 75 + injuryPenalty;
            systolic = 120;
            diastolic = 80;
        } else if (bloodRatio >= 0.65D) {
            heartRate = 95 + injuryPenalty;
            systolic = 105;
            diastolic = 70;
        } else if (bloodRatio >= 0.45D) {
            heartRate = 120 + injuryPenalty;
            systolic = 90;
            diastolic = 60;
        } else if (bloodRatio >= 0.25D) {
            heartRate = 145 + injuryPenalty;
            systolic = 75;
            diastolic = 45;
        } else {
            heartRate = 40;
            systolic = 55;
            diastolic = 30;
        }

        heartRate = Math.min(190, heartRate);

        SurgeryData.setHeartRate(player, heartRate);
        SurgeryData.setBloodPressureSystolic(player, systolic);
        SurgeryData.setBloodPressureDiastolic(player, diastolic);
    }

    private static void applyNaturalBloodRegen(Player player) {
        double blood = SurgeryData.getBloodMl(player);

        if (blood >= BloodLevel.MAX_BLOOD_ML) {
            return;
        }

        boolean bleeding = false;

        for (BodyPart part : BodyPart.VALUES) {
            if (SurgeryData.getBodyPartStatus(player, part).getBleedRateMlPerSecond() > 0.0D) {
                bleeding = true;
                break;
            }
        }

        if (bleeding) {
            return;
        }

        SurgeryData.addBloodMl(player, 0.35D / 20.0D);
    }
}