package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            Applied_extended_crafting.MODID
    );

    public static final RegistryObject<BlockItem> TABLE_BASIC_PATTERN_PROVIDER = ITEMS.register(
            "table_basic_pattern_provider",
            () -> new BlockItem(ModBlocks.TABLE_BASIC_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> TABLE_ADVANCED_PATTERN_PROVIDER = ITEMS.register(
            "table_advanced_pattern_provider",
            () -> new BlockItem(ModBlocks.TABLE_ADVANCED_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> TABLE_ELITE_PATTERN_PROVIDER = ITEMS.register(
            "table_elite_pattern_provider",
            () -> new BlockItem(ModBlocks.TABLE_ELITE_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> TABLE_ULTIMATE_PATTERN_PROVIDER = ITEMS.register(
            "table_ultimate_pattern_provider",
            () -> new BlockItem(ModBlocks.TABLE_ULTIMATE_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> ENDER_CRAFTER_PATTERN_PROVIDER = ITEMS.register(
            "ender_crafter_pattern_provider",
            () -> new BlockItem(ModBlocks.ENDER_CRAFTER_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> CRAFTER_CORE_PATTERN_PROVIDER = ITEMS.register(
            "crafter_core_pattern_provider",
            () -> new BlockItem(ModBlocks.CRAFTER_CORE_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    public static final RegistryObject<BlockItem> FLUX_CRAFTER_PATTERN_PROVIDER = ITEMS.register(
            "flux_crafter_pattern_provider",
            () -> new BlockItem(ModBlocks.FLUX_CRAFTER_PATTERN_PROVIDER.get(), new Item.Properties())
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
