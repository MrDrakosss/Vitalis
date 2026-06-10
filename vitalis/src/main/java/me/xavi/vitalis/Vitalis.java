package me.xavi.vitalis;

import me.xavi.vitalis.registry.ModBlocks;
import me.xavi.vitalis.registry.ModBlockEntities;
import me.xavi.vitalis.registry.ModComponents;
import me.xavi.vitalis.registry.ModNetwork;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vitalis implements ModInitializer {

    public static final String MOD_ID = "vitalis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModComponents.initialize();
        ModNetwork.initialize();
        LOGGER.info("Vitalis initialized for Minecraft 1.21.11!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
