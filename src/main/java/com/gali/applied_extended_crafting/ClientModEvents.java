package com.gali.applied_extended_crafting;

import appeng.client.gui.style.StyleManager;
import com.gali.applied_extended_crafting.client.gui.EnderCrafterPatternProviderScreen;
import com.gali.applied_extended_crafting.client.gui.FluxCrafterPatternProviderScreen;
import com.gali.applied_extended_crafting.client.gui.TablePatternProviderScreen;
import com.gali.applied_extended_crafting.client.render.PatternProviderPowerLightRenderer;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.init.ModMenuTypes;
import com.gali.applied_extended_crafting.menu.EnderCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.menu.FluxCrafterPatternProviderMenu;
import com.gali.applied_extended_crafting.menu.TablePatternProviderMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

final class ClientModEvents {
    ClientModEvents(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.TABLE_PATTERN_PROVIDER.get(), this::createTablePatternProviderScreen);
            MenuScreens.register(ModMenuTypes.ENDER_CRAFTER_PATTERN_PROVIDER.get(),
                    this::createEnderCrafterPatternProviderScreen);
            MenuScreens.register(ModMenuTypes.FLUX_CRAFTER_PATTERN_PROVIDER.get(),
                    this::createFluxCrafterPatternProviderScreen);
        });
    }

    private TablePatternProviderScreen createTablePatternProviderScreen(TablePatternProviderMenu menu,
                                                                        Inventory playerInventory,
                                                                        Component title) {
        return new TablePatternProviderScreen(
                menu,
                playerInventory,
                title,
                StyleManager.loadStyleDoc("/screens/pattern_provider.json")
        );
    }

    private EnderCrafterPatternProviderScreen createEnderCrafterPatternProviderScreen(EnderCrafterPatternProviderMenu menu,
                                                                                      Inventory playerInventory,
                                                                                      Component title) {
        return new EnderCrafterPatternProviderScreen(
                menu,
                playerInventory,
                title,
                StyleManager.loadStyleDoc("/screens/ender_crafter_pattern_provider.json")
        );
    }

    private FluxCrafterPatternProviderScreen createFluxCrafterPatternProviderScreen(FluxCrafterPatternProviderMenu menu,
                                                                                    Inventory playerInventory,
                                                                                    Component title) {
        return new FluxCrafterPatternProviderScreen(
                menu,
                playerInventory,
                title,
                StyleManager.loadStyleDoc("/screens/flux_crafter_pattern_provider.json")
        );
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
        event.registerBlockEntityRenderer(ModBlockEntities.ENDER_CRAFTER_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUX_CRAFTER_PATTERN_PROVIDER.get(),
                PatternProviderPowerLightRenderer::new);
    }
}
