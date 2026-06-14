package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {


    public static List<Block> BLOCKS = new ArrayList<>();

    public static final Block SURGERY_TABLE = registerBlock(
            "surgery_table",
            new SurgeryTableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final Block MEDICAL_REFRIGERATOR = registerBlock(
            "medical_refrigerator",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.REFRIGERATOR,
                    false
            )
    );

    public static final Block SUPPLY_CABINET = registerBlock(
            "supply_cabinet",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.SUPPLY,
                    false
            )
    );

    public static final Block EQUIPMENT_CABINET = registerBlock(
            "equipment_cabinet",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.EQUIPMENT,
                    false
            )
    );

    public static final Block LARGE_MEDICAL_REFRIGERATOR = registerBlock(
            "large_medical_refrigerator",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of().strength(2.5F).requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.REFRIGERATOR,
                    true
            )
    );

    public static final Block LARGE_SUPPLY_CABINET = registerBlock(
            "large_supply_cabinet",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of().strength(2.5F).requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.SUPPLY,
                    true
            )
    );

    public static final Block LARGE_EQUIPMENT_CABINET = registerBlock(
            "large_equipment_cabinet",
            new MedicalCabinetBlock(
                    BlockBehaviour.Properties.of().strength(2.5F).requiresCorrectToolForDrops(),
                    MedicalCabinetBlock.CabinetType.EQUIPMENT,
                    true
            )
    );

    public static final Block STAND = registerBlock(
            "stand",
            new StandBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F)
                            .noOcclusion()
            )
    );

    public static final Block OLD_COMPUTER = registerBlock(
            "old_computer",
            new OldComputerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F)
                            .noOcclusion()
            )
    );

    public static final Block WET_FLOOR = registerBlock(
            "wet_floor",
            new WetFloorBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .noOcclusion()
            )
    );

    private ModBlocks() {
    }

    private static Block registerBlock(String name, Block block) {
        Registry.register(
                BuiltInRegistries.BLOCK,
                Vitalis.id(name),
                block
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                Vitalis.id(name),
                new BlockItem(
                        block,
                        new Item.Properties()
                )
        );

        BLOCKS.add(block);

        return block;
    }

    public static void initialize() {
    }
}