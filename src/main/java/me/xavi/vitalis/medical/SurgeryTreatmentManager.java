package me.xavi.vitalis.medical;

import me.xavi.vitalis.network.SurgeryTreatmentPayload;
import me.xavi.vitalis.registry.ModItems;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class SurgeryTreatmentManager {

    private static final Map<UUID, ActiveTreatment> ACTIVE = new HashMap<>();

    private SurgeryTreatmentManager() {
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ActiveTreatment>> iterator = ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveTreatment> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null || !SurgeryData.isOnTable(player)) {
                iterator.remove();
                continue;
            }

            ActiveTreatment treatment = entry.getValue();
            treatment.tick();

            ServerPlayNetworking.send(
                    player,
                    new SurgeryTreatmentPayload(
                            true,
                            treatment.progressTicks(),
                            treatment.totalTicks()
                    )
            );

            if (treatment.progressTicks() >= treatment.totalTicks()) {
                finishTreatment(player, treatment);
                ServerPlayNetworking.send(player, new SurgeryTreatmentPayload(false, 0, 0));
                iterator.remove();
            }
        }
    }

    public static void startBodyPartTreatment(ServerPlayer player, BodyPart part) {
        if (ACTIVE.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.vitalis.treatment_already_running"), true);
            return;
        }

        TreatmentPlan plan = getBodyPartPlan(player, part);

        if (plan == null) {
            player.displayClientMessage(Component.translatable("message.vitalis.no_treatment_needed"), true);
            return;
        }

        if (!player.isCreative() && !hasRequirements(player, plan.requirements())) {
            player.displayClientMessage(Component.translatable("message.vitalis.not_enough_supplies"), true);
            return;
        }

        if (!player.isCreative()) {
            consumeRequirements(player, plan.requirements());
        }

        if (plan.durationTicks() <= 0) {
            applyBodyPartHeal(player, part);
            player.displayClientMessage(Component.translatable("message.vitalis.treatment_finished"), true);
            return;
        }

        ACTIVE.put(
                player.getUUID(),
                new ActiveTreatment(part, false, 0, plan.durationTicks())
        );

        player.displayClientMessage(Component.translatable("message.vitalis.surgery_started"), true);
    }

    public static void startBloodTreatment(ServerPlayer player) {
        if (ACTIVE.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.vitalis.treatment_already_running"), true);
            return;
        }

        TreatmentPlan plan = getBloodPlan(player);

        if (plan == null) {
            player.displayClientMessage(Component.translatable("message.vitalis.no_treatment_needed"), true);
            return;
        }

        if (!player.isCreative() && !hasRequirements(player, plan.requirements())) {
            player.displayClientMessage(Component.translatable("message.vitalis.not_enough_supplies"), true);
            return;
        }

        if (!player.isCreative()) {
            consumeRequirements(player, plan.requirements());
        }

        ACTIVE.put(
                player.getUUID(),
                new ActiveTreatment(BodyPart.CHEST, true, 0, plan.durationTicks())
        );

        player.displayClientMessage(Component.translatable("message.vitalis.blood_transfusion_started"), true);
    }

    private static TreatmentPlan getBodyPartPlan(ServerPlayer player, BodyPart part) {
        int hp = SurgeryData.getBodyPartHp(player, part);
        InjuryStatus status = SurgeryData.getBodyPartStatus(player, part);

        if (status == InjuryStatus.NONE && hp >= part.getMaxHp()) {
            return null;
        }

        if (status == InjuryStatus.FRACTURE) {
            return new TreatmentPlan(
                    10 * 20,
                    List.of(
                            new Requirement(ModItems.SPLINT, 1),
                            new Requirement(ModItems.CAST, 1),
                            new Requirement(ModItems.PAINKILLER, 1)
                    )
            );
        }

        if (status == InjuryStatus.OPEN_FRACTURE) {
            return new TreatmentPlan(
                    16 * 20,
                    List.of(
                            new Requirement(ModItems.SURGICAL_KIT, 1),
                            new Requirement(ModItems.SCALPEL, 1),
                            new Requirement(ModItems.SUTURE_KIT, 1),
                            new Requirement(ModItems.STERILE_BANDAGE, 2),
                            new Requirement(ModItems.MORPHINE, 1)
                    )
            );
        }

        if (status == InjuryStatus.BULLET_WOUND) {
            return new TreatmentPlan(
                    14 * 20,
                    List.of(
                            new Requirement(ModItems.SURGICAL_KIT, 1),
                            new Requirement(ModItems.FORCEPS, 1),
                            new Requirement(ModItems.SUTURE_KIT, 1),
                            new Requirement(ModItems.PRESSURE_BANDAGE, 1)
                    )
            );
        }

        if (status == InjuryStatus.CUT) {
            return new TreatmentPlan(
                    0,
                    List.of(
                            new Requirement(ModItems.BANDAGE, 1),
                            new Requirement(ModItems.SUTURE_KIT, 1)
                    )
            );
        }

        if (status == InjuryStatus.BURN) {
            return new TreatmentPlan(
                    0,
                    List.of(
                            new Requirement(ModItems.STERILE_BANDAGE, 1),
                            new Requirement(ModItems.PAINKILLER, 1)
                    )
            );
        }

        return new TreatmentPlan(
                0,
                List.of(
                        new Requirement(ModItems.BANDAGE, 1),
                        new Requirement(ModItems.PAINKILLER, 1)
                )
        );
    }

    private static TreatmentPlan getBloodPlan(ServerPlayer player) {
        if (SurgeryData.getBloodMl(player) >= BloodLevel.MAX_BLOOD_ML) {
            return null;
        }

        return new TreatmentPlan(
                8 * 20,
                List.of(
                        new Requirement(ModItems.BLOOD_BAG, 1),
                        new Requirement(ModItems.IV_SET, 1)
                )
        );
    }

    private static void finishTreatment(ServerPlayer player, ActiveTreatment treatment) {
        if (treatment.bloodTreatment()) {
            SurgeryData.addBloodMl(player, 1800.0D);
            player.displayClientMessage(Component.translatable("message.vitalis.blood_transfusion_finished"), true);
            return;
        }

        applyBodyPartHeal(player, treatment.bodyPart());
        player.displayClientMessage(Component.translatable("message.vitalis.surgery_finished"), true);
    }

    private static void applyBodyPartHeal(ServerPlayer player, BodyPart part) {
        SurgeryData.setBodyPartHp(player, part, part.getMaxHp());
        SurgeryData.setBodyPartStatus(player, part, InjuryStatus.NONE);
    }

    private static boolean hasRequirements(ServerPlayer player, List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            if (countItem(player, requirement.item()) < requirement.amount()) {
                return false;
            }
        }

        return true;
    }

    private static void consumeRequirements(ServerPlayer player, List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            consumeItem(player, requirement.item(), requirement.amount());
        }
    }

    private static int countItem(ServerPlayer player, Item item) {
        int amount = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                amount += stack.getCount();
            }
        }

        return amount;
    }

    private static void consumeItem(ServerPlayer player, Item item, int amount) {
        int remaining = amount;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(item)) {
                continue;
            }

            int remove = Math.min(remaining, stack.getCount());
            stack.shrink(remove);
            remaining -= remove;

            if (remaining <= 0) {
                return;
            }
        }
    }

    public record Requirement(Item item, int amount) {
    }

    private record TreatmentPlan(int durationTicks, List<Requirement> requirements) {
    }

    private static final class ActiveTreatment {

        private final BodyPart bodyPart;
        private final boolean bloodTreatment;
        private int progressTicks;
        private final int totalTicks;

        private ActiveTreatment(
                BodyPart bodyPart,
                boolean bloodTreatment,
                int progressTicks,
                int totalTicks
        ) {
            this.bodyPart = bodyPart;
            this.bloodTreatment = bloodTreatment;
            this.progressTicks = progressTicks;
            this.totalTicks = totalTicks;
        }

        public BodyPart bodyPart() {
            return bodyPart;
        }

        public boolean bloodTreatment() {
            return bloodTreatment;
        }

        public int progressTicks() {
            return progressTicks;
        }

        public int totalTicks() {
            return totalTicks;
        }

        public void tick() {
            progressTicks++;
        }
    }
}