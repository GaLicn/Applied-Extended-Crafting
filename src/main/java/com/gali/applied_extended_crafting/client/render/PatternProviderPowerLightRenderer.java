package com.gali.applied_extended_crafting.client.render;

import appeng.core.AppEng;
import com.gali.applied_extended_crafting.blockentity.AbstractPatternProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class PatternProviderPowerLightRenderer<T extends AbstractPatternProvider> implements BlockEntityRenderer<T> {
    private static final ModelResourceLocation LIGHTS_MODEL = ModelResourceLocation
            .standalone(AppEng.makeId("block/molecular_assembler_lights"));

    public PatternProviderPowerLightRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T provider, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
            int combinedLight, int combinedOverlay) {
        if (!provider.isPowered()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel lightsModel = minecraft.getModelManager().getModel(LIGHTS_MODEL);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.tripwire());

        minecraft.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffer, null,
                lightsModel, 1, 1, 1, combinedLight, combinedOverlay, ModelData.EMPTY, null);
    }
}
