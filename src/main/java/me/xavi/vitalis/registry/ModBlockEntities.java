package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.entity.MedicalCabinetBlockEntity;
import me.xavi.vitalis.block.entity.SurgeryTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

    public static final BlockEntityType<MedicalCabinetBlockEntity> MEDICAL_CABINET =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Vitalis.MOD_ID, "medical_cabinet"),
                    FabricBlockEntityTypeBuilder.create(
                            MedicalCabinetBlockEntity::new,
                            ModBlocks.MEDICAL_REFRIGERATOR,
                            ModBlocks.SUPPLY_CABINET,
                            ModBlocks.EQUIPMENT_CABINET,
                            ModBlocks.LARGE_MEDICAL_REFRIGERATOR,
                            ModBlocks.LARGE_SUPPLY_CABINET,
                            ModBlocks.LARGE_EQUIPMENT_CABINET
                    ).build()
            );


    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}