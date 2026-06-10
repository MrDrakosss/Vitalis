package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<SurgeryTableBlockEntity> SURGERY_TABLE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Vitalis.id("surgery_table_be"),
                    FabricBlockEntityTypeBuilder.create(SurgeryTableBlockEntity::new,
                            ModBlocks.SURGERY_TABLE).build());

    public static void initialize() {}
}