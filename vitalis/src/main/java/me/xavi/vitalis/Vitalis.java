package me.xavi.vitalis;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
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
        LOGGER.info("Vitalis mod initialized for Minecraft 26.1.2!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
