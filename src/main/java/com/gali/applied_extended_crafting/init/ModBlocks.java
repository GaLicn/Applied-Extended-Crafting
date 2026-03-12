package com.gali.applied_extended_crafting.init;

import com.gali.applied_extended_crafting.Applied_extended_crafting;
import com.gali.applied_extended_crafting.block.TableBasicPatternProviderBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Applied_extended_crafting.MODID);

    public static final DeferredBlock<TableBasicPatternProviderBlock> TABLE_BASIC_PATTERN_PROVIDER = BLOCKS.register(
            "table_basic_pattern_provider",
            TableBasicPatternProviderBlock::new
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
