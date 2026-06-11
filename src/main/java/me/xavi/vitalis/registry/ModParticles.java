package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public final class ModParticles {

    public static final SimpleParticleType BLOOD =
            Registry.register(
                    BuiltInRegistries.PARTICLE_TYPE,
                    Vitalis.id("blood"),
                    FabricParticleTypes.simple()
            );

    private ModParticles() {
    }

    public static void initialize() {
    }
}