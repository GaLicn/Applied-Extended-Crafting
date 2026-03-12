package com.gali.applied_extended_crafting;

import com.gali.applied_extended_crafting.client.render.PatternProviderPowerLightRenderer;
import com.gali.applied_extended_crafting.blockentity.TableAdvancedPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableBasicPatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableElitePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.blockentity.TableUltimatePatternProviderBlockEntity;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

final class ClientModEvents {
    ClientModEvents(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TABLE_BASIC_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TABLE_ADVANCED_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TABLE_ELITE_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TABLE_ULTIMATE_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
    }
}
