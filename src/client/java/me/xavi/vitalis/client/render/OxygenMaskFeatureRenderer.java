package me.xavi.vitalis.client.render;

import me.xavi.vitalis.registry.ModItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Példa logika:
 * - akkor rendereli a maszkot, ha a játékos fej slotjában oxygen mask item van.
 * - a tényleges modellhez érdemes a Blockbenchből exportált Java/JSON modellt használni.
 * - ezt a RenderLayer-t PlayerEntityRendererhez kell regisztrálni client initben.
 */
public class OxygenMaskFeatureRenderer extends RenderLayer<AbstractClientPlayer, EntityModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("vitalis", "textures/item/oxygen_mask_wearable.png");

    public OxygenMaskFeatureRenderer(RenderLayerParent<AbstractClientPlayer, EntityModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            AbstractClientPlayer player,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.OXYGEN_MASK)) {
            return;
        }

        matrices.pushPose();

        // A player fejének pozíciójához igazítás.
        // Ha saját JavaModelt exportálsz Blockbenchből, itt hívd meg:
        // oxygenMaskModel.renderToBuffer(...).
        //this.getParentModel().head.translateAndRotate(matrices);

        // Itt placeholder van, mert a Blockbenchből exportált modell osztályát kell bekötni.
        // A mellékelt .bbmodel pontosan ehhez van méretezve:
        // arc elé maszk, oldalt pántok, hátul fej körüli strap, alul oxigéncső.

        matrices.popPose();
    }
}
