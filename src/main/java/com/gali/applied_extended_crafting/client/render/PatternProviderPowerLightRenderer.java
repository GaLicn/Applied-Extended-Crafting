package com.gali.applied_extended_crafting.client.render;

import appeng.core.AppEng;
import com.gali.applied_extended_crafting.blockentity.AbstractPatternProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;

public class PatternProviderPowerLightRenderer<T extends AbstractPatternProvider>
        implements BlockEntityRenderer<T, PatternProviderPowerLightRenderer.PowerLightRenderState> {
    public static final Identifier LIGHTS_MODEL_ID = AppEng.makeId("block/molecular_assembler_lights");
    public static final StandaloneModelKey<BlockStateModelPart> LIGHTS_MODEL =
            new StandaloneModelKey<>(LIGHTS_MODEL_ID::toString);

    private final BlockStateModel lightsModel;

    public PatternProviderPowerLightRenderer(BlockEntityRendererProvider.Context context) {
        var modelManager = Minecraft.getInstance().getModelManager();
        this.lightsModel = new SingleVariant(Objects.requireNonNull(modelManager.getStandaloneModel(LIGHTS_MODEL)));
    }

    @Override
    public PowerLightRenderState createRenderState() {
        return new PowerLightRenderState();
    }

    @Override
    public void extractRenderState(T provider, PowerLightRenderState state, float partialTicks, Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(provider, state, partialTicks, cameraPos, crumblingOverlay);

        state.powered = provider.isPowered();
        state.model.clear();
        if (!state.powered || provider.getLevel() == null) {
            return;
        }

        // 独立灯光模型只在设备通电时提取，避免未通电设备提交透明几何体。
        var level = (BlockAndTintGetter) provider.getLevel();
        var parts = state.model.setupModel(new Matrix4f(), this.lightsModel.hasMaterialFlag(
                level, provider.getBlockPos(), provider.getBlockState(), BakedQuad.FLAG_TRANSLUCENT));
        this.lightsModel.collectParts(level, provider.getBlockPos(), provider.getBlockState(),
                state.model.scratchRandomSource(42L), parts);
    }

    @Override
    public void submit(PowerLightRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
                       CameraRenderState cameraRenderState) {
        if (!state.powered || state.model.isEmpty()) {
            return;
        }

        state.model.submit(poseStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    public static final class PowerLightRenderState extends BlockEntityRenderState {
        private final BlockModelRenderState model = new BlockModelRenderState();
        private boolean powered;
    }
}
