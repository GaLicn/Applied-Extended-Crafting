package com.gali.applied_extended_crafting;

import appeng.client.gui.style.StyleManager;
import com.gali.applied_extended_crafting.client.gui.TablePatternProviderScreen;
import com.gali.applied_extended_crafting.client.render.PatternProviderPowerLightRenderer;
import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.init.ModMenuTypes;
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
        event.enqueueWork(() -> MenuScreens.register(
                ModMenuTypes.TABLE_PATTERN_PROVIDER.get(),
                this::createTablePatternProviderScreen
        ));
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
