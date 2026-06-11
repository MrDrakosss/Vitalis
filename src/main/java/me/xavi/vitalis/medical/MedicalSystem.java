package me.xavi.vitalis.medical;

import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Server-side per-tick logic for the medical system: applies blood loss
 * from bleeding injuries, and applies/refreshes status effects derived from
 * body part injury statuses and overall blood level.
 * <p>
 * Call {@link #tick(PlayerEntity)} once per player per server tick (20
 * ticks/second).
 */
public final class MedicalSystem {

    private static final int TICKS_PER_SECOND = 20;

    /** Reapply effects periodically so they don't expire mid-condition. */
    private static final int EFFECT_REFRESH_INTERVAL_TICKS = 20;
    private static final int EFFECT_DURATION_TICKS = 30; // slightly longer than the refresh interval

    private MedicalSystem() {
    }

    public static void tick(PlayerEntity player) {
        applyBleeding(player);

        if (player.age % EFFECT_REFRESH_INTERVAL_TICKS == 0) {
            applyInjuryEffects(player);
            applyBloodLevelEffects(player);
        }
    }

    /** Reduces blood volume based on every body part's bleeding rate. */
    private static void applyBleeding(PlayerEntity player) {
        double mlPerSecond = 0.0;
        for (BodyPart part : BodyPart.VALUES) {
            InjuryStatus status = SurgeryData.getBodyPartStatus(player, part);
            mlPerSecond += status.getBleedRateMlPerSecond();
        }

        if (mlPerSecond <= 0.0) return;

        double mlPerTick = mlPerSecond / TICKS_PER_SECOND;
        SurgeryData.addBloodMl(player, -mlPerTick);

        // Open fractures also slowly damage the affected body part itself
        // (infection risk / continuous HP loss).
        for (BodyPart part : BodyPart.VALUES) {
            if (SurgeryData.getBodyPartStatus(player, part) == InjuryStatus.OPEN_FRACTURE) {
                if (player.age % TICKS_PER_SECOND == 0) {
                    SurgeryData.addBodyPartHp(player, part, -1);
                }
            }
        }
    }

    /** Applies vanilla status effects derived from each body part's injury status. */
    private static void applyInjuryEffects(PlayerEntity player) {
        boolean hasFracture = false;
        boolean hasOpenFracture = false;
        boolean hasBulletWound = false;
        boolean hasBurn = false;

        for (BodyPart part : BodyPart.VALUES) {
            InjuryStatus status = SurgeryData.getBodyPartStatus(player, part);
            switch (status) {
                case FRACTURE -> hasFracture = true;
                case OPEN_FRACTURE -> hasOpenFracture = true;
                case BULLET_WOUND -> hasBulletWound = true;
                case BURN -> hasBurn = true;
                default -> {
                }
            }
        }

        // Fracture (closed or open): Slowness II, can't sprint (sprint is
        // disabled by canceling sprint state), reduced jump height (Jump
        // Boost with negative amplifier via Slowness substitute isn't
        // directly possible, so we rely on Slowness + Mining Fatigue to
        // emulate "can barely move").
        if (hasFracture || hasOpenFracture) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, EFFECT_DURATION_TICKS, 1, true, true, true));
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }

        // Bullet wound: shock state, represented as Weakness + Nausea.
        if (hasBulletWound) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, EFFECT_DURATION_TICKS, 1, true, true, true));
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA, EFFECT_DURATION_TICKS, 0, true, true, true));
        }

        // Burn: long regeneration time, represented as a stacking Weakness
        // (slows natural healing) - actual regen-rate hooks would need a
        // mixin into player healing, left for a future pass.
        if (hasBurn) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, true, true, true));
        }
    }

    /** Applies vanilla status effects derived from the player's overall blood level. */
    private static void applyBloodLevelEffects(PlayerEntity player) {
        double bloodMl = SurgeryData.getBloodMl(player);
        BloodLevel level = BloodLevel.fromMl(bloodMl);

        switch (level) {
            case NORMAL -> {
                // No special effects.
            }
            case WEAK -> player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, true, true, true));
            case DIZZY -> {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, true, true, true));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, EFFECT_DURATION_TICKS, 0, true, true, true));
            }
            case UNCONSCIOUS -> {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, EFFECT_DURATION_TICKS, 0, true, false, false));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, EFFECT_DURATION_TICKS, 9, true, false, false));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, EFFECT_DURATION_TICKS, 2, true, false, false));
            }
            case DEAD -> {
                if (player.isAlive() && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    player.damage(serverWorld, player.getDamageSources().magic(), Float.MAX_VALUE);
                }
            }
        }
    }
}
