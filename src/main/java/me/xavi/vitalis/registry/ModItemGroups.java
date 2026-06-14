package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ModItemGroups {

    public static final CreativeModeTab VITALIS_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Vitalis.id("vitalis_tab"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.vitalis.main"))
                    .icon(() -> new ItemStack(ModItems.VITAL_SCANNER))
                    .displayItems((context, entries) -> {
                        for (Block block: ModBlocks.BLOCKS) {
                            entries.accept(block);
                        }

                        for (Item item : ModItems.ITEMS) {
                            entries.accept(item);
                        }
                    })
                    .build()
    );

    public ModItemGroups() {
    }

    public static void initialize() {
        Vitalis.LOGGER.info("Registering Vitalis creative tab");
    }
}
