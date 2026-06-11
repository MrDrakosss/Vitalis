package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final BlockEntityType<SurgeryTableBlockEntity> SURGERY_TABLE_BE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Vitalis.id("surgery_table_be"),
                    FabricBlockEntityTypeBuilder
                            .create(
                                    SurgeryTableBlockEntity::new,
                                    ModBlocks.SURGERY_TABLE
                            )
                            .build()
            );


    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}