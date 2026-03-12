package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Applied_extended_crafting.MODID);

    public static final DeferredItem<BlockItem> TABLE_BASIC_PATTERN_PROVIDER = ITEMS.registerSimpleBlockItem(
            ModBlocks.TABLE_BASIC_PATTERN_PROVIDER
    );

    public static final DeferredItem<BlockItem> TABLE_ADVANCED_PATTERN_PROVIDER = ITEMS.registerSimpleBlockItem(
            ModBlocks.TABLE_ADVANCED_PATTERN_PROVIDER
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
