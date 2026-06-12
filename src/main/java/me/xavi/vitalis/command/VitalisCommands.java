package me.xavi.vitalis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class VitalisCommands {

    private static final String[] BODY_PARTS = {
            "head",
            "chest",
            "abdomen",
            "left_arm",
            "right_arm",
            "left_leg",
            "right_leg"
    };

    private static final String[] INJURY_STATUSES = {
            "none",
            "fracture",
            "open_fracture",
            "bullet_wound",
            "cut",
            "burn"
    };

    private VitalisCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vitalis")
                        .requires(source -> source.hasPermission(2))

                        // TODO Törölni fejlesztés után
                        .then(Commands.literal("test-data")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            SurgeryData.setBodyPartHp(player, BodyPart.HEAD, 75);
                                            SurgeryData.setBodyPartHp(player, BodyPart.CHEST, 85);
                                            SurgeryData.setBodyPartHp(player, BodyPart.ABDOMEN, 90);
                                            SurgeryData.setBodyPartHp(player, BodyPart.LEFT_ARM, 70);
                                            SurgeryData.setBodyPartHp(player, BodyPart.RIGHT_ARM, 100);
                                            SurgeryData.setBodyPartHp(player, BodyPart.LEFT_LEG, 35);
                                            SurgeryData.setBodyPartHp(player, BodyPart.RIGHT_LEG, 60);

                                            SurgeryData.setBodyPartStatus(player, BodyPart.HEAD, InjuryStatus.NONE);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.CHEST, InjuryStatus.CUT);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.ABDOMEN, InjuryStatus.NONE);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.LEFT_ARM, InjuryStatus.CUT);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.RIGHT_ARM, InjuryStatus.NONE);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.LEFT_LEG, InjuryStatus.FRACTURE);
                                            SurgeryData.setBodyPartStatus(player, BodyPart.RIGHT_LEG, InjuryStatus.NONE);
                                            SurgeryData.setBloodMl(player, 4300.0D);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aTest data's set for " + player.getName().getString() + "."),
                                                    true
                                            );

                                            return 1;
                                        })))

                        .then(Commands.literal("sethp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("bodyPart", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(BODY_PARTS, builder))
                                                .then(Commands.argument("hp", IntegerArgumentType.integer(0, 100))
                                                        .executes(context -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                            BodyPart part = parseBodyPart(StringArgumentType.getString(context, "bodyPart"));
                                                            int hp = IntegerArgumentType.getInteger(context, "hp");

                                                            SurgeryData.setBodyPartHp(player, part, hp);

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal("§aSet " + player.getName().getString() + " " + part.name() + " HP to " + hp + "."),
                                                                    true
                                                            );

                                                            return 1;
                                                        })))))

                        .then(Commands.literal("setstatus")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("bodyPart", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(BODY_PARTS, builder))
                                                .then(Commands.argument("status", StringArgumentType.word())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(INJURY_STATUSES, builder))
                                                        .executes(context -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                            BodyPart part = parseBodyPart(StringArgumentType.getString(context, "bodyPart"));
                                                            InjuryStatus status = parseStatus(StringArgumentType.getString(context, "status"));

                                                            SurgeryData.setBodyPartStatus(player, part, status);

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal("§aSet " + player.getName().getString() + " " + part.name() + " status to " + status.name() + "."),
                                                                    true
                                                            );

                                                            return 1;
                                                        })))))

                        .then(Commands.literal("damage")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("bodyPart", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(BODY_PARTS, builder))
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                                        .executes(context -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                            BodyPart part = parseBodyPart(StringArgumentType.getString(context, "bodyPart"));
                                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                                            SurgeryData.addBodyPartHp(player, part, -amount);

                                                            int hp = SurgeryData.getBodyPartHp(player, part);

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal("§cDamaged " + player.getName().getString() + " " + part.name() + " by " + amount + ". Current HP: " + hp),
                                                                    true
                                                            );

                                                            return 1;
                                                        })))))

                        .then(Commands.literal("heal")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("bodyPart", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(BODY_PARTS, builder))
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    BodyPart part = parseBodyPart(StringArgumentType.getString(context, "bodyPart"));

                                                    SurgeryData.setBodyPartHp(player, part, part.getMaxHp());
                                                    SurgeryData.setBodyPartStatus(player, part, InjuryStatus.NONE);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("§aHealed " + player.getName().getString() + " " + part.name() + "."),
                                                            true
                                                    );

                                                    return 1;
                                                }))))

                        .then(Commands.literal("healall")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            SurgeryData.resetBodyParts(player);
                                            SurgeryData.resetBlood(player);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aFully healed " + player.getName().getString() + "."),
                                                    true
                                            );

                                            return 1;
                                        })))

                        .then(Commands.literal("blood")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("ml", IntegerArgumentType.integer(0, 5000))
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    int ml = IntegerArgumentType.getInteger(context, "ml");

                                                    SurgeryData.setBloodMl(player, ml);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("§cSet " + player.getName().getString() + " blood to " + ml + " ml."),
                                                            true
                                                    );

                                                    return 1;
                                                }))))
        );
    }

    private static BodyPart parseBodyPart(String value) {
        return BodyPart.valueOf(value.toUpperCase());
    }

    private static InjuryStatus parseStatus(String value) {
        return InjuryStatus.valueOf(value.toUpperCase());
    }
}