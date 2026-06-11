package me.xavi.vitalis.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class BloodParticle extends TextureSheetParticle {

    protected BloodParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            SpriteSet spriteSet
    ) {
        super(level, x, y, z);

        this.pickSprite(spriteSet);

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.rCol = 0.55F;
        this.gCol = 0.02F;
        this.bCol = 0.02F;

        this.quadSize = 0.025F + this.random.nextFloat() * 0.01F;
        this.lifetime = 18 + this.random.nextInt(10);
        this.gravity = 0.45F;
        this.friction = 0.86F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            return new BloodParticle(
                    level,
                    x,
                    y,
                    z,
                    velocityX,
                    velocityY,
                    velocityZ,
                    spriteSet
            );
        }
    }
}