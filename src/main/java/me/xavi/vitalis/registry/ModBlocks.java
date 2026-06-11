package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

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

        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
                entries.accept(SURGERY_TABLE)
        );
    }
}