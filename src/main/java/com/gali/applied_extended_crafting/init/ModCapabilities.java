package com.gali.applied_extended_crafting.init;

import appeng.api.AECapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.TABLE_BASIC_PATTERN_PROVIDER.get(),
                (blockEntity, context) -> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.TABLE_ADVANCED_PATTERN_PROVIDER.get(),
                (blockEntity,context)-> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.TABLE_ELITE_PATTERN_PROVIDER.get(),
                (blockEntity,context)-> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.TABLE_ULTIMATE_PATTERN_PROVIDER.get(),
                (blockEntity,context)-> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.ENDER_CRAFTER_PATTERN_PROVIDER.get(),
                (blockEntity, context) -> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.CRAFTER_CORE_PATTERN_PROVIDER.get(),
                (blockEntity, context) -> blockEntity
        );
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.FLUX_CRAFTER_PATTERN_PROVIDER.get(),
                (blockEntity, context) -> blockEntity
        );
    }
}
