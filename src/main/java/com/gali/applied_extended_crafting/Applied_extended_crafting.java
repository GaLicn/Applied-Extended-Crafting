package com.gali.applied_extended_crafting;

import com.gali.applied_extended_crafting.init.ModBlockEntities;
import com.gali.applied_extended_crafting.init.ModBlocks;
import com.gali.applied_extended_crafting.init.ModCapabilities;
import com.gali.applied_extended_crafting.init.ModCreativeModeTabs;
import com.gali.applied_extended_crafting.init.ModItems;
import com.gali.applied_extended_crafting.init.ModMenuTypes;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Applied_extended_crafting.MODID)
public class Applied_extended_crafting {
    public static final String MODID = "applied_extended_crafting";

    public Applied_extended_crafting(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCapabilities.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            new ClientModEvents(modEventBus);
        }
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModBlocks.TABLE_BASIC_PATTERN_PROVIDER.get().initializeBlockEntity();
            ModBlocks.TABLE_ADVANCED_PATTERN_PROVIDER.get().initializeBlockEntity();
            ModBlocks.TABLE_ELITE_PATTERN_PROVIDER.get().initializeBlockEntity();
            ModBlocks.TABLE_ULTIMATE_PATTERN_PROVIDER.get().initializeBlockEntity();
            ModBlocks.ENDER_CRAFTER_PATTERN_PROVIDER.get().initializeBlockEntity();
            ModBlocks.FLUX_CRAFTER_PATTERN_PROVIDER.get().initializeBlockEntity();
        });
    }
}
