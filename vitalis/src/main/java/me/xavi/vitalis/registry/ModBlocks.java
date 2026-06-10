package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
    public static final Block SURGERY_TABLE = registerBlock("surgery_table",
            new SurgeryTableBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Vitalis.id("surgery_table")))
                    .mapColor(MapColor.OAK_TAN)
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .nonOpaque()
            ));

    private static Block registerBlock(String name, Block block) {
        // Blokk regisztrálása a saját kulcsával (a blokk Settings-ében már szerepel)
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Vitalis.id(name));
        Registry.register(Registries.BLOCK, blockKey, block);

        // Item kulcs létrehozása
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Vitalis.id(name));
        // BlockItem létrehozása a megfelelő Settings-sel
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
                entries.add(SURGERY_TABLE));
    }
}